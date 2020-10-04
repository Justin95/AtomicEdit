
package atomicedit.frontend.render.blockmodelcreation;

import atomicedit.backend.BlockState;
import atomicedit.backend.GlobalBlockStateMap;
import atomicedit.frontend.render.RenderObject;
import atomicedit.jarreading.blockmodels.ModelBox;
import atomicedit.jarreading.blockmodels.ModelBox.ModelBoxFace;
import atomicedit.jarreading.blockstates.BlockStateModel;
import atomicedit.jarreading.blockstates.BlockStateModelLookup;
import atomicedit.utils.FloatList;
import atomicedit.utils.IntList;
import atomicedit.utils.MathUtils;
import java.util.List;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3ic;

/**
 * Individual instances of this class are not thread safe. Use at least
 * one instance per thread.
 * @author Justin Bonner
 */
public class BlockModelCreator1_13Logic implements BlockModelCreatorLogic {
    
    private static final float SHADE = .90f;
    private static final Vector3f BLOCK_STATE_ROTATION_ROTATE_ABOUT = new Vector3f(.5f, .5f, .5f);
    private static final ModelBoxFace[] MODEL_FACES = ModelBoxFace.values();
    
    private final CreatorBuffer buffer;
    
    private BlockModelCreator1_13Logic() {
        buffer = new CreatorBuffer();
    }
    
    public static BlockModelCreator1_13Logic getInstance(){
        return new BlockModelCreator1_13Logic();
    }
    
