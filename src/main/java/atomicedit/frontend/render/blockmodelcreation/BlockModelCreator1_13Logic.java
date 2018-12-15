
package atomicedit.frontend.render.blockmodelcreation;

import atomicedit.backend.BlockState;
import atomicedit.backend.GlobalBlockTypeMap;
import atomicedit.frontend.render.shaders.DataBufferLayoutFormat;
import atomicedit.frontend.texture.MinecraftTexture;
import atomicedit.jarreading.blockmodels.TexturedBox;
import atomicedit.jarreading.blockstates.BlockStateData;
import atomicedit.jarreading.blockstates.GlobalBlockStateDataLookup;
import atomicedit.jarreading.texture.TextureLoader;
import java.util.List;
import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 *
 * @author Justin Bonner
 */
public class BlockModelCreator1_13Logic implements BlockModelCreatorLogic{
    
    private static final BlockModelCreator1_13Logic INSTANCE = new BlockModelCreator1_13Logic();
    
    public static BlockModelCreator1_13Logic getInstance(){
        return INSTANCE;
    }
    
    @Override
    public void addBlockRenderData(int x, int y, int z, ChunkSectionPlus section, List<Float> vertexData, List<Short> indicies) {
        short blockId = section.getBlockAt(x, y, z);
        MinecraftTexture texture = TextureLoader.getMinecraftDefaultTexture();
        BlockState blockState = GlobalBlockTypeMap.getBlockType(blockId);
        BlockStateData blockStateData = GlobalBlockStateDataLookup.getBlockStateDataFromBlockState(blockState);
        boolean isFullBlock = blockStateData.getBlockModelData().isFullBlock();
        for(TexturedBox box : blockStateData.getBlockModelData().getTexturedBoxes()){
            for(Face face : Face.values()){
                if(!isFullBlock || face.shouldDrawFace(section, blockId, x, y, z)){
                    short adjLight = face.getAdjTotalLight(x, y, z, section);
                    float light = 0.5f + (adjLight / (15f * 2));
                    addFace(x, y, z, texture, texture.getIndexFromTextureName(box.getTextureName(face.boxFace)), light, box, face, vertexData, indicies);
                }
            }
        }
    }
    
    /**
     * Add the vertex and indices needed to draw a face of a box.
     * @param x
     * @param y
     * @param z
     * @param texture the default minecraft texture (single texture containing all loaded block textures)
     * @param faceTex the texture index of a block texture in the default minecraft texture
     * @param face the face being drawn
     * @param vertexData
     * @param indicies 
     */
    private static void addFace(int x, int y, int z, MinecraftTexture texture, int faceTex, float light, TexturedBox box, Face face, List<Float> vertexData, List<Short> indicies){
        float xMinTex = texture.getTextureCoordX(faceTex);
        float xMaxTex = xMinTex + texture.getTextureCoordDelta();
        float yMaxTex = texture.getTextureCoordY(faceTex);
        float yMinTex = yMaxTex + texture.getTextureCoordDelta();
        Vector3f min = box.smallPos;
        Vector3f max = box.largePos;
        int[] base = face.coordAdditions;
        addFaceIndicies(vertexData.size() / DataBufferLayoutFormat.NUM_ELEMENTS_PER_VERTEX, face.indicies, indicies);
        addAll(vertexData, new float[]{x + (base[0]==0?min.x:max.x), y + (base[1]==0?min.y:max.y),  z + (base[2]==0?min.x:max.x),  xMinTex,yMinTex,  light, light, light, 1,
                                       x + (base[3]==0?min.x:max.x), y + (base[4]==0?min.y:max.y),  z + (base[5]==0?min.x:max.x),  xMaxTex,yMinTex,  light, light, light, 1,
                                       x + (base[6]==0?min.x:max.x), y + (base[7]==0?min.y:max.y),  z + (base[8]==0?min.x:max.x),  xMaxTex,yMaxTex,  light, light, light, 1,
                                       x + (base[9]==0?min.x:max.x), y + (base[10]==0?min.y:max.y), z + (base[11]==0?min.x:max.x), xMinTex,yMaxTex,  light, light, light, 1,
        });
    }
    
    /**
     * Store the needed differences between faces on a box to allow iterating over the faces to draw them.
     */
    private static enum Face{
        UP(
            TexturedBox.BoxFace.UP,
            new Vector3i(0, 1, 0), //up is y + 1
            new int[]{
                0, 1, 0,
                1, 1, 0,
                1, 1, 1,
                0, 1, 1
            },
            new short[]{ //indicies
                0, 2, 1,   0, 3, 2
            }
        ),
        DOWN(
            TexturedBox.BoxFace.DOWN,
            new Vector3i(0, -1, 0), //down is y - 1
            new int[]{
                0, 0, 0,
                1, 0, 0,
                1, 0, 1,
                0, 0, 1
            },
            new short[]{
                0, 1, 2,   0, 2, 3
            }
        ),
        NORTH(
            TexturedBox.BoxFace.NORTH,
            new Vector3i(0, 0, -1), //north is z - 1,
            new int[]{
                0, 0, 0,
                1, 0, 0,
                1, 1, 0,
                0, 1, 0
            },
            new short[]{
                0, 2, 1,   0, 3, 2
            }
        ),
        SOUTH(
            TexturedBox.BoxFace.SOUTH,
            new Vector3i(0, 0, 1), //south is z + 1,
            new int[]{
                0, 0, 1,
                1, 0, 1,
                1, 1, 1,
                0, 1, 1
            },
            new short[]{ //indicies
                0, 1, 2,   0, 2, 3
            }
        ),
        EAST(
            TexturedBox.BoxFace.EAST,
            new Vector3i(1, 0, 0), //east is x + 1,
            new int[]{
                1, 0, 0,
                1, 0, 1,
                1, 1, 1,
                1, 1, 0
            },
            new short[]{ //indicies
                0, 2, 1,   0, 3, 2
            }
        ),
        WEST(
            TexturedBox.BoxFace.WEST,
            new Vector3i(-1, 0, 0), //west is x - 1,
            new int[]{
                0, 0, 0,
                0, 0, 1,
                0, 1, 1,
                0, 1, 0
            },
            new short[]{ //indicies
                0, 1, 2,   0, 2, 3
            }
        ),
        ;
        final TexturedBox.BoxFace boxFace;
        final Vector3i adjTrans;
        /**
         * Specify additions to a set of base coordinates to get the corners of this face.
         * The coordinate additions are specified x,y,z around the face following the right hand rule.
         * The coordinates are listed counter clockwise if looking at the face.
         * There are always 4 vertices listed with 3 points each for an array length of 12.
         */
        final int[] coordAdditions;
        final short[] indicies;
        
        Face(TexturedBox.BoxFace face, Vector3i adjTrans, int[] coordAdditions, short[] indicies){
            this.boxFace = face;
            this.adjTrans = adjTrans;
            this.coordAdditions = coordAdditions;
            this.indicies = indicies;
        }
        
        short getAdjTotalLight(int x, int y, int z, ChunkSectionPlus section){
            return section.getTotalLightAt(x + adjTrans.x, y + adjTrans.y, z + adjTrans.z);
        }
        
        boolean shouldDrawFace(ChunkSectionPlus section, short blockId, int x, int y, int z){
            return section.getBlockAt(x + adjTrans.x, y + adjTrans.y, z + adjTrans.z) != blockId; //check is adjacent block is the same
        }
        
        
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
