
package atomicedit.jarreading.blockmodels;

import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class TexturedBox {
    
    private static final int NUM_SIDES = 6;
    private static final float NUM_BOX_UNITS_IN_BLOCK = 16f;
    public final Vector3f smallPos;
    public final Vector3f largePos;
    private final String[] textures;
    
    TexturedBox(TexturedBoxPrecursor precursor){
        this.smallPos = new Vector3f(precursor.smallX / NUM_BOX_UNITS_IN_BLOCK, precursor.smallY / NUM_BOX_UNITS_IN_BLOCK, precursor.smallZ / NUM_BOX_UNITS_IN_BLOCK);
        this.largePos = new Vector3f(precursor.largeX / NUM_BOX_UNITS_IN_BLOCK, precursor.largeY / NUM_BOX_UNITS_IN_BLOCK, precursor.largeZ / NUM_BOX_UNITS_IN_BLOCK);
        this.textures = new String[NUM_SIDES];
        for(BoxFace face : BoxFace.values()){
            textures[face.getIndex()] = face.getTextureName(precursor);
        }
    }
    
    public String getTextureName(BoxFace face){
        return textures[face.getIndex()];
    }
    
    
    public static enum BoxFace{
        /**
         * Y plus
         */
        UP(
            (precursor) -> precursor.yPlusTexName
        ),
        /**
         * Y minus
         */
        DOWN(
            (precursor) -> precursor.yMinusTexName
        ),
        /**
         * X plus
         */
        EAST(
            (precursor) -> precursor.xPlusTexName
        ),
        /**
         * X minus
         */
        WEST(
            (precursor) -> precursor.xMinusTexName
        ),
        /**
         * Z minus
         */
        NORTH(
            (precursor) -> precursor.zMinusTexName
        ),
        /**
         * Z plus
         */
        SOUTH(
            (precursor) -> precursor.zPlusTexName
        )
        ;
        
        private final TextureGetter texGetter;
        
        BoxFace(TextureGetter texGetter){
            this.texGetter = texGetter;
        }
        
        private int getIndex(){
            return this.ordinal();
        }
        
        private String getTextureName(TexturedBoxPrecursor precursor){
            return this.texGetter.getTexture(precursor);
        }
        
        private interface TextureGetter{
            public String getTexture(TexturedBoxPrecursor precursor);
        }
        
    }
    
}
