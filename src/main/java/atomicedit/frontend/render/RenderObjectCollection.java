
package atomicedit.frontend.render;

import java.util.Arrays;
import java.util.List;
import org.joml.Vector3f;

/**
 * An ease of use default implementation of Renderable.
 * @author justin
 */
public class RenderObjectCollection implements Renderable{
    
    private final List<RenderObject> renderObjects;
    
    public RenderObjectCollection(RenderObject... renObjs) {
        this.renderObjects = Arrays.asList(renObjs);
    }
    
    public RenderObjectCollection(List<RenderObject> renObjs) {
        this.renderObjects = renObjs;
    }
    
    @Override
    public List<RenderObject> getRenderObjects() {
        return this.renderObjects;
    }
    
    public void updatePosition(Vector3f newPos) {
        for (RenderObject renObj : this.renderObjects) {
            renObj.updatePosition(newPos);
        }
    }
    
    public void updateRotation(Vector3f newRot) {
        for (RenderObject renObj : this.renderObjects) {
            renObj.updateRotation(newRot);
        }
    }
    
}
