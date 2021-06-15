
package atomicedit.operations.implementations;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.BlockState;
import atomicedit.backend.GlobalBlockStateMap;
import atomicedit.backend.World;
import atomicedit.backend.schematic.Schematic;
import atomicedit.backend.blockprovider.BlockProvider;
import atomicedit.backend.blockprovider.FillBlockProvider;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.parameters.BlockStateParameterDescriptor;
import atomicedit.backend.parameters.BooleanParameterDescriptor;
import atomicedit.backend.utils.ChunkUtils;
import atomicedit.operations.Operation;
import atomicedit.operations.OperationResult;
import atomicedit.backend.parameters.ParameterDescriptor;
import atomicedit.backend.parameters.Parameters;
import atomicedit.backend.utils.BitArray;
import atomicedit.backend.utils.GeneralUtils;
import atomicedit.logging.Logger;
import atomicedit.volumes.WorldVolume;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * This operation sets all the blocks in a volume to a specified block type.
 * @author Justin Bonner
 */
public class FloodFillOperation extends Operation {
    
    private static final BlockStateParameterDescriptor TARGET_BLOCKTYPE_DESC = new BlockStateParameterDescriptor("Target Block Type", BlockState.AIR);
    private static final BlockStateParameterDescriptor REPLACE_BLOCKTYPE_DESC = new BlockStateParameterDescriptor("Replacing Block Type", BlockState.AIR);
    private static final BooleanParameterDescriptor VERTICAL_FLOOD_DESC = new BooleanParameterDescriptor("Vertical Flooding", false);
    public static final List<ParameterDescriptor> PARAM_DESCRIPTORS = Collections.unmodifiableList(Arrays.asList(new ParameterDescriptor[] {
            TARGET_BLOCKTYPE_DESC,
            REPLACE_BLOCKTYPE_DESC,
            VERTICAL_FLOOD_DESC
        }
    ));
    
    private final WorldVolume operationVolume; //volume operated on
    private Schematic schematicBackup; //backup for undos
    private final BlockState targetBlockState; //block type to flood
    private final BlockState replaceBlockState; //block type to flood over
    private final boolean verticalFlood; //allow flood to propagate up
    
    public FloodFillOperation(WorldVolume volume, Parameters parameters){
        this.operationVolume = volume;
        this.targetBlockState = parameters.getParamAsBlockState(TARGET_BLOCKTYPE_DESC);
        if (this.targetBlockState == null) {
            throw new RuntimeException("Cannot do a flood fill operation with no target block.");
        }
        this.replaceBlockState = parameters.getParamAsBlockState(REPLACE_BLOCKTYPE_DESC);
        if (this.replaceBlockState == null) {
            throw new RuntimeException("Cannot do a flood fill operation with no replacement block.");
        }
        this.verticalFlood = parameters.getParamAsBoolean(VERTICAL_FLOOD_DESC);
    }
    
    public static FloodFillOperation getInstance(WorldVolume volume, Parameters parameters) {
        return new FloodFillOperation(volume, parameters);
    }
    
    @Override
    protected OperationResult doOperation(World world) throws Exception {
        Collection<ChunkController> chunkControllers = world.getLoadedChunkStage(operationDimension).getMutableChunks(getChunkCoordsInOperation()).values();
        this.schematicBackup = Schematic.createSchematicFromWorld(world, operationDimension, operationVolume);
        doFlood(chunkControllers);
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
    
    private void doFlood(Collection<ChunkController> chunkControllers) throws Exception {
        if (targetBlockState.equals(replaceBlockState)) {
            return; //this case should do nothing and actually running this would run out of memory
        }
        Stack<BlockCoord> floodStack = new Stack<>();
        int targetBlockId = GlobalBlockStateMap.getBlockId(targetBlockState);
        int replaceBlockId = GlobalBlockStateMap.getBlockId(replaceBlockState);
        int[] blocks = schematicBackup.getBlocks();
        BitArray floodMask = new BitArray(operationVolume.getIncludedSet().size(), false);
        schematicBackup.volume.doForXyz((x, y, z, blockIndex) -> {
            int blockIdAt = blocks[blockIndex];
            if (blockIdAt == targetBlockId) {
                floodStack.push(new BlockCoord(x, y, z));
            }
        });
        
        //iterate flood stack until empty adding flooded blocks to flood mask
        final int xLen = schematicBackup.volume.getEnclosingBox().getXLength();
        final int zLen = schematicBackup.volume.getEnclosingBox().getZLength();
        while (!floodStack.isEmpty()) {
            BlockCoord blockCoord = floodStack.pop();
            //Logger.info("Popped block coord: " + blockCoord);
            int x = blockCoord.x;
            int y = blockCoord.y;
            int z = blockCoord.z;
            //if any adj block is replace block then add that x,y,z to flood stack
            int xPlusIndex = GeneralUtils.getIndexYZX(x + 1, y, z, xLen, zLen);
            if (schematicBackup.getBlockAt(x + 1, y, z) == replaceBlockId && !floodMask.get(xPlusIndex)) {
                floodStack.push(new BlockCoord(x + 1, y, z));
                floodMask.set(xPlusIndex, true);
            }
            int xMinusIndex = GeneralUtils.getIndexYZX(x - 1, y, z, xLen, zLen);
            if (schematicBackup.getBlockAt(x - 1, y, z) == replaceBlockId && !floodMask.get(xMinusIndex)) {
                floodStack.push(new BlockCoord(x - 1, y, z));
                floodMask.set(xMinusIndex, true);
            }
            int yPlusIndex = GeneralUtils.getIndexYZX(x, y + 1, z, xLen, zLen);
            if (verticalFlood && schematicBackup.getBlockAt(x, y + 1, z) == replaceBlockId && !floodMask.get(yPlusIndex)) {
                floodStack.push(new BlockCoord(x, y + 1, z));
                floodMask.set(yPlusIndex, true);
            }
            int yMinusIndex = GeneralUtils.getIndexYZX(x, y - 1, z, xLen, zLen);
            if (schematicBackup.getBlockAt(x, y - 1, z) == replaceBlockId && !floodMask.get(yMinusIndex)) {
                floodStack.push(new BlockCoord(x, y - 1, z));
                floodMask.set(yMinusIndex, true);
            }
            int zPlusIndex = GeneralUtils.getIndexYZX(x, y, z + 1, xLen, zLen);
            if (schematicBackup.getBlockAt(x, y, z + 1) == replaceBlockId && !floodMask.get(zPlusIndex)) {
                floodStack.push(new BlockCoord(x, y, z + 1));
                floodMask.set(zPlusIndex, true);
            }
            int zMinusIndex = GeneralUtils.getIndexYZX(x, y, z - 1, xLen, zLen);
            if (schematicBackup.getBlockAt(x, y, z - 1) == replaceBlockId && !floodMask.get(zMinusIndex)) {
                floodStack.push(new BlockCoord(x, y, z - 1));
                floodMask.set(zMinusIndex, true);
            }
        }
        WorldVolume floodVolume = new WorldVolume(this.operationVolume);
        floodVolume.getIncludedSet().and(floodMask);
        BlockProvider fill = new FillBlockProvider(floodVolume, targetBlockState);
        ChunkUtils.writeBlocksIntoChunks(chunkControllers, fill, operationVolume.getSmallestPoint());
    }
    
}
