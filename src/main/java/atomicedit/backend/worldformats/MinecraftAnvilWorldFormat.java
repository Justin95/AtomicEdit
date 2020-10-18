
package atomicedit.backend.worldformats;

import atomicedit.backend.chunk.Chunk;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtInterpreter;
import atomicedit.backend.nbt.NbtTag;
import atomicedit.backend.nbt.NbtTypes;
import atomicedit.backend.utils.GeneralUtils;
import atomicedit.logging.Logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class MinecraftAnvilWorldFormat implements WorldFormat{
    
    private static final int NUM_CHUNKS_IN_REGION_FILE = 1024;
    private static final int SECTOR_SIZE_IN_BYTES = 4096;
    private static final int MAX_CHUNK_SIZE = 1024 * 1024; //1 MiB
    private static final int MAX_REGION_FILE_SIZE = NUM_CHUNKS_IN_REGION_FILE * MAX_CHUNK_SIZE + (SECTOR_SIZE_IN_BYTES * 2);
    
    //https://minecraft.gamepedia.com/Region_file_format
    
    private String worldFilepath;
    
    public MinecraftAnvilWorldFormat(String worldFilepath){
        this.worldFilepath = worldFilepath;
    }
    
    
    @Override
    public void setWorld(String filepath){
        this.worldFilepath = filepath;
    }
    
    @Override
    public void writeChunks(Map<ChunkCoord, ChunkController> chunks) throws IOException, CorruptedRegionFileException {
        Map<String, Map<ChunkCoord, Chunk>> regionFileNameToChunksMap = catigorizeByRegionFile(chunks);
        for(String regionFileName : regionFileNameToChunksMap.keySet()){
            Map<ChunkCoord, Chunk> chunksInThisRegion = regionFileNameToChunksMap.get(regionFileName);
            if(chunksInThisRegion == null || chunksInThisRegion.isEmpty()){
                continue;
            }
            writeToRegionFile(regionFileName, chunksInThisRegion);
        }
    }
    
    @Override
    public Map<ChunkCoord, Chunk> readChunks(Collection<ChunkCoord> chunkCoords){
        HashMap<ChunkCoord, Chunk> chunks = new HashMap<>();
        for(ChunkCoord coord : chunkCoords){ //in the future this should reuse datainputstreams, it is wasteful to open and close them for each chunk
            Chunk chunk = readChunk(coord);
            if(chunk != null){
                chunks.put(coord, chunk);
            }
        }
        return chunks;
    }
    
    /**
     * Close any opened files.
     */
    @Override
    public void close(){
        
    }
    
    private Chunk readChunk(ChunkCoord chunkCoord){
        DataInputStream regionInput = getRegionAsDataInputStream(chunkCoord);
        if(regionInput == null) return null;
        int chunkIndexOffset = 4 * ((chunkCoord.x & 31) + (chunkCoord.z & 31) * 32);
        Chunk chunk = null;
        try{
            regionInput.skip(chunkIndexOffset);
            int index = regionInput.readInt();
            byte chunkLengthInSectors = (byte)(index & 255);
            int chunkOffset = index >> 8;
            if(chunkOffset >= 2){
                regionInput.skip(chunkOffset * SECTOR_SIZE_IN_BYTES - (chunkIndexOffset + 4)); //set data stream to start of chunk
                chunk = readChunkFromDataStream(regionInput);
            }
        }catch(IOException e){
            Logger.error("IOException while reading chunk.", e);
        }
        try{
            regionInput.close();
        }catch(IOException e){
            Logger.error("IOException closing stream" + e);
        }
        return chunk;
    }
    
    private Chunk readChunkFromDataStream(DataInputStream input) throws IOException{
        //https://minecraft.gamepedia.com/Region_file_format
        int chunkLength = input.readInt() - 1; //-1 for the compression type
        byte compressionType = input.readByte();
        byte[] rawChunkData = new byte[chunkLength];
        input.read(rawChunkData);
        rawChunkData = GeneralUtils.decompressZippedByteArray(rawChunkData); //minecraft always uses zlib, theoretically if compressionType were 1 then use Gzip
        try{
            NbtTag chunkNbt = NbtInterpreter.interpretNbt(rawChunkData);
            //this is a good place to print chunk data to look at chunk formats
            return interpretChunk(chunkNbt);
        }catch(MalformedNbtTagException e){
            Logger.error("Malformed Nbt in chunk");
            return null;
        }
    }
    
    private static String getRegionFileName(ChunkCoord chunkCoord){
        return "r." + (int) Math.floor(chunkCoord.x / 32.0) + "." + (int) Math.floor(chunkCoord.z / 32.0) + ".mca";
    }
    
    private DataInputStream getRegionAsDataInputStream(ChunkCoord chunkCoord){
        String filepath = worldFilepath + "/region/" + getRegionFileName(chunkCoord);
        DataInputStream regionInput = null;
        try{
            regionInput = new DataInputStream(new FileInputStream(filepath));
        }catch(FileNotFoundException e){
            Logger.info("Region file not found, chunk not generated yet");
        }
        return regionInput;
    }
    
    private DataInputStream getRegionAsDataInputStream(String regionFileName) throws IOException {
        String filepath = worldFilepath + "/region/" + regionFileName;
        DataInputStream regionInput = null;
        try{
            byte[] rawRegionFile = Files.readAllBytes(Paths.get(filepath));
            regionInput = new DataInputStream(new ByteArrayInputStream(rawRegionFile));
        }catch(FileNotFoundException e){
            Logger.info("Region file not found, chunk not generated yet");
        }
        return regionInput;
    }
    
    public Chunk interpretChunk(NbtTag chunkNbt) throws MalformedNbtTagException{
        return new Chunk(NbtTypes.getAsCompoundTag(chunkNbt));
    }
    
    /**
     * Organize the given chunks by what region file they belong in.
     * @param chunks
     * @return a map from region file name to a map from a chunk's coord to the chunk.
     */
    private static Map<String, Map<ChunkCoord, Chunk>> catigorizeByRegionFile(Map<ChunkCoord, ChunkController> chunks){
        Map<String, Map<ChunkCoord, Chunk>> regionToChunkMap = new HashMap<>();
        for(ChunkCoord coord : chunks.keySet()){
            String regionFileName = getRegionFileName(coord);
            if(!regionToChunkMap.containsKey(regionFileName)){
                regionToChunkMap.put(regionFileName, new HashMap<>());
            }
            regionToChunkMap.get(regionFileName).put(coord, chunks.get(coord).getChunk());
        }
        return regionToChunkMap;
    }
    
    private void writeToRegionFile(String regionFileName, Map<ChunkCoord, Chunk> chunks) throws IOException, CorruptedRegionFileException{
        DataInputStream oldRegionFile = getRegionAsDataInputStream(regionFileName);
        byte[] oldChunkLocations = new byte[SECTOR_SIZE_IN_BYTES]; //locations and timestamps take up one sector each
        byte[] timestamps = new byte[SECTOR_SIZE_IN_BYTES];
        byte[][] oldChunkDatas = new byte[NUM_CHUNKS_IN_REGION_FILE][];//not including length header field, but does include compression header field, compressed chunk data
        byte[][] newChunkDatas = new byte[NUM_CHUNKS_IN_REGION_FILE][];//not including length header or compression header field, only compressed chunk data, compression always zip
        for(ChunkCoord coord : chunks.keySet()){ //put chunks in the position they appear in the Anvil file header
            Chunk chunk = chunks.get(coord);
            chunk.setNeedsSaving(false);
            int chunkIndex = ((coord.x & 31) + (coord.z & 31) * 32);
            ByteArrayOutputStream rawChunkDataStream = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(rawChunkDataStream);
            NbtTag.writeTag(output, chunk.getChunkTag());
            byte[] compressedChunkData = GeneralUtils.compressByteArrayToZip(rawChunkDataStream.toByteArray());
            newChunkDatas[chunkIndex] = compressedChunkData;
        }
        if(oldRegionFile != null){
            oldRegionFile.read(oldChunkLocations);
            oldRegionFile.read(timestamps);
            oldRegionFile.mark(MAX_REGION_FILE_SIZE); //mark start of chunk data
            for(int i = 0; i < oldChunkLocations.length; i += 4){ //iterate over each 4 byte chunk location
                int locHeadOne = Byte.toUnsignedInt(oldChunkLocations[i]);
                int locHeadTwo = Byte.toUnsignedInt(oldChunkLocations[i+1]);
                int locHeadThree = Byte.toUnsignedInt(oldChunkLocations[i+2]);
                int chunkOffset = (locHeadOne << 16) + (locHeadTwo << 8) + locHeadThree;
                int chunkSizeInSectors = Byte.toUnsignedInt(oldChunkLocations[i+3]);
                if(chunkOffset == 0 || chunkSizeInSectors == 0){
                    continue;
                }
                int headerIndex = i / 4;
                oldRegionFile.reset();
                oldRegionFile.skipBytes((chunkOffset - 2) * SECTOR_SIZE_IN_BYTES); //chunk offset starts at 2
                byte[] chunkData = readChunkData(oldRegionFile, chunkSizeInSectors);
                if(chunkData == null){
                    continue;
                }
                oldChunkDatas[headerIndex] = chunkData;
            }
            oldRegionFile.close();
        }
        writeRegionFile(regionFileName, timestamps, oldChunkDatas, newChunkDatas);
    }
    
    private void writeRegionFile(String regionFileName, byte[] timestampsHeader, byte[][] prevChunkDatas, byte[][] newChunkDatas) throws IOException{
        String filepath = worldFilepath + "/region/" + regionFileName;
        byte[] chunkLocHeader = calcLocHeader(prevChunkDatas, newChunkDatas);
        DataOutputStream regionFile;
        try{
            regionFile = new DataOutputStream(new FileOutputStream(filepath));
        }catch(FileNotFoundException e){
            Logger.error("Failed to write region file.", e);
            return;
        }
        regionFile.write(chunkLocHeader);
        regionFile.write(timestampsHeader);
        for(int i = 0; i < NUM_CHUNKS_IN_REGION_FILE; i++){
            byte[] chunkData;
            int bytesWritten = 0;
            if(newChunkDatas[i] != null){
                chunkData = newChunkDatas[i];
                regionFile.writeInt(chunkData.length + 1); //write datalength and length of compression type field
                regionFile.writeByte(2); //write 2 for zip compression type
                bytesWritten += 5; //wrote 5 header bytes
            }else if(prevChunkDatas[i] != null){
                chunkData = prevChunkDatas[i];
                regionFile.writeInt(chunkData.length); //chunkData includes compression type byte
                bytesWritten += 4; //wrote 4 header bytes
            }else{
                continue;
            }
            regionFile.write(chunkData);
            bytesWritten += chunkData.length;
            int fillerNeeded = (bytesWritten % SECTOR_SIZE_IN_BYTES) != 0 ? SECTOR_SIZE_IN_BYTES - (bytesWritten % SECTOR_SIZE_IN_BYTES) : 0;
            regionFile.write(new byte[fillerNeeded]); //maybe not the best way to write fillerNeeded bytes all set to 0
        }
        regionFile.close();
    }
    
    /**
     * Create the location header for the region file that would contain the given chunks.
     * Chunks in oldChunks are only considered to be in the file if there is no newChunk in the
     * same index.
     * @param oldChunks
     * @param newChunks
     * @return the location header for the chunks
     */
    private static byte[] calcLocHeader(byte[][] oldChunks, byte[][] newChunks){
        if(oldChunks.length != NUM_CHUNKS_IN_REGION_FILE || newChunks.length != NUM_CHUNKS_IN_REGION_FILE){
            Logger.error("Tried to make region file header for wrong number of chunks");
            throw new IllegalArgumentException("Wrong number of chunks being written to region file");
        }
        byte[] locHeader = new byte[SECTOR_SIZE_IN_BYTES];
        int sectorIndex = 2; //chunk data starts in sector 2 (3rd sector) of region file
        for(int i = 0; i < NUM_CHUNKS_IN_REGION_FILE; i++){
            byte[] chunkData = newChunks[i] != null ? newChunks[i] : oldChunks[i];
            boolean chunkDataIncludesCompressionHeader = newChunks[i] == null; //new chunks do not have compression type header but old chunks do
            if(chunkData == null || chunkData.length == 0){
                continue;
            }
            int totalChunkDataLength = chunkData.length + (chunkDataIncludesCompressionHeader ? 4 : 5); //4 bytes for length field, a 5th byte for compression header
            int lengthInSectors = (totalChunkDataLength / SECTOR_SIZE_IN_BYTES) + (totalChunkDataLength % SECTOR_SIZE_IN_BYTES == 0 ? 0 : 1); //number of sectors to hold data
            int locHeaderIndex = i * 4;
            locHeader[locHeaderIndex]     = (byte)(sectorIndex >> 16);
            locHeader[locHeaderIndex + 1] = (byte)(sectorIndex >> 8);
            locHeader[locHeaderIndex + 2] = (byte)(sectorIndex);
            locHeader[locHeaderIndex + 3] = (byte)lengthInSectors;
            sectorIndex += lengthInSectors;
        }
        return locHeader;
    }
    
    /**
     * Read the chunk data from an anvil file. The returned byte[] contains the compressed chunk data and the header
     * information without the data length. Assume the regionFile DataInputStream is set to the beginning of the chunk data header.
     * @param regionFile
     * @return
     * @throws IOException 
     */
    private byte[] readChunkData(DataInputStream regionFile, int chunkSizeInSectors) throws IOException, CorruptedRegionFileException {
        int chunkDataLength;
        try {
            chunkDataLength = regionFile.readInt();
        } catch(EOFException e){
            return null;
        }
        int chunkAndHeaderLength = chunkDataLength + 4;
        if(chunkAndHeaderLength > chunkSizeInSectors * SECTOR_SIZE_IN_BYTES){
            throw new CorruptedRegionFileException("Read a chunk length ("+chunkAndHeaderLength+") greater than the header specifies ("+(chunkSizeInSectors * SECTOR_SIZE_IN_BYTES)+").");
        }
        if(chunkDataLength == 0){
            return null;
        }
        byte[] chunkData = new byte[chunkDataLength];
        int bytesRead = regionFile.read(chunkData);
        if(bytesRead == -1){ //EOF
            return null;
        }
        if(bytesRead != chunkDataLength){
            Logger.error("Tried to read " + chunkDataLength + " bytes of chunk data but only read " + bytesRead + " bytes.");
        }
        int bytesToSkip = (chunkSizeInSectors * SECTOR_SIZE_IN_BYTES) - (chunkDataLength + 4);
        int bytesSkipped = regionFile.skipBytes(bytesToSkip); //skip over padded bytes in sector so the data input stream stays aligned
        if(bytesSkipped != bytesToSkip){
            Logger.error("Tried to skip " + bytesToSkip + " bytes, but only skipped " + bytesSkipped + " bytes.");
        }
        return chunkData;
    }
    
}
