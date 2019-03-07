
package atomicedit.frontend.render.blockmodelcreation;

import atomicedit.backend.BlockState;
import atomicedit.backend.GlobalBlockStateMap;
import atomicedit.frontend.render.shaders.DataBufferLayoutFormat;
import atomicedit.jarreading.blockmodels.BlockModelData;
import atomicedit.jarreading.blockmodels.CubeFace;
import atomicedit.jarreading.blockmodels.PositionedFace;
import atomicedit.jarreading.blockmodels.TexturedFace;
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
    public void addBlockRenderData(int x, int y, int z, ChunkSectionPlus section, List<Float> vertexData, List<Integer> indicies, boolean includeTranslucent){
        short blockId = section.getBlockAt(x, y, z);
        BlockState blockState = GlobalBlockStateMap.getBlockType(blockId);
        BlockStateData blockStateData = GlobalBlockStateDataLookup.getBlockStateDataFromBlockState(blockState);
        for(BlockModelData modelData : blockStateData.getBlockModelDatas()){
            boolean isFullBlock = modelData.isFullBlock();
            for(TexturedBox box : modelData.getTexturedBoxes()){
                for(CubeFace cubeFace : CubeFace.values()){
                    if(!isFullBlock || cubeFace.shouldDrawFace(section, blockId, x, y, z)){
                        TexturedFace texFace = box.texFaces.get(cubeFace);
                        PositionedFace posFace = box.posFaces.get(cubeFace);
                        if(texFace == null || posFace == null) continue;
                        if(texFace.isTranslucent() != includeTranslucent){
                            continue;
                        }
                        short adjLight = cubeFace.getAdjTotalLight(x, y, z, section);
                        float light = 0.5f + (adjLight / (15f * 2)); //scale 0-15 light levels between half light and full light
                        addFace(x, y, z, light, box, cubeFace, texFace, posFace, vertexData, indicies);
                    }
                }
            }
        }
    }
    
    /**
     * Add the vertex and indices needed to draw a cubeFace of a box.
     * @param x
     * @param y
     * @param z
     * @param light
     * @param box the textured box
     * @param cubeFace
     * @param texFace the cubeFace being drawn
     * @param vertexData
     * @param indicies 
     */
    private static void addFace(int x, int y, int z, float light, TexturedBox box, CubeFace cubeFace, TexturedFace texFace, PositionedFace posFace, List<Float> vertexData, List<Integer> indicies){
        float xMinTex = texFace.getTexCoordsMin().x;
        float xMaxTex = texFace.getTexCoordsMax().x;
        float yMinTex = texFace.getTexCoordsMin().y;
        float yMaxTex = texFace.getTexCoordsMax().y;
        //Vector3f min = box.smallPos;
        //Vector3f max = box.largePos;
        Vector3f tint = texFace.getTint();
        int[] base = cubeFace.coordAdditions;
        boolean useShade = box.useShade;
        float shade1 = (useShade && base[1]==0 ? SHADE : 1) * light;
        float shade2 = (useShade && base[4]==0 ? SHADE : 1) * light;
        float shade3 = (useShade && base[7]==0 ? SHADE : 1) * light;
        float shade4 = (useShade && base[10]==0 ? SHADE : 1)* light;
        addFaceIndicies(vertexData.size() / DataBufferLayoutFormat.NUM_ELEMENTS_PER_VERTEX, posFace.indicies, indicies);
        addAll(vertexData, new float[]{
            x + posFace.pos1.x, y + posFace.pos1.y, z + posFace.pos1.z,  xMinTex,yMinTex,  shade1*tint.x, shade1*tint.y, shade1*tint.z, 1, //slight shading
            x + posFace.pos2.x, y + posFace.pos2.y, z + posFace.pos2.z,  xMaxTex,yMinTex,  shade2*tint.x, shade2*tint.y, shade2*tint.z, 1,
            x + posFace.pos3.x, y + posFace.pos3.y, z + posFace.pos3.z,  xMaxTex,yMaxTex,  shade3*tint.x, shade3*tint.y, shade3*tint.z, 1,
            x + posFace.pos4.x, y + posFace.pos4.y, z + posFace.pos4.z,  xMinTex,yMaxTex,  shade4*tint.x, shade4*tint.y, shade4*tint.z, 1,
        });
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
