
package atomicedit.jarreading.blockmodels_v2;

import atomicedit.frontend.texture.MinecraftTexture;
import atomicedit.jarreading.texture.TextureLoader;
import atomicedit.utils.MathUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

/**
 *
 * @author Justin Bonner
 */
public class ModelBox {
    
    static final Vector3f NO_BLOCK_TINT = new Vector3f(1f, 1f, 1f);
    static final Vector3f RED_BLOCK_TINT = new Vector3f(1f, .2f, .2f);
    static final Vector3f GREEN_BLOCK_TINT = new Vector3f(.3f, .9f, .3f);
    static final Vector3f BLUE_BLOCK_TINT = new Vector3f(.3f, .3f, 1f);
    
    private static final int NUM_POS_VERTICIES = 8;
    private static final int POS_VERTICIES_SIZE = 3;
    private static final int POS_DATA_LENGTH = NUM_POS_VERTICIES * POS_VERTICIES_SIZE;
    private static final int NUM_TEX_COORD_VERTICIES = 4 * 6; //4 verticies per face, faces do not share verticies
    private static final int TEX_COORD_VERTICIES_SIZE = 2;
    private static final int TEX_COORD_DATA_LENGTH = NUM_TEX_COORD_VERTICIES * TEX_COORD_VERTICIES_SIZE;
    
    private final float[] posData; //xyz: 000, 001, 010, 011, 100, 101, 110, 111
    private final float[] texCoordData;
    private final boolean isFullBlock;
    private final Vector3f[] blockTintColorData;
    private final boolean[] existingFaces;
    private final boolean hasTranslucency;
    private final boolean useShade;
    private final Vector3f center;
    
    private ModelBox(float[] posData, float[] texCoordData, Vector3f[] blockTintColors, boolean[] existingFaces, boolean hasTranslucency, boolean useShade){
        this.posData = posData;
        this.texCoordData = texCoordData;
        this.isFullBlock = checkFullBlock(posData);
        this.blockTintColorData = blockTintColors;
        this.existingFaces = existingFaces;
        this.hasTranslucency = hasTranslucency;
        this.useShade = useShade;
        this.center = calcCenter();
    }
    
    static ModelBox getInstance(ModelBoxPrecursor precursor){
        float[] posData = new float[POS_DATA_LENGTH];
        float[] texCoordData = new float[TEX_COORD_DATA_LENGTH];
        boolean[] existingFaces = getExistingFaces(precursor);
        setPosData(posData, precursor.minPosition, precursor.maxPosition);
        rotatePosData(posData, precursor.rotation, precursor.rotateAbout);
        setTextureCoords(texCoordData, precursor);
        boolean hasTranslucency = checkTranslucency(precursor);
        Vector3f[] blockTintColors = createBlockTintColorData(precursor);
        return new ModelBox(posData, texCoordData, blockTintColors, existingFaces, hasTranslucency, precursor.useShade);
    }
    
    public boolean isFullBlock(){
        return this.isFullBlock;
    }
    
    public Vector3f getTintColor(ModelBoxFace face){
        if(this.blockTintColorData == null){
            return NO_BLOCK_TINT;
        }
        return this.blockTintColorData[face.ordinal()];
    }
    
    /**
     * Determine if this ModelBox has this face.
     * @param face
     * @return 
     */
    public boolean hasFace(ModelBoxFace face){
        return this.existingFaces[face.ordinal()];
    }
    
    public boolean hasTranslucency(){
        return this.hasTranslucency;
    }
    
    public boolean getUseShade(){
        return this.useShade;
    }
    
    public Vector3f getCenter(){
        return this.center;
    }
    
