
package atomicedit.frontend;

import atomicedit.backend.BackendController;
import atomicedit.logging.Logger;
import org.liquidengine.legui.animation.Animator;
import org.liquidengine.legui.layout.LayoutManager;
import org.liquidengine.legui.listener.processor.EventProcessor;
import org.liquidengine.legui.system.context.CallbackKeeper;
import org.liquidengine.legui.system.context.DefaultCallbackKeeper;
import org.liquidengine.legui.system.handler.processor.SystemEventProcessor;
import org.lwjgl.opengl.GL11;


/**
 *
 * @author Justin Bonner
 */
public class AtomicEditFrontEnd {
    
    //window
    private BackendController backendController;
    private AtomicEditRenderer renderer;
    private SystemEventProcessor systemEventProcessor;
    private boolean keepRunning;
    
    public AtomicEditFrontEnd(BackendController backendController){
        this.renderer = new AtomicEditRenderer();
        this.keepRunning = false;
        this.backendController = backendController;
    }
    
    
    public void run(){
        initialize();
        mainLoop();
        cleanUp();
    }
    
    private void initialize(){
        this.keepRunning = true;
        renderer.initialize();
        this.systemEventProcessor = new SystemEventProcessor();
        
        CallbackKeeper keeper = new DefaultCallbackKeeper();
        CallbackKeeper.registerCallbacks(renderer.getGlfwWindow(), keeper);
        keeper.getChainWindowCloseCallback().add(w -> keepRunning = false);
        systemEventProcessor = new SystemEventProcessor();
        systemEventProcessor.addDefaultCallbacks(keeper);
        Logger.info("\nGL Version: " + GL11.glGetString(GL11.GL_VERSION));
    }
    
    private void mainLoop(){
        while(keepRunning){
            renderer.render();
            systemEventProcessor.processEvents(renderer.getFrame(), this.renderer.getContext());
            EventProcessor.getInstance().processEvents();
            LayoutManager.getInstance().layout(renderer.getFrame());
            Animator.getInstance().runAnimations();
        }
    }
    
    private void cleanUp(){
        renderer.cleanUp();
    }
    
}
