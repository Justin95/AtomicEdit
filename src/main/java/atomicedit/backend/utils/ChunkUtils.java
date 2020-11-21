
package atomicedit.backend.utils;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.ChunkSectionCoord;
import atomicedit.backend.blockprovider.BlockProvider;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.ChunkReader;
import atomicedit.backend.chunk.ChunkSection;
import atomicedit.backend.entity.Entity;
import atomicedit.backend.entity.EntityCoord;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.logging.Logger;
import atomicedit.volumes.Box;
import atomicedit.volumes.Volume;
import atomicedit.volumes.WorldVolume;
import java.util.ArrayList;
import java.util.Arrays;
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
    public static void writeBlocksIntoChunks(Collection<ChunkController> chunkControllers, BlockProvider blockProvider, BlockCoord smallestPoint) throws MalformedNbtTagException {
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
        //in some cases (seemingly when a volume borders a chunk section border) this array is created too large, idk why, this doesn't cause any issues though
        ChunkSectionBlocks[] chunkSectionBlocks = new ChunkSectionBlocks[xChunkLen * yChunkLen * zChunkLen];//y, z, x order
        for(ChunkController chunkController : controllers){
            ChunkCoord chunkCoord = chunkController.getChunkCoord();
            if (chunkCoord.x < smallestChunk.x || chunkCoord.x > largestChunk.x || chunkCoord.z < smallestChunk.z || chunkCoord.z > largestChunk.z) {
                Logger.warning("Extra chunk controller aquired for operation: " + chunkCoord);
                continue;
            }
            int chunkX = chunkCoord.x - smallestChunk.x;
            int chunkZ = chunkCoord.z - smallestChunk.z;
            int sectionStart = Math.max(smallestChunk.y, 0); //inclusive
            int sectionEnd = Math.min(largestChunk.y + 1, chunkController.chunkHeightInSections()); //exclusive
            for(int sectionIndex = sectionStart; sectionIndex < sectionEnd ; sectionIndex++){
                int chunkY = sectionIndex - smallestChunk.y;
                chunkSectionBlocks[GeneralUtils.getIndexYZX(chunkX, chunkY, chunkZ, xChunkLen, zChunkLen)] = 
                    new ChunkSectionBlocks(chunkController, chunkController.getBlocks(sectionIndex), sectionIndex);
            }
        }
        return chunkSectionBlocks;
    }
    
    private static class ChunkSectionBlocks {
        public final int sectionIndex;
        public final short[] blocks;
        public final ChunkController controller;
        
        public ChunkSectionBlocks(ChunkController controller, short[] blocks, int sectionIndex){
            this.blocks = blocks;
            this.controller = controller;
            this.sectionIndex = sectionIndex;
        }
        
        @Override
        public String toString() {
            try {
                return "{x=" + controller.getChunkCoord().x + ", y=" + sectionIndex + ", z=" + controller.getChunkCoord().z + "}";
            } catch (Exception e) {
                return "Exception in ChunkSectionBlocks.toString()";
            }
        }
        
    }
    
    public static void writeBlockEntitiesIntoChunks(Collection<ChunkController> controllers, Collection<BlockEntity> blockEntities) throws MalformedNbtTagException{
        writeBlockEntitiesIntoChunks(getChunkControllerMap(controllers), blockEntities);
    }
    
    public static void writeBlockEntitiesIntoChunks(Map<ChunkCoord, ChunkController> controllers, Collection<BlockEntity> blockEntities) throws MalformedNbtTagException{
        for(BlockEntity blockEntity : blockEntities){
            if (blockEntity == null) {
                continue;
            }
            BlockCoord coord = blockEntity.getBlockCoord();
            ChunkController controller = controllers.get(coord.getChunkCoord());
            if (controller == null) {
                Logger.warning("Unable to write block entity to chunk because it is not in the volume: " + blockEntity);
                continue;
            }
            //incase an identical block entity is present it must be removed to avoid duplicating block entitites
            controller.removeBlockEntity(blockEntity);
            controller.addBlockEntity(blockEntity);
        }
    }
    
    public static void writeEntitiesIntoChunks(Collection<ChunkController> controllers, Collection<Entity> entities) throws MalformedNbtTagException{
        writeEntitiesIntoChunks(getChunkControllerMap(controllers), entities);
    }
    
    public static void writeEntitiesIntoChunks(Map<ChunkCoord, ChunkController> controllers, Collection<Entity> entities) throws MalformedNbtTagException{
        for(Entity entity : entities) {
            if (entity == null) {
                continue;
            }
            BlockCoord coord = entity.getCoord().getBlockCoord();
            ChunkController controller = controllers.get(coord.getChunkCoord());
            if (controller == null) {
                Logger.warning("Unable to write entity to chunk because it is not in the volume: " + entity);
                continue;
            }
            //incase an identical entity is present it must be removed to avoid duplicating entities
            controller.removeEntity(entity);
            controller.addEntity(entity);
        }
    }
    
    public static void removeBlockEntitiesFromChunks(Collection<ChunkController> controllers, Collection<BlockEntity> blockEntities) throws MalformedNbtTagException {
        removeBlockEntitiesFromChunks(getChunkControllerMap(controllers), blockEntities);
    }
    
    public static void removeBlockEntitiesFromChunks(Map<ChunkCoord, ChunkController> controllers, Collection<BlockEntity> blockEntities) throws MalformedNbtTagException {
        for(BlockEntity blockEntity : blockEntities){
            if (blockEntity == null) {
                continue;
            }
            BlockCoord coord = blockEntity.getBlockCoord();
            ChunkController controller = controllers.get(coord.getChunkCoord());
            if (controller == null) {
                Logger.warning("Unable to remove block entity from chunk because it is not in the volume: " + blockEntity);
                continue;
            }
            controller.removeBlockEntity(blockEntity);
        }
    }
    
    public static void removeEntitiesFromChunks(Collection<ChunkController> controllers, Collection<Entity> entities) throws MalformedNbtTagException {
        removeEntitiesFromChunks(getChunkControllerMap(controllers), entities);
    }
    
    public static void removeEntitiesFromChunks(Map<ChunkCoord, ChunkController> controllers, Collection<Entity> entities) throws MalformedNbtTagException {
        for(Entity entity : entities){
            if (entity == null) {
                continue;
            }
            BlockCoord coord = entity.getCoord().getBlockCoord();
            ChunkController controller = controllers.get(coord.getChunkCoord());
            if (controller == null) {
                Logger.warning("Unable to remove entity from chunk because it is not in the volume: " + entity);
                continue;
            }
            controller.removeEntity(entity);
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
    
    public static List<Entity> readEntitiesFromChunkReaders(Collection<ChunkReader> controllers, WorldVolume readVolume) throws MalformedNbtTagException{
        List<Entity> entities = new ArrayList<>();
        for(ChunkReader controller : controllers){
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
    
    public static List<Entity> readEntitiesFromChunkControllers(Collection<ChunkController> chunkReaders, WorldVolume readVolume) throws MalformedNbtTagException{
        return readEntitiesFromChunkReaders((Collection<ChunkReader>)(Collection)chunkReaders, readVolume);
    }
    
    public static List<BlockEntity> readBlockEntitiesFromChunkReaders(Collection<ChunkReader> chunkReaders, WorldVolume readVolume) throws MalformedNbtTagException{
        List<BlockEntity> blockEntities = new ArrayList<>();
        for(ChunkReader chunkReader : chunkReaders){
            for(BlockEntity blockEntity : chunkReader.getBlockEntities()){
                BlockCoord coord = blockEntity.getBlockCoord();
                if(readVolume.containsCoord((int)coord.x, (int)coord.y, (int)coord.z)){
                    //could add a 'block entity filter' test that can be passed in as an optional param
                    blockEntities.add(blockEntity);
                }
            }
        }
        return blockEntities;
    }
    
    public static List<BlockEntity> readBlockEntitiesFromChunkControllers(Collection<ChunkController> chunkReaders, WorldVolume readVolume) throws MalformedNbtTagException{
        return readBlockEntitiesFromChunkReaders((Collection<ChunkReader>)(Collection)chunkReaders, readVolume);
    }
    
}
