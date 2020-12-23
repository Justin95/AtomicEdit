
package atomicedit.operations.implementations;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.BlockState;
import atomicedit.backend.World;
import atomicedit.backend.blockprovider.BlockProvider;
import atomicedit.backend.blockprovider.FillBlockProvider;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.parameters.BlockStateParameterDescriptor;
import atomicedit.backend.parameters.FloatParameterDescriptor;
import atomicedit.backend.parameters.IntegerParameterDescriptor;
import atomicedit.backend.parameters.ParameterDescriptor;
import atomicedit.backend.parameters.Parameters;
import atomicedit.backend.schematic.Schematic;
import atomicedit.backend.utils.BitArray;
import atomicedit.backend.utils.ChunkUtils;
import atomicedit.backend.utils.GeneralUtils;
import atomicedit.operations.Operation;
import atomicedit.operations.OperationResult;
import atomicedit.volumes.Box;
import atomicedit.volumes.Volume;
import atomicedit.volumes.WorldVolume;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import org.spongepowered.noise.Noise;
import org.spongepowered.noise.NoiseQuality;

/**
 * This operation assigns each block in the volume its distance from the nearest block not in the
 * volume. Then a user seeded scaled Perlin noise value is calculated for each block in the volume.
 * If the noise value is less than the distance value the block is effected by this operation.
 * The Perlin noise is always the same for the same world coordinates and seed.
 * @author Justin Bonner
 */
