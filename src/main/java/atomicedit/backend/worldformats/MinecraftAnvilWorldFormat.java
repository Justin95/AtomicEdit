
package atomicedit.backend.worldformats;

import atomicedit.backend.chunk.Chunk;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtInterpreter;
import atomicedit.backend.nbt.NbtTag;
import atomicedit.backend.nbt.NbtTypes;
import atomicedit.backend.utils.GeneralUtils;
import atomicedit.logging.Logger;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author Justin Bonner
 */
public class MinecraftAnvilWorldFormat implements WorldFormat{
    
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
    public void writeChunks(Collection<Chunk> chunks){
        throw new UnsupportedOperationException(); //TODO
    }
    
    @Override
    public HashMap<ChunkCoord, Chunk> readChunks(Collection<ChunkCoord> chunkCoords){
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
                regionInput.skip(chunkOffset * 4096 - (chunkIndexOffset + 4)); //set data stream to start of chunk
                chunk = readChunkFromDataStream(regionInput);
            }
        }catch(IOException e){
            Logger.error("IOException while reading chunk" + e);
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
            //Logger.info(chunkNbt.toString());
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
    
    
    public Chunk interpretChunk(NbtTag chunkNbt) throws MalformedNbtTagException{
        return new Chunk(NbtTypes.getAsCompoundTag(chunkNbt));
    }
    
}
