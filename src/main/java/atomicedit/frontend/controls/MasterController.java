
package atomicedit.frontend.controls;

import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.frontend.editor.EditorSystem;
import java.nio.DoubleBuffer;
import org.joml.Vector3f;
import org.liquidengine.legui.component.Frame;
import org.liquidengine.legui.system.context.Context;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

/**
 *
 * @author Justin Bonner
 */
public class MasterController {
    
    private final Context context;
    private final Frame frame;
    private final CameraController cameraController;
    private final AtomicEditRenderer renderer;
    private boolean cameraLookAround;
    private boolean centerMouse;
    
    public MasterController(AtomicEditRenderer renderer){
        this.context = renderer.getContext();
        this.frame = renderer.getFrame();
        this.cameraController = new CameraController(renderer.getCamera());
        this.renderer = renderer;
        this.cameraLookAround = false;
        this.centerMouse = false;
    }
    
    public void handleInput(int key, int action, int mods){
        KeyControls.getKeyControlsFromKey(key).doAction(this, action, mods);
        EditorSystem.handleInput(key, action, mods);
    }
    
    public void renderUpdate(){
        if(cameraLookAround){
            cameraController.addToRotation(cameraPan());
        }
        cameraController.updateCamera();
    }
    
    private static DoubleBuffer cursorX = BufferUtils.createDoubleBuffer(1);
    private static DoubleBuffer cursorY = BufferUtils.createDoubleBuffer(1);
    private Vector3f cameraPan(){
        if(this.centerMouse){
            centerMouse();
            this.centerMouse = false;
        }
        GLFW.glfwGetCursorPos(renderer.getGlfwWindow(), cursorX, cursorY); //for some reason this seems switched
        double xPos = cursorX.get(0);
        double yPos = cursorY.get(0);
        double deltaX = xPos - renderer.getWidth() / 2;
        double deltaY = yPos - renderer.getHeight() / 2;
        centerMouse();
        return new Vector3f((float)deltaY * CameraController.ROT_SPEED, (float)deltaX * CameraController.ROT_SPEED, 0); //x and y flipped for some reason
    }
    
    private void centerMouse(){
        GLFW.glfwSetCursorPos(renderer.getGlfwWindow(), renderer.getWidth() / 2, renderer.getHeight() / 2);
    }
    
    private static enum KeyControls{
        NO_ACTION(
            -1,
            (masterController, action, mods) -> {}
        ),
        TOGGLE_LOOK_AROUND(
            GLFW.GLFW_MOUSE_BUTTON_RIGHT,
            (masterController, action, mods) -> {
                if(action == GLFW.GLFW_PRESS){
                    masterController.centerMouse = true;
                    masterController.cameraLookAround = !masterController.cameraLookAround;
                    masterController.renderer.setCursorVisible(!masterController.cameraLookAround);
                }
            }
        ),
        CAMERA_SHIFT_UP(
            GLFW.GLFW_KEY_SPACE,
            (masterController, action, mods) -> setCamDirection(masterController, CameraController.Directions.UP, action)
        ),
        CAMERA_SHIFT_DOWN(
            GLFW.GLFW_KEY_LEFT_SHIFT,
            (masterController, action, mods) -> setCamDirection(masterController, CameraController.Directions.DOWN, action)
        ),
        CAMERA_SHIFT_FORWARD(
            GLFW.GLFW_KEY_W,
            (masterController, action, mods) -> setCamDirection(masterController, CameraController.Directions.FORWARD, action)
        ),
        CAMERA_SHIFT_BACKWARD(
            GLFW.GLFW_KEY_S,
            (masterController, action, mods) -> setCamDirection(masterController, CameraController.Directions.BACKWARD, action)
        ),
        CAMERA_SHIFT_LEFT(
            GLFW.GLFW_KEY_A,
            (masterController, action, mods) -> setCamDirection(masterController, CameraController.Directions.LEFT, action)
        ),
        CAMERA_SHIFT_RIGHT(
            GLFW.GLFW_KEY_D,
            (masterController, action, mods) -> setCamDirection(masterController, CameraController.Directions.RIGHT, action)
        )
        ;
        
        private final int triggerKey;
        private final KeyPressAction pressAction;
        
        KeyControls(int key, KeyPressAction pressAction){
            this.triggerKey = key;
            this.pressAction = pressAction;
        }
        
        private interface KeyPressAction{
            void keyPress(MasterController masterController, int action, int mods);
        }
        
        public static KeyControls getKeyControlsFromKey(int key){
            for(KeyControls option : KeyControls.values()){
                if(option.triggerKey == key) return option;
            }
            return NO_ACTION;
        }
        
        public void doAction(MasterController masterController, int action, int mods){
            pressAction.keyPress(masterController, action, mods);
        }
        
    }
    
    private static void setCamDirection(MasterController controller, CameraController.Directions dir, int action){
        boolean moving = action != GLFW.GLFW_RELEASE; //true if GLFW_PRESS or GLFW_REPEAT
        controller.cameraController.setMovingDirection(dir, moving);
    }
    
}