public class NoiseOperation extends Operation {
    private static final BlockStateParameterDescriptor TO_SET_BLOCKTYPE_DESC = new BlockStateParameterDescriptor("Block Type", BlockState.AIR);
    private static final IntegerParameterDescriptor NOISE_SEED_DESC = new IntegerParameterDescriptor("Noise Seed", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
    /**
     * Affects the range into the volume that the noise can cause to be ignored.
     */
    private static final FloatParameterDescriptor NOISE_1_AMP_DESC = new FloatParameterDescriptor("Noise 1 Amplifier", 5f, 0f, Float.MAX_VALUE);
    /**
     * Affects the horizontal scaling of the noise. This value cannot be zero.
     */
    private static final FloatParameterDescriptor NOISE_1_HOIZ_DESC = new FloatParameterDescriptor("Noise 1 Strech", 4f, .1f, Float.MAX_VALUE);
    private static final FloatParameterDescriptor NOISE_2_AMP_DESC = new FloatParameterDescriptor("Noise 2 Amplifier", 0f, 0f, Float.MAX_VALUE);
    private static final FloatParameterDescriptor NOISE_2_HOIZ_DESC = new FloatParameterDescriptor("Noise 2 Strech", 3f, .1f, Float.MAX_VALUE);
    private static final FloatParameterDescriptor NOISE_3_AMP_DESC = new FloatParameterDescriptor("Noise 3 Amplifier", 0f, 0f, Float.MAX_VALUE);
    private static final FloatParameterDescriptor NOISE_3_HOIZ_DESC = new FloatParameterDescriptor("Noise 3 Strech", 2f, .1f, Float.MAX_VALUE);
    private static final FloatParameterDescriptor NOISE_4_AMP_DESC = new FloatParameterDescriptor("Noise 4 Amplifier", 0f, 0f, Float.MAX_VALUE);
    private static final FloatParameterDescriptor NOISE_4_HOIZ_DESC = new FloatParameterDescriptor("Noise 4 Strech", 1f, .1f, Float.MAX_VALUE);
    //TODO add horizontal noise scales
    public static final List<ParameterDescriptor> PARAM_DESCRIPTORS = Collections.unmodifiableList(Arrays.asList(new ParameterDescriptor[]{
            TO_SET_BLOCKTYPE_DESC,
            NOISE_SEED_DESC,
            NOISE_1_AMP_DESC,
            NOISE_1_HOIZ_DESC,
            NOISE_2_AMP_DESC,
            NOISE_2_HOIZ_DESC,
            NOISE_3_AMP_DESC,
            NOISE_3_HOIZ_DESC,
            NOISE_4_AMP_DESC,
            NOISE_4_HOIZ_DESC,
        }
    ));
    
    private static final int NUM_OCTAVES = 4;
    
    private final WorldVolume operationVolume; //volume operated on
    private Schematic schematicBackup; //backup for undos
    private final BlockState blockState; //block type to fill
    private final int seed; //noise seed
    private final double[] amplitudes;
    private final double[] horizontalScales;
    
    public NoiseOperation(WorldVolume volume, Parameters parameters){
        this.operationVolume = volume;
        this.blockState = parameters.getParamAsBlockState(TO_SET_BLOCKTYPE_DESC);
        if (this.blockState == null) {
            throw new RuntimeException("Cannot do a set blocks operation with no block to set to");
        }
        this.seed = parameters.getParamAsInteger(NOISE_SEED_DESC);
        this.amplitudes = new double[] {
            parameters.getParamAsFloat(NOISE_1_AMP_DESC),
            parameters.getParamAsFloat(NOISE_2_AMP_DESC),
            parameters.getParamAsFloat(NOISE_3_AMP_DESC),
            parameters.getParamAsFloat(NOISE_4_AMP_DESC)
        };
        this.horizontalScales = new double[]{
            parameters.getParamAsFloat(NOISE_1_HOIZ_DESC),
            parameters.getParamAsFloat(NOISE_2_HOIZ_DESC),
            parameters.getParamAsFloat(NOISE_3_HOIZ_DESC),
            parameters.getParamAsFloat(NOISE_4_HOIZ_DESC)
        };
    }
    
    public static NoiseOperation getInstance(WorldVolume volume, Parameters parameters) {
        return new NoiseOperation(volume, parameters);
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
        Volume filteredVolume = filterVolume();
        BlockProvider provider = new FillBlockProvider(filteredVolume, this.blockState); //TODO
        ChunkUtils.writeBlocksIntoChunks(chunkControllers, provider, operationVolume.getSmallestPoint());
    }
    
    private Volume filterVolume() {
        Box box = this.operationVolume.getEnclosingBox();
        BitArray includedSet = new BitArray(box.getNumBlocksContained());
        BlockCoord smallestPoint = operationVolume.getSmallestPoint();
        int[] blockDistances = calcDistances(operationVolume);
        operationVolume.doForXyz((x, y, z) -> {
            int index = GeneralUtils.getIndexYZX(x, y, z, box.getXLength(), box.getZLength());
            //also consider Noise.gradientCoherentNoise3D
            double noiseValue = 0;
            for (int i = 0; i < NUM_OCTAVES; i++) {
                noiseValue += amplitudes[i] * Noise.valueCoherentNoise3D(
                    (x + smallestPoint.x) / horizontalScales[i],
                    (y + smallestPoint.y) / horizontalScales[i],
                    (z + smallestPoint.z) / horizontalScales[i],
                    seed,
                    NoiseQuality.STANDARD
                );
            }
            if (noiseValue < blockDistances[index]) {
                includedSet.set(index, true);
            }
        });
        return new Volume(box, includedSet);
    }
    
    private static int[] calcDistances(Volume volume) {
        Box box = volume.getEnclosingBox();
        int[] dists = new int[box.getNumBlocksContained()];
        Arrays.fill(dists, Integer.MAX_VALUE);
        Queue<Integer> indexQueue = new PriorityQueue<>();
        volume.doForXyz((x, y, z) -> {
            if (isInterior(x, y, z, volume)) {
                return;
            }
            int index = GeneralUtils.getIndexYZX(x, y, z, box.getXLength(), box.getZLength());
            dists[index] = 1;
            indexQueue.add(index);
        });
        while(!indexQueue.isEmpty()) {
            int index = indexQueue.poll();
            int dist = dists[index];
            int x = GeneralUtils.getXFromIndexYZX(index, box.getXLength());
            int y = GeneralUtils.getYFromIndexYZX(index, box.getXLength(), box.getZLength());
            int z = GeneralUtils.getZFromIndexYZX(index, box.getXLength(), box.getZLength());
            int indexXPlus  = GeneralUtils.getIndexYZX(x+1, y  , z  , box.getXLength(), box.getZLength());
            int indexXMinus = GeneralUtils.getIndexYZX(x-1, y  , z  , box.getXLength(), box.getZLength());
            int indexYPlus  = GeneralUtils.getIndexYZX(x  , y+1, z  , box.getXLength(), box.getZLength());
            int indexYMinus = GeneralUtils.getIndexYZX(x  , y-1, z  , box.getXLength(), box.getZLength());
            int indexZPlus  = GeneralUtils.getIndexYZX(x  , y  , z+1, box.getXLength(), box.getZLength());
            int indexZMinus = GeneralUtils.getIndexYZX(x  , y  , z-1, box.getXLength(), box.getZLength());
            if (volume.containsXYZ(x + 1, y, z) && dists[indexXPlus] > dist + 1) {
                dists[indexXPlus] = dist + 1;
                indexQueue.add(indexXPlus);
            }
            if (volume.containsXYZ(x - 1, y, z) && dists[indexXMinus] > dist + 1) {
                dists[indexXMinus] = dist + 1;
                indexQueue.add(indexXMinus);
            }
            if (volume.containsXYZ(x, y + 1, z) && dists[indexYPlus] > dist + 1) {
                dists[indexYPlus] = dist + 1;
                indexQueue.add(indexYPlus);
            }
            if (volume.containsXYZ(x, y - 1, z) && dists[indexYMinus] > dist + 1) {
                dists[indexYMinus] = dist + 1;
                indexQueue.add(indexYMinus);
            }
            if (volume.containsXYZ(x, y, z + 1) && dists[indexZPlus] > dist + 1) {
                dists[indexZPlus] = dist + 1;
                indexQueue.add(indexZPlus);
            }
            if (volume.containsXYZ(x, y, z - 1) && dists[indexZMinus] > dist + 1) {
                dists[indexZMinus] = dist + 1;
                indexQueue.add(indexZMinus);
            }
        }
        return dists;
    }
    
    private static boolean isInterior(int x, int y, int z, Volume volume) {
        return volume.containsXYZ(x+1, y  , z)
            && volume.containsXYZ(x-1, y  , z)
            && volume.containsXYZ(x  , y+1, z)
            && volume.containsXYZ(x  , y-1, z)
            && volume.containsXYZ(x  , y  , z+1)
            && volume.containsXYZ(x  , y  , z-1);
    }
    
    
    
}
