
package com.atomicedit.backend.utils;

import com.atomicedit.backend.BlockCoord;
import com.atomicedit.backend.blockentity.BlockEntity;
import com.atomicedit.backend.ChunkSectionCoord;
import com.atomicedit.backend.blockprovider.BlockProvider;
import com.atomicedit.backend.chunk.ChunkController;
import com.atomicedit.backend.chunk.ChunkCoord;
import com.atomicedit.backend.chunk.ChunkSection;
import com.atomicedit.backend.entity.Entity;
import com.atomicedit.backend.nbt.MalformedNbtTagException;
import com.atomicedit.volumes.Box;
import com.atomicedit.volumes.Volume;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class CopyUtils {
    
    
    /**
     * Copy the given BlockProvider into the given chunks. Will likely throw a null pointer if the blockProvider
     * has blocks in a chunk not in chunkControllers.
     * @param chunkControllers all the chunks that are contained in the block provider's volume
     * @param blockProvider the source of the blocks to write into the chunks
     * @param smallestPoint
     * @throws Exception 
     */
    public static void writeIntoChunks(Collection<ChunkController> chunkControllers, BlockProvider blockProvider, BlockCoord smallestPoint) throws Exception{
        Volume volume = blockProvider.getVolume();
        Box enclosingBox = volume.getEnclosingBox();
        BlockCoord largestPoint = new BlockCoord(smallestPoint.x + enclosingBox.getXLength(), smallestPoint.y + enclosingBox.getYLength(), smallestPoint.z + enclosingBox.getZLength());
        ChunkSectionCoord largestChunk = largestPoint.getChunkSectionCoord();
        ChunkSectionCoord smallestChunk = smallestPoint.getChunkSectionCoord();
        
        int xLengthInChunks = largestChunk.x - smallestChunk.x + 1; //inclusive
        int yLengthInChunks = largestPoint.getSubChunkIndex() - smallestPoint.getSubChunkIndex() + 1;
        int zLengthInChunks = largestChunk.z - smallestChunk.z + 1; //inclusive
        ChunkSectionBlocks[] chunkSectionBlocks = getChunkSectionBlocks(chunkControllers, smallestChunk, largestChunk, xLengthInChunks, yLengthInChunks, zLengthInChunks);
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
            section.controller.setBlocks(section.sectionIndex, section.blocks);
        }
    }
    
    /**
     * Creates an array of ChunkSectionBlocks objects ordered in Y, Z, X order. Where Y, Z, and X are the chunk sections offset from the
     * smallest chunk section.
     * @param controllers
     * @param smallestChunk
     * @param largestChunk
     * @param xChunkLen
     * @param yChunkLen
     * @param zChunkLen
     * @return
     * @throws MalformedNbtTagException 
     */
    private static ChunkSectionBlocks[] getChunkSectionBlocks(Collection<ChunkController> controllers,
                                                             ChunkSectionCoord smallestChunk,
                                                             ChunkSectionCoord largestChunk,
                                                             int xChunkLen,
                                                             int yChunkLen,
                                                             int zChunkLen)throws MalformedNbtTagException{
        ChunkSectionBlocks[] chunkSectionBlocks = new ChunkSectionBlocks[xChunkLen * yChunkLen * zChunkLen];//y, z, x order
        for(ChunkController chunkController : controllers){
            ChunkCoord chunkCoord = chunkController.getChunkCoord();
            int chunkX = chunkCoord.x - smallestChunk.x;
            int chunkZ = chunkCoord.z - smallestChunk.z;
            for(int sectionIndex = smallestChunk.y; sectionIndex <= largestChunk.y; sectionIndex++){
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
    
    public static void writeBlockEntitiesIntoChunks(Collection<ChunkController> controllers, Collection<BlockEntity> blockEntities) throws Exception{
        Map<ChunkCoord, ChunkController> controllerMap = new HashMap<>();
        for(ChunkController controller : controllers){
            controllerMap.put(controller.getChunkCoord(), controller);
        }
        writeBlockEntitiesIntoChunks(controllerMap, blockEntities);
    }
    
    public static void writeBlockEntitiesIntoChunks(Map<ChunkCoord, ChunkController> controllers, Collection<BlockEntity> blockEntities) throws Exception{
        for(BlockEntity blockEntity : blockEntities){
            BlockCoord coord = blockEntity.getBlockCoord();
            ChunkController controller = controllers.get(coord.getChunkCoord());
            controller.addBlockEntity(blockEntity);
        }
    }
    
    public static void writeEntitiesIntoChunks(Collection<ChunkController> controllers, Collection<Entity> entities) throws Exception{
        Map<ChunkCoord, ChunkController> controllerMap = new HashMap<>();
        for(ChunkController controller : controllers){
            controllerMap.put(controller.getChunkCoord(), controller);
        }
        writeEntitiesIntoChunks(controllerMap, entities);
    }
    
    public static void writeEntitiesIntoChunks(Map<ChunkCoord, ChunkController> controllers, Collection<Entity> entities) throws Exception{
        for(Entity entity : entities){
            BlockCoord coord = entity.getCoord().getBlockCoord();
            ChunkController controller = controllers.get(coord.getChunkCoord());
            controller.addEntity(entity);
        }
    }
    
}
