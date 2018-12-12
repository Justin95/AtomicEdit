
package atomicedit.frontend.render;

import java.util.EnumMap;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

/**
 *
 * @author Justin Bonner
 */
public class CameraController {
    
    private static final float SPEED = .4f / 16;
    
    
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
    
    public void handleInput(int key, int action){
        Directions dir = Directions.getDirectionFromKey(key);
        if(dir == null) return;
        if(action == GLFW.GLFW_PRESS){
            activeDirections.put(dir, true);
        }else if(action == GLFW.GLFW_RELEASE){
            activeDirections.put(dir, false);
        }
    }
    
    public void updateCamera(){
        long currTime = System.currentTimeMillis();
        long deltaTime = currTime - lastUpdateTime;
        lastUpdateTime = currTime;
        Vector3f directionVector = calcDirectionVector();
        Vector3f newPos = camera.getPosition().add(directionVector.mul(SPEED * deltaTime));
        this.camera.setPosition(newPos);
    }
    
    private Vector3f calcDirectionVector(){
        Vector3f dirVector = new Vector3f(0,0,0);
        for(Directions dir : Directions.values()){
            if(activeDirections.get(dir)){
                dirVector = dirVector.add(dir.DIRECTION);
            }
        }
        return dirVector;
    }
    
    private enum Directions {
        UP(
            GLFW.GLFW_KEY_SPACE,
            new Vector3f(0,1,0)
        ),
        DOWN(
            GLFW.GLFW_KEY_LEFT_SHIFT,
            new Vector3f(0,-1,0)
        ),
        FORWARD(
            GLFW.GLFW_KEY_W,
            new Vector3f(0,0,-1)
        ),
        BACKWARD(
            GLFW.GLFW_KEY_S,
            new Vector3f(0,0,1)
        ),
        LEFT(
            GLFW.GLFW_KEY_A,
            new Vector3f(-1,0,0)
        ),
        RIGHT(
            GLFW.GLFW_KEY_D,
            new Vector3f(1,0,0)
        )
        ;
        
        public final int KEY;
        public final Vector3f DIRECTION;
        
        Directions(int key, Vector3f dir){
            this.KEY = key;
            this.DIRECTION = dir;
        }
        
        static Directions getDirectionFromKey(int key){
            for(Directions dir : Directions.values()){
                if(dir.KEY == key) return dir;
            }
            return null;
        }
        
        
        
    }
    
}
