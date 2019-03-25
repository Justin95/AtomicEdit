
package atomicedit.operations.implementations;

import atomicedit.backend.BlockState;
import atomicedit.backend.World;
import atomicedit.backend.schematic.Schematic;
import atomicedit.backend.blockprovider.BlockProvider;
import atomicedit.backend.blockprovider.FillBlockProvider;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.utils.ChunkUtils;
import atomicedit.operations.Operation;
import atomicedit.operations.OperationResult;
import atomicedit.operations.OperationType;
import atomicedit.operations.utils.OperationParameterDescriptor;
import atomicedit.operations.utils.OperationParameterType;
import atomicedit.operations.utils.OperationParameters;
import atomicedit.volumes.WorldVolume;
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
    public SetBlocksOperation getOperationInstance(WorldVolume volume, OperationParameters parameters){
        return new SetBlocksOperation(volume, parameters);
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
        
        private final WorldVolume operationVolume; //volume operated on
        private Schematic schematicBackup; //backup for undos
        private final BlockState blockState; //block type to fill

        public SetBlocksOperation(WorldVolume volume, OperationParameters parameters){
            this.operationVolume = volume;
            this.blockState = parameters.getParamAsBlockState(TO_SET_BLOCKTYPE_DESC);
            if(this.blockState == null){
                throw new RuntimeException("Cannot do a set blocks operation with no block to set to");
            }
        }
        
        @Override
        protected OperationResult doOperation(World world) throws Exception{
            Collection<ChunkController> chunkControllers = world.getLoadedChunkStage().getMutableChunks(getChunkCoordsInOperation()).values();
            this.schematicBackup = Schematic.createSchematicFromWorld(world, operationVolume);
            setBlocks(chunkControllers);
            return new OperationResult(true);
        }
        
        @Override
        protected OperationResult undoOperation(World world) throws Exception{
            Schematic.putSchematicIntoWorld(world, schematicBackup, operationVolume.getSmallestPoint());
            return new OperationResult(true);
        }
        
        @Override
        public WorldVolume getWorldVolume(){
            return this.operationVolume;
        }
        
        private void setBlocks(Collection<ChunkController> chunkControllers) throws Exception{
            BlockProvider fill = new FillBlockProvider(operationVolume, blockState);
            ChunkUtils.writeBlocksIntoChunks(chunkControllers, fill, operationVolume.getSmallestPoint());
        }
        
    }
}
