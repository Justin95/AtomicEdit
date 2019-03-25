
package atomicedit.frontend.render.blockmodelcreation;

import atomicedit.backend.BlockState;
import atomicedit.backend.GlobalBlockStateMap;
import atomicedit.frontend.render.RenderObject;
import atomicedit.frontend.render.shaders.DataBufferLayoutFormat;
import atomicedit.jarreading.blockmodels.ModelBox;
import atomicedit.jarreading.blockmodels.ModelBox.ModelBoxFace;
import atomicedit.jarreading.blockstates.BlockStateModel;
import atomicedit.jarreading.blockstates.BlockStateModelLookup;
import atomicedit.utils.MathUtils;
import java.util.List;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 *
 * @author Justin Bonner
 */
public class BlockModelCreator1_13Logic implements BlockModelCreatorLogic{
    
    private static final BlockModelCreator1_13Logic INSTANCE = new BlockModelCreator1_13Logic();
    private static final float SHADE = .90f;
    private static final Vector3f BLOCK_STATE_ROTATION_ROTATE_ABOUT = new Vector3f(.5f, .5f, .5f);
    
    public static BlockModelCreator1_13Logic getInstance(){
        return INSTANCE;
    }
    
    @Override
    public void addBlockRenderData(int x, int y, int z, ChunkSectionPlus section, List<Float> vertexData, List<Integer> indicies, boolean includeTranslucent){
        short blockId = section.getBlockAt(x, y, z);
        BlockState blockState = GlobalBlockStateMap.getBlockType(blockId);
        List<BlockStateModel> blockStateModels = BlockStateModelLookup.getBlockStateModel(blockState);
        for(BlockStateModel blockStateModel : blockStateModels){
            boolean isFullBlock = blockStateModel.isFullBlock();
            if(isFullBlock && isUnseeable(section, blockId, x, y, z)){
                continue;
            }
            for(ModelBox modelBox : blockStateModel.getBlockModel().getBlockModels()){
                if(modelBox.hasTranslucency() != includeTranslucent){ //translucent blocks and opaque blocks done in different calls
                    continue;
                }
                for(ModelBoxFace face : ModelBoxFace.values()){
                    Vector3f rotation = new Vector3f(blockStateModel.getRotation()).mul(-1);
                    Vector3i adjDir = getAdjacent(modelBox, face, rotation);
                    if(!modelBox.hasFace(face) || (isFullBlock && isFaceUnviewable(adjDir, section, blockId, x, y, z))){
                        continue;
                    }
                    short blockLight;
                    if(isFullBlock){
                        blockLight = getAdjTotalLight(adjDir, x, y, z, section);
                    }else{
                        short adjLight = getAdjTotalLight(adjDir, x, y, z, section);
                        short atLight = section.getTotalLightAt(x, y, z);
                        blockLight = adjLight > atLight ? adjLight : atLight;
                    }
                    float light = 0.5f + (blockLight / (15f * 2)); //scale 0-15 light levels between half light and full light
                    addFace(x, y, z, light, modelBox, face, rotation, vertexData, indicies);
                }
            }
        }
    }
    
    private static void addFace(int x, int y, int z, float light, ModelBox modelBox, ModelBoxFace face, Vector3f rotation, List<Float> vertexData, List<Integer> indicies){
        Vector3f posA = new Vector3f(); //hopefully these will be allocated on the stack, wouldnt be thread safe if they were made instance vars
        Vector3f posB = new Vector3f();
        Vector3f posC = new Vector3f();
        Vector3f posD = new Vector3f();
        Vector2f texA = new Vector2f();
        Vector2f texB = new Vector2f();
        Vector2f texC = new Vector2f();
        Vector2f texD = new Vector2f();
        modelBox.getFacePosVerticies(face, posA, posB, posC, posD);
        modelBox.getTexCoordVerticies(face, texA, texB, texC, texD);
        //Logger.info("texA: " + texA + " texB: " + texB + " texC: " + texC + " texD: " + texD);
        
        rotateFace(posA, posB, posC, posD, rotation);
        
        Vector3f tint = modelBox.getTintColor(face);
        
        float lightA = (modelBox.getUseShade() && posA.y < 0.01 ? SHADE : 1f) * light;
        float lightB = (modelBox.getUseShade() && posB.y < 0.01 ? SHADE : 1f) * light;
        float lightC = (modelBox.getUseShade() && posC.y < 0.01 ? SHADE : 1f) * light;
        float lightD = (modelBox.getUseShade() && posD.y < 0.01 ? SHADE : 1f) * light;
        
        addFaceIndicies(vertexData.size() / RenderObject.BUFFER_FORMAT.NUM_ELEMENTS_PER_VERTEX, new int[]{0, 1, 2, 0, 2, 3}, indicies);
        addAll(vertexData, new float[]{
            x + posA.x, y + posA.y, z + posA.z,   texA.x, texA.y,    lightA * tint.x, lightA * tint.y, lightA * tint.z, 1,
            x + posB.x, y + posB.y, z + posB.z,   texB.x, texB.y,    lightB * tint.x, lightB * tint.y, lightB * tint.z, 1,
            x + posC.x, y + posC.y, z + posC.z,   texC.x, texC.y,    lightC * tint.x, lightC * tint.y, lightC * tint.z, 1,
            x + posD.x, y + posD.y, z + posD.z,   texD.x, texD.y,    lightD * tint.x, lightD * tint.y, lightD * tint.z, 1
        });
    }
    
    
    private static short getAdjTotalLight(Vector3i adjDir, int x, int y, int z, ChunkSectionPlus section){
        return section.getTotalLightAt(x + adjDir.x, y + adjDir.y, z + adjDir.z);
    }
    
