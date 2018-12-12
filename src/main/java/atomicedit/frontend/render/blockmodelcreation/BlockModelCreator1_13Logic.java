
package atomicedit.frontend.render.blockmodelcreation;

import atomicedit.backend.BlockState;
import atomicedit.backend.GlobalBlockTypeMap;
import atomicedit.frontend.render.shaders.DataBufferLayoutFormat;
import atomicedit.frontend.texture.MinecraftTexture;
import atomicedit.jarreading.blockmodels.TexturedBox;
import atomicedit.jarreading.blockstates.BlockStateData;
import atomicedit.jarreading.blockstates.GlobalBlockStateDataLookup;
import atomicedit.jarreading.texture.TextureLoader;
import atomicedit.logging.Logger;
import java.util.List;
import org.joml.Vector3f;

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
                    addFace(x, y, z, texture, texture.getIndexFromTextureName(box.getTextureName(face.boxFace)), box, face, vertexData, indicies);
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
    private static void addFace(int x, int y, int z, MinecraftTexture texture, int faceTex, TexturedBox box, Face face, List<Float> vertexData, List<Short> indicies){
        float xMinTex = texture.getTextureCoordX(faceTex);
        float xMaxTex = xMinTex + texture.getTextureCoordDelta();
        float yMinTex = texture.getTextureCoordY(faceTex);
        float yMaxTex = yMinTex + texture.getTextureCoordDelta();
        Vector3f min = box.smallPos;
        Vector3f max = box.largePos;
        int[] base = face.coordAdditions;
        addAll(vertexData, new float[]{x + (base[0]==0?min.x:max.x), y + (base[1]==0?min.y:max.y),  z + (base[2]==0?min.x:max.x),  xMinTex,yMinTex,  1,1,1,1,
                                       x + (base[3]==0?min.x:max.x), y + (base[4]==0?min.y:max.y),  z + (base[5]==0?min.x:max.x),  xMaxTex,yMinTex,  1,1,1,1,
                                       x + (base[6]==0?min.x:max.x), y + (base[7]==0?min.y:max.y),  z + (base[8]==0?min.x:max.x),  xMaxTex,yMaxTex,  1,1,1,1,
                                       x + (base[9]==0?min.x:max.x), y + (base[10]==0?min.y:max.y), z + (base[11]==0?min.x:max.x), xMinTex,yMaxTex,  1,1,1,1
        });
        addFaceIndicies(vertexData.size() / DataBufferLayoutFormat.NUM_ELEMENTS_PER_VERTEX, indicies);
    }
    
    /**
     * Store the needed differences between faces on a box to allow iterating over the faces to draw them.
     */
    private static enum Face{
        UP(
            TexturedBox.BoxFace.UP,
            (section, blockId, x, y, z) -> section.getBlockAt(x, y + 1, z) != blockId,
            new int[]{
                0, 1, 0,
                1, 1, 0,
                1, 1, 1,
                0, 1, 1
            }
        ),
        DOWN(
            TexturedBox.BoxFace.DOWN,
            (section, blockId, x, y, z) -> section.getBlockAt(x, y - 1, z) != blockId,
            new int[]{
                0, 0, 0,
                1, 0, 0,
                1, 0, 1,
                0, 0, 1
            }
        ),
        NORTH(
            TexturedBox.BoxFace.NORTH,
            (section, blockId, x, y, z) -> section.getBlockAt(x, y, z - 1) != blockId,
            new int[]{
                0, 0, 0,
                1, 0, 0,
                1, 1, 0,
                0, 1, 0
            }
        ),
        SOUTH(
            TexturedBox.BoxFace.SOUTH,
            (section, blockId, x, y, z) -> section.getBlockAt(x, y, z + 1) != blockId,
            new int[]{
                0, 0, 1,
                1, 0, 1,
                1, 1, 1,
                0, 1, 1
            }
        ),
        EAST(
            TexturedBox.BoxFace.EAST,
            (section, blockId, x, y, z) -> section.getBlockAt(x + 1, y, z) != blockId,
            new int[]{
                1, 0, 0,
                1, 0, 1,
                1, 1, 1,
                1, 1, 0
            }
        ),
        WEST(
            TexturedBox.BoxFace.UP,
            (section, blockId, x, y, z) -> section.getBlockAt(x - 1, y, z) != blockId,
            new int[]{
                0, 0, 0,
                0, 0, 1,
                0, 1, 1,
                0, 1, 0
            }
        ),
        ;
        final TexturedBox.BoxFace boxFace;
        final FaceDesider faceDesider;
        /**
         * Specify additions to a set of base coordinates to get the corners of this face.
         * The coordinate additions are specified x,y,z around the face following the right hand rule.
         * The coordinates are listed counter clockwise if looking at the face.
         * There are always 4 vertices listed with 3 points each for an array length of 12.
         */
        final int[] coordAdditions;
        
        Face(TexturedBox.BoxFace face, FaceDesider desider, int[] coordAdditions){
            this.boxFace = face;
            this.faceDesider = desider;
            this.coordAdditions = coordAdditions;
        }
        
        boolean shouldDrawFace(ChunkSectionPlus section, short blockId, int x, int y, int z){
            return this.faceDesider.shouldDraw(section, blockId, x, y, z);
        }
        
        private interface FaceDesider{
            boolean shouldDraw(ChunkSectionPlus section, short blockId, int x, int y, int z);
        }
        
    }
    
    private static void addFaceIndicies(int numVerticies, List<Short> indicies){
        //triangle 1
        indicies.add((short)(0 + numVerticies));
        indicies.add((short)(2 + numVerticies));
        indicies.add((short)(1 + numVerticies));
        //triangle 2
        indicies.add((short)(0 + numVerticies));
        indicies.add((short)(3 + numVerticies));
        indicies.add((short)(2 + numVerticies));
    }
    
    private static void addAll(List<Float> dest, float[] source){
        for(int i = 0; i < source.length; i++){
            dest.add(source[i]);
        }
    }
    
}