    /**
     * Place the 4 points on the given face into the destination vectors.
     * The 4 points are ordered according to the right hand rule with the point
     * placed into aDest coinsiding with the min texture coordinate.
     * @param face
     * @param aDest
     * @param bDest
     * @param cDest
     * @param dDest 
     */
    public void getFacePosVerticies(ModelBoxFace face, Vector3f aDest, Vector3f bDest, Vector3f cDest, Vector3f dDest){
        aDest.x = posData[face.posVertIndexes[0] * POS_VERTICIES_SIZE + 0];
        aDest.y = posData[face.posVertIndexes[0] * POS_VERTICIES_SIZE + 1];
        aDest.z = posData[face.posVertIndexes[0] * POS_VERTICIES_SIZE + 2];
        
        bDest.x = posData[face.posVertIndexes[1] * POS_VERTICIES_SIZE + 0];
        bDest.y = posData[face.posVertIndexes[1] * POS_VERTICIES_SIZE + 1];
        bDest.z = posData[face.posVertIndexes[1] * POS_VERTICIES_SIZE + 2];
        
        cDest.x = posData[face.posVertIndexes[2] * POS_VERTICIES_SIZE + 0];
        cDest.y = posData[face.posVertIndexes[2] * POS_VERTICIES_SIZE + 1];
        cDest.z = posData[face.posVertIndexes[2] * POS_VERTICIES_SIZE + 2];
        
        dDest.x = posData[face.posVertIndexes[3] * POS_VERTICIES_SIZE + 0];
        dDest.y = posData[face.posVertIndexes[3] * POS_VERTICIES_SIZE + 1];
        dDest.z = posData[face.posVertIndexes[3] * POS_VERTICIES_SIZE + 2];
    }
    
    /**
     * Place the 4 texture coordinates into the destination vectors.
     * The 4 points are order according to the right hand rule.
     * @param face
     * @param aDest
     * @param bDest
     * @param cDest
     * @param dDest 
     */
    public void getTexCoordVerticies(ModelBoxFace face, Vector2f aDest, Vector2f bDest, Vector2f cDest, Vector2f dDest){
        aDest.x = texCoordData[face.texCoordVertIndexes[0] * TEX_COORD_VERTICIES_SIZE];
        aDest.y = texCoordData[face.texCoordVertIndexes[0] * TEX_COORD_VERTICIES_SIZE + 1];
        
        bDest.x = texCoordData[face.texCoordVertIndexes[1] * TEX_COORD_VERTICIES_SIZE];
        bDest.y = texCoordData[face.texCoordVertIndexes[1] * TEX_COORD_VERTICIES_SIZE + 1];
        
        cDest.x = texCoordData[face.texCoordVertIndexes[2] * TEX_COORD_VERTICIES_SIZE];
        cDest.y = texCoordData[face.texCoordVertIndexes[2] * TEX_COORD_VERTICIES_SIZE + 1];
        
        dDest.x = texCoordData[face.texCoordVertIndexes[3] * TEX_COORD_VERTICIES_SIZE];
        dDest.y = texCoordData[face.texCoordVertIndexes[3] * TEX_COORD_VERTICIES_SIZE + 1];
    }
    
