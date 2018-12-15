
package atomicedit.frontend.controls;

import atomicedit.frontend.render.Camera;
import java.util.EnumMap;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

/**
 *
 * @author Justin Bonner
 */
public class CameraController {
    
    private static final float SPEED = .4f / 16;
    static final float ROT_SPEED = 0.1f;
    
    
    private final Camera camera;
    private final EnumMap<Directions, Boolean> activeDirections;
    private long lastUpdateTime;
    
    public CameraController(Camera camera){
        if(camera == null) throw new IllegalArgumentException("Camera must not be null in CameraController");
        this.camera = camera;
        this.activeDirections = new EnumMap<>(Directions.class);
        for(Directions dir : Directions.values()){
            activeDirections.put(dir, false);
        }
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    void setMovingDirection(Directions dir, boolean movingThisDir){
        if(dir == null) return;
        activeDirections.put(dir, movingThisDir);
    }
    
    public void updateCamera(){
        long currTime = System.currentTimeMillis();
        long deltaTime = currTime - lastUpdateTime;
        lastUpdateTime = currTime;
        Vector3f directionVector = calcDirectionVector();
        Vector3f newPos = camera.getPosition().add(directionVector.mul(SPEED * deltaTime));
        this.camera.setPosition(newPos);
    }
    
    
    public void addToRotation(Vector3f addRot){
        Vector3f cameraRot = camera.getRotation();
        cameraRot.add(addRot);
        cameraRot.x = cameraRot.x % 360;
        cameraRot.y = cameraRot.y % 360;
        cameraRot.z = cameraRot.z % 360;
    }
    
    private Vector3f calcDirectionVector(){
        Vector3f dirVector = new Vector3f(0,0,0); //in degrees
        Vector3f rot = new Vector3f(camera.getRotation()).mul(2); //dont know why but the rotation angles appear to be half of what they should be to match the visual camera rotation
        for(Directions dir : Directions.values()){
            if(activeDirections.get(dir)){
                dirVector.add(dir.rotateVector(new Vector3f(dir.DIRECTION), rot));
            }
        }
        return dirVector;
    }
    
    static enum Directions {
        UP(
            new Vector3f(0,1,0),
            (toRotate, rotateBy) -> toRotate.rotateZ(-(float)Math.toRadians(rotateBy.z))
        ),
        DOWN(
            new Vector3f(0,-1,0),
            (toRotate, rotateBy) -> toRotate.rotateZ(-(float)Math.toRadians(rotateBy.z))
        ),
        FORWARD(
            new Vector3f(0,0,-1),
            (toRotate, rotateBy) -> toRotate.rotateY(-(float)Math.toRadians(rotateBy.y))
        ),
        BACKWARD(
            new Vector3f(0,0,1),
            (toRotate, rotateBy) -> toRotate.rotateY(-(float)Math.toRadians(rotateBy.y))
        ),
        LEFT(
            new Vector3f(-1,0,0),
            (toRotate, rotateBy) -> toRotate.rotateY(-(float)Math.toRadians(rotateBy.y))
        ),
        RIGHT(
            new Vector3f(1,0,0),
            (toRotate, rotateBy) -> toRotate.rotateY(-(float)Math.toRadians(rotateBy.y))
        )
        ;
        
        public final Vector3f DIRECTION;
        public final Rotator rotator;
        
        Directions(Vector3f dir, Rotator rotator){
            this.DIRECTION = dir;
            this.rotator = rotator;
        }
        
        
        private interface Rotator{
            Vector3f rotateVector(Vector3f toRotate, Vector3f rotateBy);
        }
        
        Vector3f rotateVector(Vector3f toRotate, Vector3f rotateBy){
            return this.rotator.rotateVector(toRotate, rotateBy);
        }
        
    }
    
}
