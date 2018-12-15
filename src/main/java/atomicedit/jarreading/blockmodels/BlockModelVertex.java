
package atomicedit.jarreading.blockmodels;

import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class BlockModelVertex {
    
    private final Vector3f pos;
    private final Vector2f texCoord;
    
    BlockModelVertex(Vector3f pos, Vector2f texCoord){
        this.pos = pos;
        this.texCoord = texCoord;
    }
    
    public Vector3f getPosition(){
        return this.pos;
    }
    
    public Vector2f getTexCoord(){
        return this.texCoord;
    }
    
}
