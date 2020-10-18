
package atomicedit.backend.utils;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.ChunkSectionCoord;
import atomicedit.backend.blockprovider.BlockProvider;
import atomicedit.backend.chunk.Chunk;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.ChunkSection;
import atomicedit.backend.entity.Entity;
import atomicedit.backend.entity.EntityCoord;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.volumes.Box;
import atomicedit.volumes.Volume;
import atomicedit.volumes.WorldVolume;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class ChunkUtils {
    
    
    /**
     * Copy the given BlockProvider into the given chunks. Will likely throw a null pointer if the blockProvider
     * has blocks in a chunk not in chunkControllers.
     * @param chunks all the chunks that are contained in the block provider's volume
     * @param blockProvider the source of the blocks to write into the chunks
     * @param smallestPoint
     * @throws MalformedNbtTagException 
     */
    public static void writeBlocksIntoChunks(Collection<Chunk> chunks, BlockProvider blockProvider, BlockCoord smallestPoint) throws MalformedNbtTagException{
        Volume volume = blockProvider.getVolume();
        Box enclosingBox = volume.getEnclosingBox();
        BlockCoord largestPoint = new BlockCoord(smallestPoint.x + enclosingBox.getXLength(), smallestPoint.y + enclosingBox.getYLength(), smallestPoint.z + enclosingBox.getZLength());
        ChunkSectionCoord largestChunk = largestPoint.getChunkSectionCoord();
        ChunkSectionCoord smallestChunk = smallestPoint.getChunkSectionCoord();
        
        int xLengthInChunks = largestChunk.x - smallestChunk.x + 1; //inclusive
        int yLengthInChunks = largestPoint.getSubChunkIndex() - smallestPoint.getSubChunkIndex() + 1;
        int zLengthInChunks = largestChunk.z - smallestChunk.z + 1; //inclusive
        ChunkSectionBlocks[] chunkSectionBlocks = getChunkSectionBlocks(chunks, smallestChunk, largestChunk, xLengthInChunks, yLengthInChunks, zLengthInChunks);
        int xOffset = smallestPoint.getChunkLocalX();
        int yOffset = smallestPoint.getSubChunkLocalY();
        int zOffset = smallestPoint.getChunkLocalZ();
        blockProvider.doForBlock((x, y, z, block) -> {
            int chunkSectionIndex = GeneralUtils.getIndexYZX((x + xOffset) / ChunkSection.SIDE_LENGTH,
                                                             (y + yOffset) / ChunkSection.SIDE_LENGTH,
                                                             (z + zOffset) / ChunkSection.SIDE_LENGTH,
                                                             xLengthInChunks,
                                                             zLengthInChunks);
            int indexInSection = GeneralUtils.getIndexYZX((x + xOffset) % ChunkSection.SIDE_LENGTH,
                                                          (y + yOffset) % ChunkSection.SIDE_LENGTH,
                                                          (z + zOffset) % ChunkSection.SIDE_LENGTH,
                                                          ChunkSection.SIDE_LENGTH);
            chunkSectionBlocks[chunkSectionIndex].blocks[indexInSection] = block;
        });
        for(ChunkSectionBlocks section : chunkSectionBlocks){ //update chunk controllers
            section.chunk.getChunkSection(section.sectionIndex).setBlocks(section.blocks);
        }
    }
    
    /**
     * Creates an array of ChunkSectionBlocks objects ordered in Y, Z, X order. Where Y, Z, and X are the chunk sections offset from the
     * smallest chunk section.
     * @param chunks
     * @param smallestChunk
     * @param largestChunk
     * @param xChunkLen
     * @param yChunkLen
     * @param zChunkLen
     * @return
     * @throws MalformedNbtTagException 
     */
    private static ChunkSectionBlocks[] getChunkSectionBlocks(Collection<Chunk> chunks,
                                                             ChunkSectionCoord smallestChunk,
                                                             ChunkSectionCoord largestChunk,
                                                             int xChunkLen,
                                                             int yChunkLen,
                                                             int zChunkLen)throws MalformedNbtTagException{
        ChunkSectionBlocks[] chunkSectionBlocks = new ChunkSectionBlocks[xChunkLen * yChunkLen * zChunkLen];//y, z, x order
        for(Chunk chunk : chunks){
            ChunkCoord chunkCoord = chunk.getChunkCoord();
            int chunkX = chunkCoord.x - smallestChunk.x;
            int chunkZ = chunkCoord.z - smallestChunk.z;
            for(int sectionIndex = smallestChunk.y; sectionIndex <= largestChunk.y; sectionIndex++){
                int chunkY = sectionIndex - smallestChunk.y;
                chunkSectionBlocks[GeneralUtils.getIndexYZX(chunkX, chunkY, chunkZ, xChunkLen, zChunkLen)] = 
                    new ChunkSectionBlocks(chunk, chunk.getBlocks(sectionIndex), sectionIndex);
            }
        }
        return chunkSectionBlocks;
    }
    
    private static class ChunkSectionBlocks{
        public final int sectionIndex;
        public final short[] blocks;
        public final Chunk chunk;
        
        public ChunkSectionBlocks(Chunk chunk, short[] blocks, int sectionIndex){
            this.blocks = blocks;
            this.chunk = chunk;
            this.sectionIndex = sectionIndex;
        }
        
    }
    
    public static void writeBlockEntitiesIntoChunks(Collection<Chunk> controllers, Collection<BlockEntity> blockEntities) throws MalformedNbtTagException{
        Map<ChunkCoord, Chunk> controllerMap = new HashMap<>();
        for(Chunk controller : controllers){
            controllerMap.put(controller.getChunkCoord(), controller);
        }
        writeBlockEntitiesIntoChunks(controllerMap, blockEntities);
    }
    
    public static void writeBlockEntitiesIntoChunks(Map<ChunkCoord, Chunk> controllers, Collection<BlockEntity> blockEntities) throws MalformedNbtTagException{
        for(BlockEntity blockEntity : blockEntities){
            BlockCoord coord = blockEntity.getBlockCoord();
            Chunk chunk = controllers.get(coord.getChunkCoord());
            chunk.getBlockEntities().add(blockEntity);
        }
    }
    
    public static void writeEntitiesIntoChunks(Collection<Chunk> controllers, Collection<Entity> entities) throws MalformedNbtTagException{
        Map<ChunkCoord, Chunk> controllerMap = getChunkMap(controllers);
        writeEntitiesIntoChunks(controllerMap, entities);
    }
    
    public static void writeEntitiesIntoChunks(Map<ChunkCoord, Chunk> controllers, Collection<Entity> entities) throws MalformedNbtTagException{
        for(Entity entity : entities){
            BlockCoord coord = entity.getCoord().getBlockCoord();
            Chunk chunk = controllers.get(coord.getChunkCoord());
            chunk.getEntities().add(entity);
        }
    }
    
    private static Map<ChunkCoord, Chunk> getChunkMap(Collection<Chunk> chunks) throws MalformedNbtTagException{
        Map<ChunkCoord, Chunk> chunkMap = new HashMap<>();
        for(Chunk chunk : chunks){
            chunkMap.put(chunk.getChunkCoord(), chunk);
        }
        return chunkMap;
    }
    
    public static short[] readBlocksFromChunks(Collection<Chunk> controllers, WorldVolume readVolume) throws MalformedNbtTagException{
        BlockCoord smallestPoint = readVolume.getSmallestPoint();
        Box enclosingBox = readVolume.getEnclosingBox();
        BlockCoord largestPoint = new BlockCoord(smallestPoint.x + enclosingBox.getXLength(), smallestPoint.y + enclosingBox.getYLength(), smallestPoint.z + enclosingBox.getZLength());
        ChunkSectionCoord largestChunk = largestPoint.getChunkSectionCoord();
        ChunkSectionCoord smallestChunk = smallestPoint.getChunkSectionCoord();
        
        int xLengthInChunks = largestChunk.x - smallestChunk.x + 1; //inclusive
        int yLengthInChunks = largestPoint.getSubChunkIndex() - smallestPoint.getSubChunkIndex() + 1;
        int zLengthInChunks = largestChunk.z - smallestChunk.z + 1; //inclusive
        ChunkSectionBlocks[] chunkSectionBlocks = getChunkSectionBlocks(controllers, smallestChunk, largestChunk, xLengthInChunks, yLengthInChunks, zLengthInChunks);
        int xOffset = smallestPoint.getChunkLocalX();
        int yOffset = smallestPoint.getSubChunkLocalY();
        int zOffset = smallestPoint.getChunkLocalZ();
        short[] blocks = new short[enclosingBox.getNumBlocksContained()]; //blocks not in volume are undefined, left as 0 in this case
        readVolume.doForXyz((x, y, z) -> {
            int chunkSectionIndex = GeneralUtils.getIndexYZX((x + xOffset) / ChunkSection.SIDE_LENGTH,
                                                             (y + yOffset) / ChunkSection.SIDE_LENGTH,
                                                             (z + zOffset) / ChunkSection.SIDE_LENGTH,
                                                             xLengthInChunks,
                                                             zLengthInChunks);
            int indexInSection = GeneralUtils.getIndexYZX((x + xOffset) % ChunkSection.SIDE_LENGTH,
                                                          (y + yOffset) % ChunkSection.SIDE_LENGTH,
                                                          (z + zOffset) % ChunkSection.SIDE_LENGTH,
                                                          ChunkSection.SIDE_LENGTH);
            int blocksIndex = GeneralUtils.getIndexYZX(x, y, z, enclosingBox.getXLength(), enclosingBox.getZLength());
            blocks[blocksIndex] = chunkSectionBlocks[chunkSectionIndex].blocks[indexInSection];
        });
        return blocks;
    }
    
    public static short[] readBlocksFromChunks(Map<ChunkCoord, Chunk> controllers, WorldVolume readVolume) throws MalformedNbtTagException{
        return readBlocksFromChunks(controllers.values(), readVolume);
    }
    
    public static Collection<Entity> readEntitiesFromChunks(Collection<Chunk> controllers, WorldVolume readVolume) throws MalformedNbtTagException{
        List<Entity> entities = new ArrayList<>();
        for(Chunk controller : controllers){
            for(Entity entity : controller.getEntities()){
                EntityCoord coord = entity.getCoord();
                if(readVolume.containsCoord((int)coord.x, (int)coord.y, (int)coord.z)){
                    //could add an 'entity filter' test that can be passed in as an optional param
                    entities.add(entity);
                }
            }
        }
        return entities;
    }
    
    public static Collection<Entity> readEntitiesFromChunks(Map<ChunkCoord, Chunk> controllers, WorldVolume readVolume) throws MalformedNbtTagException{
        return readEntitiesFromChunks(controllers.values(), readVolume);
    }
    
    public static Collection<BlockEntity> readBlockEntitiesFromChunks(Collection<Chunk> controllers, WorldVolume readVolume) throws MalformedNbtTagException{
        List<BlockEntity> blockEntities = new ArrayList<>();
        for(Chunk controller : controllers){
            for(BlockEntity blockEntity : controller.getBlockEntities()){
                BlockCoord coord = blockEntity.getBlockCoord();
                if(readVolume.containsCoord((int)coord.x, (int)coord.y, (int)coord.z)){
                    //could add a 'block entity filter' test that can be passed in as an optional param
                    blockEntities.add(blockEntity);
                }
            }
        }
        return blockEntities;
    }
    
    public static Collection<BlockEntity> readBlockEntitiesFromChunks(Map<ChunkCoord, Chunk> controllers, WorldVolume readVolume) throws MalformedNbtTagException{
        return readBlockEntitiesFromChunks(controllers.values(), readVolume);
    }
    
}
