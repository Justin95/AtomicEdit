
package atomicedit.operations.implementations;

import atomicedit.backend.BlockState;
import atomicedit.backend.World;
import atomicedit.backend.schematic.Schematic;
import atomicedit.backend.blockprovider.BlockProvider;
import atomicedit.backend.blockprovider.ErosionBlockProvider;
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
public class ErosionOperation extends Operation {
    
    private static final ParameterDescriptor EROSION_BLOCKTYPE_DESC = new ParameterDescriptor("Erode Next To", ParameterType.BLOCK_SELECTOR, BlockState.AIR);
    private static final ParameterDescriptor ERODE_TO_BLOCKTYPE_DESC = new ParameterDescriptor("Erode To", ParameterType.BLOCK_SELECTOR, BlockState.AIR);
    private static final ParameterDescriptor ZERO_ADJ_CHANCE_DESC = new ParameterDescriptor("Zero Faces Chance", ParameterType.INT, 0);
    private static final ParameterDescriptor ONE_ADJ_CHANCE_DESC = new ParameterDescriptor("One Face Chance", ParameterType.INT, 2);
    private static final ParameterDescriptor TWO_ADJ_CHANCE_DESC = new ParameterDescriptor("Two Faces Chance", ParameterType.INT, 4);
    private static final ParameterDescriptor THREE_ADJ_CHANCE_DESC = new ParameterDescriptor("Three Faces Chance", ParameterType.INT, 8);
    private static final ParameterDescriptor FOUR_ADJ_CHANCE_DESC = new ParameterDescriptor("Four Faces Chance", ParameterType.INT, 16);
    private static final ParameterDescriptor FIVE_ADJ_CHANCE_DESC = new ParameterDescriptor("Five Faces Chance", ParameterType.INT, 32);
    private static final ParameterDescriptor SIX_ADJ_CHANCE_DESC = new ParameterDescriptor("Six Faces Chance", ParameterType.INT, 64);
    public static final List<ParameterDescriptor> PARAM_DESCRIPTORS = Collections.unmodifiableList(Arrays.asList(new ParameterDescriptor[]{
            EROSION_BLOCKTYPE_DESC,
            ERODE_TO_BLOCKTYPE_DESC,
            ZERO_ADJ_CHANCE_DESC,
            ONE_ADJ_CHANCE_DESC,
            TWO_ADJ_CHANCE_DESC,
            THREE_ADJ_CHANCE_DESC,
            FOUR_ADJ_CHANCE_DESC,
            FIVE_ADJ_CHANCE_DESC,
            SIX_ADJ_CHANCE_DESC
        }
    ));
    
    private final WorldVolume operationVolume; //volume operated on
    private Schematic schematicBackup; //backup for undos
    private final BlockState erosionNextTo;
    private final BlockState erodeTo;
    private final int[] erosionChances;
    
    public ErosionOperation(WorldVolume volume, Parameters parameters){
        this.operationVolume = volume;
        this.erosionNextTo = parameters.getParamAsBlockState(EROSION_BLOCKTYPE_DESC);
        this.erodeTo = parameters.getParamAsBlockState(ERODE_TO_BLOCKTYPE_DESC);
        this.erosionChances = new int[]{
            parameters.getParamAsInteger(ZERO_ADJ_CHANCE_DESC),
            parameters.getParamAsInteger(ONE_ADJ_CHANCE_DESC),
            parameters.getParamAsInteger(TWO_ADJ_CHANCE_DESC),
            parameters.getParamAsInteger(THREE_ADJ_CHANCE_DESC),
            parameters.getParamAsInteger(FOUR_ADJ_CHANCE_DESC),
            parameters.getParamAsInteger(FIVE_ADJ_CHANCE_DESC),
            parameters.getParamAsInteger(SIX_ADJ_CHANCE_DESC),
        };
    }
    
    public static ErosionOperation getInstance(WorldVolume volume, Parameters parameters) {
        return new ErosionOperation(volume, parameters);
    }
    
    @Override
    protected OperationResult doOperation(World world) throws Exception {
        Collection<ChunkController> chunkControllers = world.getLoadedChunkStage(operationDimension).getMutableChunks(getChunkCoordsInOperation()).values();
        this.schematicBackup = Schematic.createSchematicFromWorld(world, operationDimension, operationVolume);
        setBlocks(chunkControllers);
        return new OperationResult(true);
    }
    
    @Override
    protected OperationResult undoOperation(World world) throws Exception {
        Schematic.putSchematicIntoWorld(world, operationDimension, schematicBackup, operationVolume.getSmallestPoint());
        return new OperationResult(true);
    }
    
    @Override
    public WorldVolume getWorldVolume(){
        return this.operationVolume;
    }
    
    private void setBlocks(Collection<ChunkController> chunkControllers) throws Exception {
        BlockProvider fill = new ErosionBlockProvider(operationVolume, schematicBackup, this.erosionNextTo, this.erodeTo, this.erosionChances);
        ChunkUtils.writeBlocksIntoChunks(chunkControllers, fill, operationVolume.getSmallestPoint());
    }
    
}
