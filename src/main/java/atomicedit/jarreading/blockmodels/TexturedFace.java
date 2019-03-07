
package atomicedit.jarreading.blockmodels;

import atomicedit.frontend.texture.MinecraftTexture;
import atomicedit.jarreading.texture.TextureLoader;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class TexturedFace {
    
    private static final Vector3f RED_TINT = new Vector3f(1f, .2f, .2f);
    private static final Vector3f GREEN_TINT = new Vector3f(.3f, .9f, .3f); //may need adjustment
    private static final Vector3f BLUE_TINT = new Vector3f(.3f,.3f,1f);
    private static final Vector3f NO_TINT = new Vector3f(1f, 1f, 1f);
    
    private final Vector2f texCoordsMin;
    private final Vector2f texCoordsMax;
    private final Vector3f tint;
    private final boolean isTranslucent;
    
    public TexturedFace(TexturedFacePrecursor precursor){
        this.tint = calcTint(precursor.tintIndex, precursor.textureName);
        MinecraftTexture texture = TextureLoader.getMinecraftDefaultTexture();
        this.isTranslucent = texture.isTextureTranslucent(texture.getIndexFromTextureName(precursor.textureName));
        float pixelDelta = 1f / (texture.getBlockTextureLength() * MinecraftTexture.TEXTURE_RES);
        int textureIndex = texture.getIndexFromTextureName(precursor.textureName);
        float baseMinX = texture.getTextureCoordX(textureIndex);
        float baseMinY = texture.getTextureCoordY(textureIndex);
        float minX = baseMinX + (precursor.texCoordsMin.x * pixelDelta);
        float minY = baseMinY + (precursor.texCoordsMin.y * pixelDelta);
        float maxX = baseMinX + (precursor.texCoordsMax.x * pixelDelta);
        float maxY = baseMinY + (precursor.texCoordsMax.y * pixelDelta);
        this.texCoordsMin = new Vector2f(minX, maxY); //yes max and min y are supposed to be flipped
        this.texCoordsMax = new Vector2f(maxX, minY);
    }
    
    private static Vector3f calcTint(int tintIndex, String textureName){
        switch(tintIndex){
            case 0:
                if(textureName.contains("red")){ //yes really, block tints are hardcoded in MC and not included in .json model files
                    return RED_TINT;
                }
                return GREEN_TINT;
            case 1:
                return BLUE_TINT;
            default:
                return NO_TINT;
        }
    }
    
    public boolean isTranslucent(){
        return this.isTranslucent;
    }
    
    public Vector2f getTexCoordsMin(){
        return this.texCoordsMin;
    }
    
    public Vector2f getTexCoordsMax(){
        return this.texCoordsMax;
    }
    
    public Vector3f getTint(){
        return this.tint;
    }
    
}
