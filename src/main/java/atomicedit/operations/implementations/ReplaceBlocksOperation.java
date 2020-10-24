
package atomicedit.operations.implementations;

import atomicedit.backend.BlockState;
import atomicedit.backend.World;
import atomicedit.backend.schematic.Schematic;
import atomicedit.backend.blockprovider.BlockProvider;
import atomicedit.backend.blockprovider.ReplaceBlockProvider;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.utils.ChunkUtils;
import atomicedit.operations.Operation;
import atomicedit.operations.OperationResult;
import atomicedit.backend.parameters.ParameterDescriptor;
import atomicedit.backend.parameters.ParameterType;
import atomicedit.backend.parameters.Parameters;
import atomicedit.volumes.WorldVolume;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This operation sets all the blocks in a volume to a specified block type.
 * @author Justin Bonner
 */
public class ReplaceBlocksOperation extends Operation {
    
    
    private static final ParameterDescriptor TO_FIND_BLOCKTYPE_DESC = new ParameterDescriptor("Replace From", ParameterType.BLOCK_SELECTOR, BlockState.AIR);
    private static final ParameterDescriptor TO_SET_BLOCKTYPE_DESC = new ParameterDescriptor("Replace To", ParameterType.BLOCK_SELECTOR, BlockState.AIR);
    public static final List<ParameterDescriptor> PARAM_DESCRIPTORS = Collections.unmodifiableList(Arrays.asList(new ParameterDescriptor[]{
            TO_FIND_BLOCKTYPE_DESC,
            TO_SET_BLOCKTYPE_DESC
        }
    ));
    
    private final WorldVolume operationVolume; //volume operated on
    private Schematic initialSchematic; //backup for undos and operation calculations
    private final BlockState fromBlockState;
    private final BlockState toBlockState;

    public ReplaceBlocksOperation(WorldVolume volume, Parameters parameters){
        this.operationVolume = volume;
        this.fromBlockState = parameters.getParamAsBlockState(TO_FIND_BLOCKTYPE_DESC);
        this.toBlockState = parameters.getParamAsBlockState(TO_SET_BLOCKTYPE_DESC);
        if(this.fromBlockState == null || this.toBlockState == null){
            throw new RuntimeException("Cannot do a replace blocks operation without a from and to choice.");
        }
    }
    
    public static ReplaceBlocksOperation getInstance(WorldVolume volume, Parameters parameters) {
        return new ReplaceBlocksOperation(volume, parameters);
    }
    
    @Override
    protected OperationResult doOperation(World world) throws Exception{
        Collection<ChunkController> chunkControllers = world.getLoadedChunkStage().getMutableChunks(getChunkCoordsInOperation()).values();
        this.initialSchematic = Schematic.createSchematicFromWorld(world, operationVolume);
        setBlocks(chunkControllers);
        return new OperationResult(true);
    }
    
    @Override
    protected OperationResult undoOperation(World world) throws Exception{
        Schematic.putSchematicIntoWorld(world, initialSchematic, operationVolume.getSmallestPoint());
        return new OperationResult(true);
    }
    
    @Override
    public WorldVolume getWorldVolume(){
        return this.operationVolume;
    }
    
    private void setBlocks(Collection<ChunkController> chunkControllers) throws Exception{
        BlockProvider replace = new ReplaceBlockProvider(operationVolume, initialSchematic, fromBlockState, toBlockState);
        ChunkUtils.writeBlocksIntoChunks(chunkControllers, replace, operationVolume.getSmallestPoint());
    }
    
}
