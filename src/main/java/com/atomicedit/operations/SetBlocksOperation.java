
package com.atomicedit.operations;

import com.atomicedit.backend.BlockCoord;
import com.atomicedit.backend.BlockType;
import com.atomicedit.backend.World;
import com.atomicedit.backend.schematic.Schematic;
import com.atomicedit.backend.blockprovider.BlockProvider;
import com.atomicedit.backend.blockprovider.FillBlockProvider;
import com.atomicedit.backend.chunk.ChunkController;
import com.atomicedit.backend.utils.CopyUtils;
import com.atomicedit.volumes.Volume;
import java.util.Collection;

/**
 *
 * @author Justin Bonner
 */
public class SetBlocksOperation extends Operation{
    
    private Volume operationVolume; //volume operated on
    private Schematic schematicBackup; //backup for undos
    private BlockType blockType; //block type to fill
    private BlockCoord smallestCoord;
    
    public SetBlocksOperation(BlockType toFill, Volume volume, BlockCoord smallestCoord){
        this.operationVolume = volume;
        this.blockType = toFill;
        this.smallestCoord = smallestCoord;
    }
    
    @Override
    protected OperationResult doOperation(World world){
        try{
            Collection<ChunkController> chunkControllers = world.getMutableChunks(getChunkCoordsInOperation(smallestCoord)).values();
            this.schematicBackup = Schematic.createSchematicFromWorld(world, operationVolume, smallestCoord);
            setBlocks(chunkControllers);
        }catch(Exception e){
            return new OperationResult(false, e);
        }
        return new OperationResult(true);
    }
    
    @Override
    protected OperationResult undoOperation(World world){
        try{
            Schematic.putSchematicIntoWorld(world, schematicBackup, smallestCoord);
        }catch(Exception e){
            return new OperationResult(false, e);
        }
        return new OperationResult(true);
    }
    
    public Volume getVolume(){
        return this.operationVolume;
    }
    
    private void setBlocks(Collection<ChunkController> chunkControllers) throws Exception{
        BlockProvider fill = new FillBlockProvider(operationVolume, blockType);
        CopyUtils.writeIntoChunks(chunkControllers, fill, smallestCoord);
    }
    
}
