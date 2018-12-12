
package atomicedit.operations;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.BlockState;
import atomicedit.backend.World;
import atomicedit.backend.schematic.Schematic;
import atomicedit.backend.blockprovider.BlockProvider;
import atomicedit.backend.blockprovider.FillBlockProvider;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.utils.CopyUtils;
import atomicedit.volumes.Volume;
import java.util.Collection;

/**
 *
 * @author Justin Bonner
 */
public class SetBlocksOperation extends Operation{
    
    private Volume operationVolume; //volume operated on
    private Schematic schematicBackup; //backup for undos
    private BlockState blockType; //block type to fill
    private BlockCoord smallestCoord;
    
    public SetBlocksOperation(BlockState toFill, Volume volume, BlockCoord smallestCoord){
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
