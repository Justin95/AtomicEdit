
package atomicedit.operations.implementations;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.BlockState;
import atomicedit.backend.World;
import atomicedit.backend.schematic.Schematic;
import atomicedit.backend.blockprovider.BlockProvider;
import atomicedit.backend.blockprovider.FillBlockProvider;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.utils.CopyUtils;
import atomicedit.operations.Operation;
import atomicedit.operations.OperationResult;
import atomicedit.operations.OperationType;
import atomicedit.operations.utils.OperationParameterDescriptor;
import atomicedit.operations.utils.OperationParameterType;
import atomicedit.operations.utils.OperationParameters;
import atomicedit.volumes.Volume;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * This operation sets all the blocks in a volume to a specified block type.
 * @author Justin Bonner
 */
public class SetBlocksOperationType implements OperationType {
    
    private static final SetBlocksOperationType INSTANCE = new SetBlocksOperationType();
    
    private SetBlocksOperationType(){}
    
    public static SetBlocksOperationType getInstance(){
        return INSTANCE;
    }
    
    private static final String OP_NAME = "Set Blocks Operation";
    private static final List<OperationParameterDescriptor> PARAM_DESCRIPTORS;
    private static final OperationParameterDescriptor TO_SET_BLOCKTYPE_DESC = new OperationParameterDescriptor("Block Type", OperationParameterType.BLOCK_SELECTOR);
    static{
        OperationParameterDescriptor[] descriptors = new OperationParameterDescriptor[]{
            TO_SET_BLOCKTYPE_DESC
        };
        PARAM_DESCRIPTORS = Arrays.asList(descriptors);
    }
    
    @Override
    public List<OperationParameterDescriptor> getOperationParameterDescription() {
        return PARAM_DESCRIPTORS;
    }
    
    @Override
    public SetBlocksOperation getOperationInstance(Volume volume, BlockCoord smallestCoord, OperationParameters parameters){
        return new SetBlocksOperation(volume, smallestCoord, parameters);
    }
    
    @Override
    public String getOperationName(){
        return OP_NAME;
    }
    
    @Override
    public String toString(){
        return getOperationName();
    }
    
    public class SetBlocksOperation extends Operation {
        
        private final Volume operationVolume; //volume operated on
        private Schematic schematicBackup; //backup for undos
        private final BlockState blockState; //block type to fill
        private final BlockCoord smallestCoord; //the smallest world space block coord in the volume, used to give the volume param a fixed position

        public SetBlocksOperation(Volume volume, BlockCoord smallestCoord, OperationParameters parameters){
            this.operationVolume = volume;
            this.blockState = parameters.getParamAsBlockState(TO_SET_BLOCKTYPE_DESC);
            this.smallestCoord = smallestCoord;
            if(this.blockState == null){
                throw new RuntimeException("Cannot do a set blocks operation with no block to set to");
            }
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
        
        @Override
        public Volume getVolume(){
            return this.operationVolume;
        }
        
        private void setBlocks(Collection<ChunkController> chunkControllers) throws Exception{
            BlockProvider fill = new FillBlockProvider(operationVolume, blockState);
            CopyUtils.writeIntoChunks(chunkControllers, fill, smallestCoord);
        }
        
    }
}
