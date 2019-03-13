
package atomicedit.jarreading.blockmodels_v2;

import atomicedit.frontend.texture.MinecraftTexture;
import java.util.EnumMap;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 *
 * @author Justin Bonner
 */
class ModelBoxPrecursor {
    
    /**
     * Map a model box face to a vector4f describing the texture coords for that face on the model box.
     * The format of the vector should be: {minTexCoordX, minTexCoordY, maxTexCoordX, maxTexCoordY}
     * The texture coordinates should be from 0 to 1 inclusive.
     */
    EnumMap<ModelBox.ModelBoxFace, Vector4f> faceToTexCoords;
    EnumMap<ModelBox.ModelBoxFace, String> faceToTexName;
    EnumMap<ModelBox.ModelBoxFace, Boolean> faceExists;
    EnumMap<ModelBox.ModelBoxFace, Vector3f> faceToBlockTintColor;
    EnumMap<ModelBox.ModelBoxFace, Integer> faceToNumTextureRotations;
    Vector3f minPosition;
    Vector3f maxPosition;
    Vector3f rotation;
    Vector3f rotateAbout;
    boolean useShade;
    
    ModelBoxPrecursor(){
        faceToTexCoords = new EnumMap<>(ModelBox.ModelBoxFace.class);
        for(ModelBox.ModelBoxFace face : ModelBox.ModelBoxFace.values()){
            faceToTexCoords.put(face, new Vector4f(0,0, 1,1));
        }
        faceToTexName = new EnumMap<>(ModelBox.ModelBoxFace.class);
        for(ModelBox.ModelBoxFace face : ModelBox.ModelBoxFace.values()){
            faceToTexName.put(face, MinecraftTexture.UNKNOWN_TEXTURE_NAME);
        }
        faceExists = new EnumMap<>(ModelBox.ModelBoxFace.class);
        for(ModelBox.ModelBoxFace face : ModelBox.ModelBoxFace.values()){
            faceExists.put(face, Boolean.FALSE);
        }
        faceToBlockTintColor = new EnumMap<>(ModelBox.ModelBoxFace.class);
        for(ModelBox.ModelBoxFace face : ModelBox.ModelBoxFace.values()){
            faceToBlockTintColor.put(face, ModelBox.NO_BLOCK_TINT);
        }
        faceToNumTextureRotations = new EnumMap<>(ModelBox.ModelBoxFace.class);
        for(ModelBox.ModelBoxFace face : ModelBox.ModelBoxFace.values()){
            faceToNumTextureRotations.put(face, 0);
        }
        minPosition = new Vector3f();
        maxPosition = new Vector3f(1, 1, 1);
        rotation = new Vector3f();
        rotateAbout = new Vector3f(.5f, .5f, .5f);
        useShade = true;
    }
    
}