    private static void setTextureCoords(float[] texCoordData, ModelBoxPrecursor precursor){
        MinecraftTexture texture = TextureLoader.getMinecraftDefaultTexture();
        float delta = texture.getTextureCoordDelta();
        for(ModelBoxFace face : ModelBoxFace.values()){
            if(!precursor.faceExists.get(face)){
                continue;
            }
            int texIndex = texture.getIndexFromTextureName(precursor.faceToTexName.get(face));
            Vector4f texCoords = precursor.faceToTexCoords.get(face);
            Integer numRots = precursor.faceToNumTextureRotations.get(face) % 4;
            float minX = texture.getTextureCoordX(texIndex) + texCoords.get(0) * delta;
            float maxY = texture.getTextureCoordY(texIndex) + texCoords.get(1) * delta; //minecraft json vs atomic edit tex coords. must swap Ys
            float maxX = texture.getTextureCoordX(texIndex) + texCoords.get(2) * delta;
            float minY = texture.getTextureCoordY(texIndex) + texCoords.get(3) * delta;
            //corner A
            texCoordData[face.texCoordVertIndexes[0] * TEX_COORD_VERTICIES_SIZE]     = numRots == 0 || numRots == 3 ? minX : maxX; //minX
            texCoordData[face.texCoordVertIndexes[0] * TEX_COORD_VERTICIES_SIZE + 1] = numRots == 0 || numRots == 1 ? minY : maxY; //minY
            
            //corner B
            texCoordData[face.texCoordVertIndexes[1] * TEX_COORD_VERTICIES_SIZE]     = numRots == 2 || numRots == 3 ? minX : maxX; //maxX
            texCoordData[face.texCoordVertIndexes[1] * TEX_COORD_VERTICIES_SIZE + 1] = numRots == 0 || numRots == 3 ? minY : maxY; //minY
            
            //corner C
            texCoordData[face.texCoordVertIndexes[2] * TEX_COORD_VERTICIES_SIZE]     = numRots == 1 || numRots == 2 ? minX : maxX; //maxX
            texCoordData[face.texCoordVertIndexes[2] * TEX_COORD_VERTICIES_SIZE + 1] = numRots == 2 || numRots == 3 ? minY : maxY; //maxY
            
            //corner D
            texCoordData[face.texCoordVertIndexes[3] * TEX_COORD_VERTICIES_SIZE]     = numRots == 0 || numRots == 1 ? minX : maxX; //minX
            texCoordData[face.texCoordVertIndexes[3] * TEX_COORD_VERTICIES_SIZE + 1] = numRots == 1 || numRots == 2 ? minY : maxY; //maxY
        }
    }
    
    private static boolean checkTranslucency(ModelBoxPrecursor precursor){
        for(ModelBoxFace face : ModelBoxFace.values()){
            MinecraftTexture tex = TextureLoader.getMinecraftDefaultTexture();
            int texIndex = tex.getIndexFromTextureName(precursor.faceToTexName.get(face));
            if(tex.isTextureTranslucent(texIndex)){
                return true;
            }
        }
        return false;
    }
    
    private static boolean[] getExistingFaces(ModelBoxPrecursor precursor){
        boolean[] existence = new boolean[ModelBoxFace.values().length];
        for(ModelBoxFace face : ModelBoxFace.values()){
            existence[face.ordinal()] = precursor.faceExists.get(face);
        }
        return existence;
    }
    
    private static Vector3f[] createBlockTintColorData(ModelBoxPrecursor precursor){
        boolean noTintAtAll = true;
        for(ModelBoxFace face : ModelBoxFace.values()){
            if(precursor.faceToBlockTintColor.get(face) != NO_BLOCK_TINT){
                noTintAtAll = false;
                break;
            }
        }
        if(noTintAtAll){
            return null;
        }
        Vector3f[] blockTintData = new Vector3f[ModelBoxFace.values().length];
        for(ModelBoxFace face : ModelBoxFace.values()){
            blockTintData[face.ordinal()] = precursor.faceToBlockTintColor.get(face);
        }
        return blockTintData;
    }
    
    private Vector3f calcCenter(){
        Vector3f avg = new Vector3f();
        for(int i = 0; i < NUM_POS_VERTICIES; i++){
            avg.x += posData[POS_VERTICIES_SIZE * i];
            avg.y += posData[POS_VERTICIES_SIZE * i + 1];
            avg.z += posData[POS_VERTICIES_SIZE * i + 2];
        }
        avg.x /= NUM_POS_VERTICIES;
        avg.y /= NUM_POS_VERTICIES;
        avg.z /= NUM_POS_VERTICIES;
        return avg;
    }
    
    private static void rotatePosData(float[] posData, Vector3f rotation, Vector3f rotateAbout){
        if(rotation.x == 0 && rotation.y == 0 && rotation.z == 0){
            return;
        }
        Vector3f posVec = new Vector3f();
        for(int i = 0; i < NUM_POS_VERTICIES; i++){
            posVec.x = posData[3 * i    ] - rotateAbout.x;
            posVec.y = posData[3 * i + 1] - rotateAbout.y;
            posVec.z = posData[3 * i + 2] - rotateAbout.z;
            MathUtils.rotateAllAxis(posVec, rotation);
            posData[3 * i    ] = posVec.x + rotateAbout.x;
            posData[3 * i + 1] = posVec.y + rotateAbout.y;
            posData[3 * i + 2] = posVec.z + rotateAbout.z;
        }
    }
    
