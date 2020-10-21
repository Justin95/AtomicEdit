
package atomicedit.backend.utils;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.ChunkSectionCoord;
import atomicedit.backend.blockprovider.BlockProvider;
import atomicedit.backend.chunk.ChunkController;
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
     * @param chunkControllers all the chunks that are contained in the block provider's volume
     * @param blockProvider the source of the blocks to write into the chunks
     * @param smallestPoint
     * @throws MalformedNbtTagException 
     */
    public static void writeBlocksIntoChunks(Collection<ChunkController> chunkControllers, BlockProvider blockProvider, BlockCoord smallestPoint) throws MalformedNbtTagException{
        Volume volume = blockProvider.getVolume();
        Box enclosingBox = volume.getEnclosingBox();
        BlockCoord largestPoint = new BlockCoord(
                smallestPoint.x + enclosingBox.getXLength(),
                smallestPoint.y + enclosingBox.getYLength(),
                smallestPoint.z + enclosingBox.getZLength()
        );
        ChunkSectionCoord largestChunk = largestPoint.getChunkSectionCoord();
        ChunkSectionCoord smallestChunk = smallestPoint.getChunkSectionCoord();
        
        int xLengthInChunks = largestChunk.x - smallestChunk.x + 1; //inclusive
        int yLengthInChunks = largestChunk.y - smallestChunk.y + 1;
        int zLengthInChunks = largestChunk.z - smallestChunk.z + 1; //inclusive
        ChunkSectionBlocks[] chunkSectionBlocks = getChunkSectionBlocks(chunkControllers, smallestChunk, largestChunk, xLengthInChunks, yLengthInChunks, zLengthInChunks);
        int xOffset = smallestPoint.getChunkLocalX();
        int yOffset = smallestPoint.getSubChunkLocalY();
        int zOffset = smallestPoint.getChunkLocalZ();
        blockProvider.doForBlock((x, y, z, block) -> {
            int chunkSectionIndex = GeneralUtils.getIndexYZX(
                    (x + xOffset) / ChunkSection.SIDE_LENGTH,
                    (y + yOffset) / ChunkSection.SIDE_LENGTH,
                    (z + zOffset) / ChunkSection.SIDE_LENGTH,
                    xLengthInChunks,
                    zLengthInChunks
            );
            int indexInSection = GeneralUtils.getIndexYZX(
                    (x + xOffset) % ChunkSection.SIDE_LENGTH,
                    (y + yOffset) % ChunkSection.SIDE_LENGTH,
                    (z + zOffset) % ChunkSection.SIDE_LENGTH,
                    ChunkSection.SIDE_LENGTH
            );
            if (chunkSectionBlocks[chunkSectionIndex] != null) {
                chunkSectionBlocks[chunkSectionIndex].blocks[indexInSection] = block;
            }
        });
        for(ChunkSectionBlocks section : chunkSectionBlocks){ //update chunk controllers
            if (section == null) { //null chunk sections were out of bounds
                continue;
            }
            section.controller.setBlocks(section.sectionIndex, section.blocks);
        }
    }
    
    /**
     * Creates an array of ChunkSectionBlocks objects ordered in Y, Z, X order. Where Y, Z, and X are the chunk sections offset from the
     * smallest chunk section. Any out of bounds chunk sections are null.
     * @param controllers
     * @param smallestChunk
     * @param largestChunk
     * @param xChunkLen
     * @param yChunkLen
     * @param zChunkLen
     * @return
     * @throws MalformedNbtTagException 
     */
    private static ChunkSectionBlocks[] getChunkSectionBlocks(
            Collection<ChunkController> controllers,
            ChunkSectionCoord smallestChunk,
            ChunkSectionCoord largestChunk,
            int xChunkLen,
            int yChunkLen,
            int zChunkLen
    ) throws MalformedNbtTagException {
        ChunkSectionBlocks[] chunkSectionBlocks = new ChunkSectionBlocks[xChunkLen * yChunkLen * zChunkLen];//y, z, x order
        for(ChunkController chunkController : controllers){
            ChunkCoord chunkCoord = chunkController.getChunkCoord();
            int chunkX = chunkCoord.x - smallestChunk.x;
            int chunkZ = chunkCoord.z - smallestChunk.z;
            int sectionStart = Math.max(Math.min(smallestChunk.y, chunkController.chunkHeightInSections() - 1), 0); //inclusive
            int sectionEnd = Math.max(Math.min(largestChunk.y + 1, chunkController.chunkHeightInSections()), 0); //exclusive
            for(int sectionIndex = sectionStart; sectionIndex < sectionEnd ; sectionIndex++){
                int chunkY = sectionIndex - smallestChunk.y;
                chunkSectionBlocks[GeneralUtils.getIndexYZX(chunkX, chunkY, chunkZ, xChunkLen, zChunkLen)] = 
                    new ChunkSectionBlocks(chunkController, chunkController.getBlocks(sectionIndex), sectionIndex);
            }
        }
        return chunkSectionBlocks;
    }
    
    private static class ChunkSectionBlocks{
        public final int sectionIndex;
        public final short[] blocks;
        public final ChunkController controller;
        
        public ChunkSectionBlocks(ChunkController controller, short[] blocks, int sectionIndex){
            this.blocks = blocks;
            this.controller = controller;
            this.sectionIndex = sectionIndex;
        }
        
    }
    
    public static void writeBlockEntitiesIntoChunks(Collection<ChunkController> controllers, Collection<BlockEntity> blockEntities) throws MalformedNbtTagException{
        Map<ChunkCoord, ChunkController> controllerMap = new HashMap<>();
        for(ChunkController controller : controllers){
            controllerMap.put(controller.getChunkCoord(), controller);
        }
        writeBlockEntitiesIntoChunks(controllerMap, blockEntities);
    }
    
    public static void writeBlockEntitiesIntoChunks(Map<ChunkCoord, ChunkController> controllers, Collection<BlockEntity> blockEntities) throws MalformedNbtTagException{
        for(BlockEntity blockEntity : blockEntities){
            BlockCoord coord = blockEntity.getBlockCoord();
            ChunkController controller = controllers.get(coord.getChunkCoord());
            controller.addBlockEntity(blockEntity);
        }
    }
    
    public static void writeEntitiesIntoChunks(Collection<ChunkController> controllers, Collection<Entity> entities) throws MalformedNbtTagException{
        Map<ChunkCoord, ChunkController> controllerMap = getChunkControllerMap(controllers);
        writeEntitiesIntoChunks(controllerMap, entities);
    }
    
    public static void writeEntitiesIntoChunks(Map<ChunkCoord, ChunkController> controllers, Collection<Entity> entities) throws MalformedNbtTagException{
        for(Entity entity : entities){
            BlockCoord coord = entity.getCoord().getBlockCoord();
            ChunkController controller = controllers.get(coord.getChunkCoord());
            controller.addEntity(entity);
        }
    }
    
    private static Map<ChunkCoord, ChunkController> getChunkControllerMap(Collection<ChunkController> controllers) throws MalformedNbtTagException{
        Map<ChunkCoord, ChunkController> controllerMap = new HashMap<>();
        for(ChunkController controller : controllers){
            controllerMap.put(controller.getChunkCoord(), controller);
        }
        return controllerMap;
    }
    
    public static short[] readBlocksFromChunks(Collection<ChunkController> controllers, WorldVolume readVolume) throws MalformedNbtTagException{
        BlockCoord smallestPoint = readVolume.getSmallestPoint();
        Box enclosingBox = readVolume.getEnclosingBox();
        BlockCoord largestPoint = new BlockCoord(
                smallestPoint.x + enclosingBox.getXLength(),
                smallestPoint.y + enclosingBox.getYLength(),
                smallestPoint.z + enclosingBox.getZLength()
        );
        ChunkSectionCoord largestChunk = largestPoint.getChunkSectionCoord();
        ChunkSectionCoord smallestChunk = smallestPoint.getChunkSectionCoord();
        int xLengthInChunks = largestChunk.x - smallestChunk.x + 1; //inclusive
        int yLengthInChunks = largestChunk.y - smallestChunk.y + 1;
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
            if (chunkSectionBlocks[chunkSectionIndex] != null) {
                blocks[blocksIndex] = chunkSectionBlocks[chunkSectionIndex].blocks[indexInSection];
            }
        });
        return blocks;
    }
    
    public static short[] readBlocksFromChunks(Map<ChunkCoord, ChunkController> controllers, WorldVolume readVolume) throws MalformedNbtTagException{
        return readBlocksFromChunks(controllers.values(), readVolume);
    }
    
    public static Collection<Entity> readEntitiesFromChunks(Collection<ChunkController> controllers, WorldVolume readVolume) throws MalformedNbtTagException{
        List<Entity> entities = new ArrayList<>();
        for(ChunkController controller : controllers){
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
    
    public static Collection<Entity> readEntitiesFromChunks(Map<ChunkCoord, ChunkController> controllers, WorldVolume readVolume) throws MalformedNbtTagException{
        return readEntitiesFromChunks(controllers.values(), readVolume);
    }
    
    public static Collection<BlockEntity> readBlockEntitiesFromChunks(Collection<ChunkController> controllers, WorldVolume readVolume) throws MalformedNbtTagException{
        List<BlockEntity> blockEntities = new ArrayList<>();
        for(ChunkController controller : controllers){
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
    
    public static Collection<BlockEntity> readBlockEntitiesFromChunks(Map<ChunkCoord, ChunkController> controllers, WorldVolume readVolume) throws MalformedNbtTagException{
        return readBlockEntitiesFromChunks(controllers.values(), readVolume);
    }
    
}