    @Override
    public void addBlockRenderData(int x, int y, int z, ChunkSectionPlus section, FloatList vertexData, IntList indicies, boolean includeTranslucent){
        short blockId = section.getBlockAt(x, y, z);
        BlockState blockState = GlobalBlockStateMap.getBlockType(blockId);
        List<BlockStateModel> blockStateModels = BlockStateModelLookup.getBlockStateModel(blockState);
        for(int i = 0; i < blockStateModels.size(); i++){
            BlockStateModel blockStateModel = blockStateModels.get(i);
            boolean isFullBlock = blockStateModel.isFullBlock();
            if(isFullBlock && isUnseeable(section, blockId, x, y, z)){
                continue;
            }
            for(int j = 0; j < blockStateModel.getBlockModel().getBlockModels().size(); j++){
                ModelBox modelBox = blockStateModel.getBlockModel().getBlockModels().get(j);
                if(modelBox.hasTranslucency() != includeTranslucent){ //translucent blocks and opaque blocks done in different calls
                    continue;
                }
                for(int k = 0; k < MODEL_FACES.length; k++){
                    ModelBoxFace face = MODEL_FACES[k];
                    buffer.rot.set(blockStateModel.getRotation()).mul(-1);
                    Vector3f rotation = buffer.rot;//new Vector3f(blockStateModel.getRotation()).mul(-1);
                    Vector3ic adjDir = getAdjacent(face, blockStateModel.getRotation());
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
    
    private void addFace(int x, int y, int z, float light, ModelBox modelBox, ModelBoxFace face, Vector3f rotation, FloatList vertexData, IntList indicies){
        Vector3f posA = buffer.posA;
        Vector3f posB = buffer.posB;
        Vector3f posC = buffer.posC;
        Vector3f posD = buffer.posD;
        Vector2f texA = buffer.texA;
        Vector2f texB = buffer.texB;
        Vector2f texC = buffer.texC;
        Vector2f texD = buffer.texD;
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
        float[] v = buffer.vd;
        v[0] = x + posA.x; v[1] = y + posA.y; v[2] = z + posA.z;   v[3] = texA.x; v[4] = texA.y;   v[5] = lightA * tint.x; v[6] = lightA * tint.y; v[7] = lightA * tint.z; v[8] = 1;
        v[9] = x + posB.x; v[10]= y + posB.y; v[11]= z + posB.z;   v[12]= texB.x; v[13]= texB.y;   v[14]= lightB * tint.x; v[15]= lightB * tint.y; v[16]= lightB * tint.z; v[17]= 1;
        v[18]= x + posC.x; v[19]= y + posC.y; v[20]= z + posC.z;   v[21]= texC.x; v[22]= texC.y;   v[23]= lightC * tint.x; v[24]= lightC * tint.y; v[25]= lightC * tint.z; v[26]= 1;
        v[27]= x + posD.x; v[28]= y + posD.y; v[29]= z + posD.z;   v[30]= texD.x; v[31]= texD.y;   v[32]= lightD * tint.x; v[33]= lightD * tint.y; v[34]= lightD * tint.z; v[35]= 1;
        vertexData.addAll(v);
        /*
        vertexData.addAll(
            x + posA.x, y + posA.y, z + posA.z,   texA.x, texA.y,    lightA * tint.x, lightA * tint.y, lightA * tint.z, 1,
            x + posB.x, y + posB.y, z + posB.z,   texB.x, texB.y,    lightB * tint.x, lightB * tint.y, lightB * tint.z, 1,
            x + posC.x, y + posC.y, z + posC.z,   texC.x, texC.y,    lightC * tint.x, lightC * tint.y, lightC * tint.z, 1,
            x + posD.x, y + posD.y, z + posD.z,   texD.x, texD.y,    lightD * tint.x, lightD * tint.y, lightD * tint.z, 1
        );*/
    }
    
    
    private static short getAdjTotalLight(Vector3ic adjDir, int x, int y, int z, ChunkSectionPlus section){
        return section.getTotalLightAt(x + adjDir.x(), y + adjDir.y(), z + adjDir.z());
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
        for(int k = 0; k < MODEL_FACES.length; k++){
            ModelBoxFace face = MODEL_FACES[k];
            if(!isFaceUnviewable(face.getAdjacentDirection(), section, blockId, x, y, z)){
                return false;
            }
        }
        return true;
    }
    
    private static boolean isFaceUnviewable(Vector3ic adjDir, ChunkSectionPlus section, short blockId, int x, int y, int z){
        return section.getBlockAt(x + adjDir.x(), y + adjDir.y(), z + adjDir.z()) == blockId; //check is adjacent block is the same
    }
    
    private static Vector3ic getAdjacent(ModelBox.ModelBoxFace face, Vector3f rotation){
        int xTurns = Math.round(((rotation.x + 360) % 360) / 90f);
        int yTurns = Math.round(((rotation.y + 360) % 360) / 90f);
        int zTurns = Math.round(((rotation.z + 360) % 360) / 90f);
        ModelBox.ModelBoxFace dest = face.rotate(xTurns, yTurns, zTurns);
        return dest.getAdjacentDirection();
    }
    
    private static void rotateFace(Vector3f posA, Vector3f posB, Vector3f posC, Vector3f posD, Vector3f rotation){
        if(rotation.x != 0 || rotation.y != 0 || rotation.z != 0){
            MathUtils.rotateAllAxisAbout(posA, rotation, BLOCK_STATE_ROTATION_ROTATE_ABOUT);
            MathUtils.rotateAllAxisAbout(posB, rotation, BLOCK_STATE_ROTATION_ROTATE_ABOUT);
            MathUtils.rotateAllAxisAbout(posC, rotation, BLOCK_STATE_ROTATION_ROTATE_ABOUT);
            MathUtils.rotateAllAxisAbout(posD, rotation, BLOCK_STATE_ROTATION_ROTATE_ABOUT);
        }
    }
    
    private static void addFaceIndicies(int numVerticies, int[] toAdd, IntList indicies){
        for(int i = 0; i < toAdd.length; i++){
            indicies.add(toAdd[i] + numVerticies);
        }
    }
    
    /**
    * Java doesn't allow creating objects on the stack so we have to resort
    * to reusing this buffer to avoid way to many small short lived heap allocs
    * in frequently called code.
    * @author Justin Bonner
    */
   private static class CreatorBuffer {

       final Vector3f posA;
       final Vector3f posB;
       final Vector3f posC;
       final Vector3f posD;
       final Vector2f texA;
       final Vector2f texB;
       final Vector2f texC;
       final Vector2f texD;
       final float[] vd;
       final Vector3f rot;

       public CreatorBuffer() {
           posA = new Vector3f();
           posB = new Vector3f();
           posC = new Vector3f();
           posD = new Vector3f();
           texA = new Vector2f();
           texB = new Vector2f();
           texC = new Vector2f();
           texD = new Vector2f();
           vd = new float[9 * 4]; //9 floats per vertex, 4 verticies
           rot = new Vector3f();
       }

   }
    
}