    /**
     * Define the layout of the posData array.
     * A true value means use the max corner of the box for this value,
     * a false value means use the min corner of the box.
     */
    private static final boolean[] POS_DATA_LAYOUT = new boolean[]{
        false, false, false, //xyz: 000
        false, false, true,  //xyz: 001
        false, true, false,  //xyz: 010
        false, true, true,   //xyz: 011
        true, false, false,  //xyz: 100
        true, false, true,   //xyz: 101
        true, true, false,   //xyz: 110
        true, true, true,    //xyz: 111
    };
    
    private static void setPosData(float[] posData, Vector3f minPos, Vector3f maxPos){
        for(int i = 0; i < POS_DATA_LENGTH; i++){
            posData[i] = POS_DATA_LAYOUT[i] ? maxPos.get(i % 3) : minPos.get(i % 3);
        }
    }
    
    /**
     * Check if all the floats in posData are 0 or 1 within a small margin.
     * If all the floats are 0 or 1 then this block model is a full block
     * stretching from 0,0,0 to 1,1,1
     * @param posData
     * @return 
     */
    private static boolean checkFullBlock(float[] posData){
        float ALLOWED_DIFF = 0.000003f;
        for(int i = 0; i < posData.length; i++){
            //true if not 0 or 1
            if(!((posData[i] < 0 + ALLOWED_DIFF && posData[i] > 0 - ALLOWED_DIFF) || (posData[i] < 1 + ALLOWED_DIFF && posData[i] > 1 - ALLOWED_DIFF))){
                return false;
            }
        }
        return true;
    }
    
    public static enum ModelBoxFace {
        X_PLUS(
            new int[]{5, 4, 6, 7}, //xyz: 101, 100, 110, 111 
            new int[]{0, 1, 2, 3},
            new Vector3i(1, 0, 0)
        ),
        X_MINUS(
            new int[]{0, 1, 3, 2}, //xyz: 000, 001, 011, 010
            new int[]{4, 5, 6, 7},
            new Vector3i(-1, 0, 0)
        ),
        Y_PLUS(
            new int[]{3, 7, 6, 2}, //xyz: 011, 111, 110, 010
            new int[]{8, 9, 10, 11},
            new Vector3i(0, 1, 0)
        ),
        Y_MINUS(
            new int[]{0, 4, 5, 1}, //xyz: 000, 100, 101, 001
            new int[]{12, 13, 14, 15},
            new Vector3i(0, -1, 0)
        ),
        Z_PLUS(
            new int[]{1, 5, 7, 3}, //xyz: 001, 101, 111, 011
            new int[]{16, 17, 18, 19},
            new Vector3i(0, 0, 1)
        ),
        Z_MINUS(
            new int[]{4, 0, 2, 6}, //xyz: 100, 000, 010, 110
            new int[]{20, 21, 22, 23},
            new Vector3i(0, 0, -1)
        )
        ;
        
        private final int[] posVertIndexes; //follow right hand rule, first index should be the corner with the min texCoord
        private final int[] texCoordVertIndexes;
        /**
         * Default adjacent direction. block models and block states are rotated so this doesn't have much meaning. But you can iterate over adjDirections
         */
        private final Vector3i adjDirection;
        
        ModelBoxFace(int[] posVertIndexes, int[] texCoordVertIndexes, Vector3i adjDirection){
            this.posVertIndexes = posVertIndexes;
            this.texCoordVertIndexes = texCoordVertIndexes;
            this.adjDirection = adjDirection;
        }
        
        public Vector3i getAdjacentDirection(){
            return this.adjDirection;
        }
        
    }
    
}
