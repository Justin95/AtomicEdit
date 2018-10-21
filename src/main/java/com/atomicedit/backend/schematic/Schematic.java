
package com.atomicedit.backend.schematic;

import com.atomicedit.backend.BlockCoord;
import com.atomicedit.backend.BlockEntity.BlockEntity;
import com.atomicedit.backend.World;
import com.atomicedit.backend.blockprovider.BlockProvider;
import com.atomicedit.backend.blockprovider.SchematicBlockProvider;
import com.atomicedit.backend.chunk.ChunkController;
import com.atomicedit.backend.chunk.ChunkCoord;
import com.atomicedit.backend.entity.Entity;
import com.atomicedit.backend.nbt.NbtCompoundTag;
import com.atomicedit.backend.utils.CopyUtils;
import com.atomicedit.operations.OperationResult;
import com.atomicedit.volumes.Volume;
import java.util.List;
import java.util.Map;

/**
 * Store an arbitrarily shaped section of a world.
 * Reference: https://minecraft.gamepedia.com/Schematic_file_format
 * @author Justin Bonner
 */
public class Schematic {
    
    public final Volume volume;
    private short[] blocks; //blocks stored in y,z,x order, skipping blocks not in the volume
    private List<Entity> entities;
    private List<BlockEntity> blockEntities;
    
    
    private Schematic(Volume volume, short[] blocks, List<Entity> entities, List<BlockEntity> blockEntities){
        if(volume.getEnclosingBox().getNumBlocksContained() != blocks.length) throw new IllegalArgumentException("Number of blocks and volume size differ");
        this.volume = volume;
        this.blocks = blocks;
        this.entities = entities;
        this.blockEntities = blockEntities;
    }
    
    public static Schematic interpretSchematic(NbtCompoundTag schmaticNbt){
        throw new UnsupportedOperationException(); //need different schematic interpreters + factory
    }
    
    public static NbtCompoundTag writeSchematicToNbt(Schematic schematic){
        throw new UnsupportedOperationException(); //TODO
    }
    
    public static Schematic createSchematicFromWorld(World world, Volume volume, BlockCoord smallestCoord){
        throw new UnsupportedOperationException(); //TODO
    }
    
    /**
     * Put a schematic into the world at a given location.
     * @param world
     * @param schematic
     * @param smallestCoord the smallest block coordinate in the volume, where we want it put into the world.
     * @return
     * @throws Exception 
     */
    public static OperationResult putSchematicIntoWorld(World world, Schematic schematic, BlockCoord smallestCoord) throws Exception{
        BlockProvider provider = new SchematicBlockProvider(schematic);
        Map<ChunkCoord, ChunkController> chunkControllers = world.getMutableChunks(schematic.volume.getContainedChunkCoords(smallestCoord));
        CopyUtils.writeIntoChunks(chunkControllers.values(), provider, smallestCoord);
        CopyUtils.writeBlockEntitiesIntoChunks(chunkControllers, schematic.blockEntities);
        CopyUtils.writeEntitiesIntoChunks(chunkControllers, schematic.entities);
        return new OperationResult(true);
    }
    
    protected Volume getVolume(){
        return this.volume;
    }
    
    public short[] getBlocks(){
        return this.blocks;
    }
    
    public List<Entity> getEntities(){
        return this.entities;
    }
    
    public List<BlockEntity> getBlockEntities(){
        return this.blockEntities;
    }
    
}
