
package atomicedit.frontend.render.blockmodelcreation;

import atomicedit.backend.BlockState;
import atomicedit.backend.GlobalBlockTypeMap;
import atomicedit.frontend.render.shaders.DataBufferLayoutFormat;
import atomicedit.jarreading.blockmodels.CubeFace;
import atomicedit.jarreading.blockmodels.Face;
import atomicedit.jarreading.blockmodels.TexturedBox;
import atomicedit.jarreading.blockstates.BlockStateData;
import atomicedit.jarreading.blockstates.GlobalBlockStateDataLookup;
import java.util.List;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class BlockModelCreator1_13Logic implements BlockModelCreatorLogic{
    
    private static final BlockModelCreator1_13Logic INSTANCE = new BlockModelCreator1_13Logic();
    private static final float SHADE = .90f;
    
    public static BlockModelCreator1_13Logic getInstance(){
        return INSTANCE;
    }
    
    @Override
    public void addBlockRenderData(int x, int y, int z, ChunkSectionPlus section, List<Float> vertexData, List<Short> indicies) {
        short blockId = section.getBlockAt(x, y, z);
        BlockState blockState = GlobalBlockTypeMap.getBlockType(blockId);
        BlockStateData blockStateData = GlobalBlockStateDataLookup.getBlockStateDataFromBlockState(blockState);
        boolean isFullBlock = blockStateData.getBlockModelData().isFullBlock();
        for(TexturedBox box : blockStateData.getBlockModelData().getTexturedBoxes()){
            for(CubeFace cubeFace : CubeFace.values()){
                if(!isFullBlock || cubeFace.shouldDrawFace(section, blockId, x, y, z)){
                    Face face = box.faces.get(cubeFace);
                    if(face == null) continue;
                    short adjLight = cubeFace.getAdjTotalLight(x, y, z, section);
                    float light = 0.5f + (adjLight / (15f * 2));
                    addFace(x, y, z, light, box, cubeFace, face, vertexData, indicies);
                }
            }
        }
    }
    
    /**
     * Add the vertex and indices needed to draw a cubeFace of a box.
     * @param x
     * @param y
     * @param z
     * @param texture the default minecraft texture (single texture containing all loaded block textures)
     * @param faceTex the texture index of a block texture in the default minecraft texture
     * @param face the cubeFace being drawn
     * @param vertexData
     * @param indicies 
     */
    private static void addFace(int x, int y, int z, float light, TexturedBox box, CubeFace cubeFace, Face face, List<Float> vertexData, List<Short> indicies){
        float xMinTex = face.getTexCoordsMin().x;
        float xMaxTex = face.getTexCoordsMax().x;
        float yMinTex = face.getTexCoordsMin().y;
        float yMaxTex = face.getTexCoordsMax().y;
        Vector3f min = box.smallPos;
        Vector3f max = box.largePos;
        Vector3f tint = face.getTint();
        int[] base = cubeFace.coordAdditions;
        boolean useShade = box.useShade;
        float shade1 = (useShade && base[1]==0 ? SHADE : 1) * light;
        float shade2 = (useShade && base[4]==0 ? SHADE : 1) * light;
        float shade3 = (useShade && base[7]==0 ? SHADE : 1) * light;
        float shade4 = (useShade && base[10]==0 ? SHADE : 1)* light;
        addFaceIndicies(vertexData.size() / DataBufferLayoutFormat.NUM_ELEMENTS_PER_VERTEX, cubeFace.indicies, indicies);
        addAll(vertexData, new float[]{
            x + (base[0]==0?min.x:max.x), y + (base[1]==0?min.y:max.y),  z + (base[2]==0?min.z:max.z),  xMinTex,yMinTex,  shade1*tint.x, shade1*tint.y, shade1*tint.z, 1, //slight shading
            x + (base[3]==0?min.x:max.x), y + (base[4]==0?min.y:max.y),  z + (base[5]==0?min.z:max.z),  xMaxTex,yMinTex,  shade2*tint.x, shade2*tint.y, shade2*tint.z, 1,
            x + (base[6]==0?min.x:max.x), y + (base[7]==0?min.y:max.y),  z + (base[8]==0?min.z:max.z),  xMaxTex,yMaxTex,  shade3*tint.x, shade3*tint.y, shade3*tint.z, 1,
            x + (base[9]==0?min.x:max.x), y + (base[10]==0?min.y:max.y), z + (base[11]==0?min.z:max.z), xMinTex,yMaxTex,  shade4*tint.x, shade4*tint.y, shade4*tint.z, 1,
        });
    }
    
    private static void addFaceIndicies(int numVerticies, short[] toAdd, List<Short> indicies){
        for(int i = 0; i < toAdd.length; i++){
            indicies.add((short)(toAdd[i] + numVerticies));
        }
    }
    
    private static void addAll(List<Float> dest, float[] source){
        for(int i = 0; i < source.length; i++){
            dest.add(source[i]);
        }
    }
    
}
