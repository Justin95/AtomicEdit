
package atomicedit.jarreading.blockmodels;

import atomicedit.frontend.texture.MinecraftTexture;
import org.joml.Vector2f;

/**
 *
 * @author Justin Bonner
 */
public class FacePrecursor {
    
    String textureName;
    Vector2f texCoordsMin;
    Vector2f texCoordsMax;
    int tintIndex;
    
    FacePrecursor(){
        textureName = MinecraftTexture.UNKNOWN_TEXTURE_NAME;
        texCoordsMin = new Vector2f(0,0);
        texCoordsMax = new Vector2f(MinecraftTexture.TEXTURE_RES, MinecraftTexture.TEXTURE_RES);
        tintIndex = -1;
    }
    
    
    FacePrecursor copy(){
        FacePrecursor other = new FacePrecursor();
        other.textureName = this.textureName;
        other.texCoordsMin = new Vector2f(this.texCoordsMin);
        other.texCoordsMax = new Vector2f(this.texCoordsMax);
        other.tintIndex = this.tintIndex;
        return other;
    }
    
}