    /*
    private static boolean shouldDrawFaceIfFullBlock(Vector3i adjDir, ChunkSectionPlus section, short blockId, int x, int y, int z){
        return section.getBlockAt(x + adjDir.x, y + adjDir.y, z + adjDir.z) != blockId; //check is adjacent block is the same
    }
    */
    /**
     * Determines if a full block is completely surrounded by identical blocks.
     * @return 
     */
    private static boolean isUnseeable(ChunkSectionPlus section, short blockId, int x, int y, int z){
        for(ModelBox.ModelBoxFace face : ModelBox.ModelBoxFace.values()){ //really just need to iterate over all adjDirections
            if(!isFaceUnviewable(face.getAdjacentDirection(), section, blockId, x, y, z)){
                return false;
            }
        }
        return true;
    }
    
    private static boolean isFaceUnviewable(Vector3i adjDir, ChunkSectionPlus section, short blockId, int x, int y, int z){
        return section.getBlockAt(x + adjDir.x, y + adjDir.y, z + adjDir.z) == blockId; //check is adjacent block is the same
    }
    
    private static Vector3i getAdjacent(ModelBox modelBox, ModelBox.ModelBoxFace face, Vector3f rotation){
        Vector3f posA = new Vector3f();
        Vector3f posB = new Vector3f();
        Vector3f posC = new Vector3f();
        Vector3f posD = new Vector3f();
        modelBox.getFacePosVerticies(face, posA, posB, posC, posD);
        rotateFace(posA, posB, posC, posD, rotation);
        Vector3i offset = getAdjacent(posA, posB, posC, posD, modelBox.getCenter());
        return offset;
    }
    
    private static Vector3i getAdjacent(Vector3f posA, Vector3f posB, Vector3f posC, Vector3f posD, Vector3f center){
        Vector3f testA = new Vector3f(posA);
        Vector3f testB = new Vector3f(posB);
        Vector3f testC = new Vector3f(posC);
        Vector3f testD = new Vector3f(posD);
        Vector3f faceAvg = MathUtils.average(testA, testB, testC, testD);
        
        Vector3f dir = faceAvg.sub(center);
        Vector3f absDir = new Vector3f(Math.abs(dir.x), Math.abs(dir.y), Math.abs(dir.z));
        
        if(absDir.x >= absDir.y && absDir.x >= absDir.z){
            return new Vector3i(dir.x > 0 ? 1 : -1, 0, 0);
        }else if(absDir.y >= absDir.x && absDir.y >= absDir.z){
            return new Vector3i(0, dir.y > 0 ? 1 : -1, 0);
        }else if(absDir.z >= absDir.x && absDir.z >= absDir.y){
            return new Vector3i(0, 0, dir.z > 0 ? 1 : -1);
        }
        throw new RuntimeException("Tried to get adjacent direction of non full block.");
    }
    
    private static void rotateFace(Vector3f posA, Vector3f posB, Vector3f posC, Vector3f posD, Vector3f rotation){
        if(rotation.x != 0 || rotation.y != 0 || rotation.z != 0){
            MathUtils.rotateAllAxisAbout(posA, rotation, BLOCK_STATE_ROTATION_ROTATE_ABOUT);
            MathUtils.rotateAllAxisAbout(posB, rotation, BLOCK_STATE_ROTATION_ROTATE_ABOUT);
            MathUtils.rotateAllAxisAbout(posC, rotation, BLOCK_STATE_ROTATION_ROTATE_ABOUT);
            MathUtils.rotateAllAxisAbout(posD, rotation, BLOCK_STATE_ROTATION_ROTATE_ABOUT);
        }
    }
    
    private static void addFaceIndicies(int numVerticies, int[] toAdd, List<Integer> indicies){
        for(int i = 0; i < toAdd.length; i++){
            indicies.add(toAdd[i] + numVerticies);
        }
    }
    
    private static void addAll(List<Float> dest, float[] source){
        for(int i = 0; i < source.length; i++){
            dest.add(source[i]);
        }
    }
    
}
