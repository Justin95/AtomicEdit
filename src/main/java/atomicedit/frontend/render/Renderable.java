
package atomicedit.frontend.render;

import java.util.Collection;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public interface Renderable {
    
    
    public Collection<RenderObject> getRenderObjects();
    
    default void updatePosition(Vector3f pos) {
        for (RenderObject obj : getRenderObjects()) {
            obj.updatePosition(pos);
        }
    }
    
    default void updateRotation(Vector3f rot) {
        for (RenderObject obj : getRenderObjects()) {
            obj.updateRotation(rot);
        }
    }
    
    default void updatePositionAndRotation(Vector3f pos, Vector3f rot) {
        for (RenderObject obj : getRenderObjects()) {
            obj.updatePositionAndRotation(pos, rot);
        }
    }
    
}
