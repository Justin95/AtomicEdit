
package atomicedit.frontend;

import atomicedit.backend.BackendController;
import atomicedit.frontend.controls.MasterController;
import atomicedit.frontend.editor.EditorSystem;
import atomicedit.frontend.worldmaintinance.ChunkLoadingThread;
import atomicedit.logging.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;


/**
 *
 * @author Justin Bonner
 */
public class AtomicEditFrontEnd {
    
    //window
    private final BackendController backendController;
    private final AtomicEditRenderer renderer;
    private final ChunkLoadingThread chunkLoadingThread;
    private MasterController masterController;
    private boolean keepRunning;
    
    public AtomicEditFrontEnd(AtomicEditRenderer renderer, BackendController backendController){
        this.renderer = renderer;
        this.keepRunning = false;
        this.backendController = backendController;
        chunkLoadingThread = new ChunkLoadingThread(renderer);
    }
    
    
    public void run() {
        initialize();
        mainLoop();
        cleanUp();
    }
    
    private void initialize(){
        this.keepRunning = true;
        renderer.initialize();
        this.masterController = new MasterController(renderer);
        chunkLoadingThread.start();
        EditorSystem.initialize(renderer); //editor system must be initialized before gui
        
        //set up GLFW callbacks
        long glfwWindow = renderer.getGlfwWindow();
        
        GLFW.glfwSetMouseButtonCallback(
            glfwWindow, 
            (window, button, action, mods) -> {
                masterController.handleInput(AtomicEditUi.isUiFocused(), button, action, mods);
            }
        );
        GLFW.glfwSetKeyCallback(
            glfwWindow,
            (long window, int key, int scancode, int action, int mods) -> {
                masterController.handleInput(AtomicEditUi.isUiFocused(), key, action, mods);
            }
        );
        GLFW.glfwSetScrollCallback(
            glfwWindow,
            (long window, double xScroll, double yScroll) -> {
                masterController.handleScrollInput(AtomicEditUi.isUiFocused(), yScroll);
            }
        );
        GLFW.glfwSetWindowCloseCallback(
            glfwWindow,
            w -> keepRunning = false
        );
        
        //Initialize UI after setting callbacks
        AtomicEditUi.initialize(renderer);
        
        Logger.info(
            "OpenGL INFO:"
            + "\nGL Renderer: " + GL11.glGetString(GL11.GL_RENDERER)
            + "\nGL Vendor:   " + GL11.glGetString(GL11.GL_VENDOR)
            + "\nGL Version:  " + GL11.glGetString(GL11.GL_VERSION)
            + "\nGL Shader Version: " + GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION)
        );
    }
    
    private void mainLoop(){
        while(keepRunning){
            renderer.pollInput();
            masterController.renderUpdate();
            
            renderer.render();
            
            // render frame / GUI
            AtomicEditUi.updateUi(renderer, backendController);

            EditorSystem.renderTick();
            
            renderer.swapBuffers();
            renderer.sleep();
        }
    }
    
    private void cleanUp(){
        EditorSystem.cleanUp();
        chunkLoadingThread.shutdown();
        try {
            chunkLoadingThread.join();
        } catch (InterruptedException e) {
            //pass
        }
        renderer.cleanUp();
    }
    
}
