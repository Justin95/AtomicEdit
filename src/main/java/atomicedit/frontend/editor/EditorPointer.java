
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
    
    public static final float MAX_POINTER_DISTANCE = 200;
    public static final float MIN_POINTER_DISTANCE = 3;
    
    private Vector3i selectorPoint;
    private Vector3f prevCameraPos;
    private Vector3f prevCameraRot;
    private float selectorDistanceFromCamera;
    
    //box selection data
    private Vector3i pointA;
    private Vector3i pointB;
    private boolean currentlyDrawingBox;
    
    public EditorPointer(){
        this.selectorDistanceFromCamera = 10;
        this.prevCameraPos = new Vector3f(0, 0, 0);
        this.prevCameraRot = new Vector3f(0, 0, 0);
        this.pointA = null;
        this.pointB = null;
        this.currentlyDrawingBox = false;
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
        if (cameraPos != null) {
            this.prevCameraPos = cameraPos;
        } else {
            cameraPos = this.prevCameraPos;
        }
        if (cameraRot != null) {
            this.prevCameraRot = cameraRot;
        } else {
            cameraRot = prevCameraRot;
        }
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
    
    public void clickUpdate() {
        if(pointA != null && pointB != null){ //already have full box
            pointA = null; //clear existing box
            pointB = null;
        }
        if(!currentlyDrawingBox){
            pointA = this.getSelectorPoint();
        }else{
            pointB = this.getSelectorPoint();
        }
        currentlyDrawingBox = !currentlyDrawingBox;
    }
    
    public boolean getCurrentlyDrawingBox() {
        return this.currentlyDrawingBox;
    }
    
    public void setCurrentlyDrawingBox(boolean currDrawing) {
        this.currentlyDrawingBox = currDrawing;
    }

    public Vector3i getPointA() {
        return pointA;
    }

    public void setPointA(Vector3i pointA) {
        this.pointA = pointA;
    }

    public Vector3i getPointB() {
        return pointB;
    }

    public void setPointB(Vector3i pointB) {
        this.pointB = pointB;
    }
    
}
