
package atomicedit.frontend.editor;

import atomicedit.logging.Logger;
import atomicedit.utils.MathUtils;
import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 *
 * @author Justin Bonner
 */
public class EditorPointer {
    
    public static final float MAX_POINTER_DISTANCE = 250;
    public static final float MIN_POINTER_DISTANCE = 1;
    
    private Vector3i selectorPoint;
    private float selectorDistanceFromCamera;
    
    public EditorPointer(){
        this.selectorDistanceFromCamera = 10;
    }
    
    /**
     * Update the position that this editor pointer points to. This
     * should only ever be called by one thread. By convention this
     * should only be called in the rendering thread. This ensures 
     * the needed thread safety without the need for locks.
     * @param cameraPos
     * @param cameraRot
     * @param changeInDistance 
     */
    public void updatePosition(Vector3f cameraPos, Vector3f cameraRot, float changeInDistance){
        Vector3f facingDir = MathUtils.rotationVectorToDirectionVector(cameraRot);
        float distance = this.selectorDistanceFromCamera + changeInDistance;
        if(distance < MIN_POINTER_DISTANCE){
            distance = MIN_POINTER_DISTANCE;
        }else if(distance > MAX_POINTER_DISTANCE){
            distance = MAX_POINTER_DISTANCE;
        }
        this.selectorDistanceFromCamera = distance;
        facingDir.mul(this.selectorDistanceFromCamera);
        this.selectorPoint = new Vector3i(Math.round(cameraPos.x + facingDir.x), Math.round(cameraPos.y + facingDir.y), Math.round(cameraPos.z + facingDir.z));
    }
    
    public Vector3i getSelectorPoint(){
        if(selectorPoint == null){
            Logger.warning("Cannot get selector point before setting it.");
        }
        return this.selectorPoint; //don't need to lock on selector point. if it gets updated at the same time we can work with old or new point
    }
    
}
