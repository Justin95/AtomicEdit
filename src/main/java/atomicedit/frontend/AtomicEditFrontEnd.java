
package atomicedit.frontend;

import atomicedit.backend.BackendController;
import atomicedit.frontend.controls.MasterController;
import atomicedit.frontend.editor.EditorSystem;
import atomicedit.frontend.ui.AtomicEditGui;
import atomicedit.frontend.worldmaintinance.ChunkLoadingThread;
import atomicedit.logging.Logger;
import org.liquidengine.legui.animation.AnimatorProvider;
import org.liquidengine.legui.listener.processor.EventProcessor;
import org.liquidengine.legui.system.context.CallbackKeeper;
import org.liquidengine.legui.system.context.DefaultCallbackKeeper;
import org.liquidengine.legui.system.handler.processor.SystemEventProcessor;
import org.liquidengine.legui.system.layout.LayoutManager;
import org.lwjgl.opengl.GL11;


/**
 *
 * @author Justin Bonner
 */
public class AtomicEditFrontEnd {
    
    //window
    private final BackendController backendController;
    private final AtomicEditRenderer renderer;
    private SystemEventProcessor systemEventProcessor;
    private final ChunkLoadingThread chunkLoadingThread;
    private MasterController masterController;
    private boolean keepRunning;
    
    public AtomicEditFrontEnd(AtomicEditRenderer renderer, BackendController backendController){
        this.renderer = renderer;
        this.keepRunning = false;
        this.backendController = backendController;
        chunkLoadingThread = new ChunkLoadingThread(renderer);
    }
    
    
    public void run(){
        initialize();
        mainLoop();
        cleanUp();
    }
    
    private void initialize(){
        this.keepRunning = true;
        renderer.initialize();
        this.masterController = new MasterController(renderer);
        chunkLoadingThread.start();
        this.systemEventProcessor = new SystemEventProcessor();
        EditorSystem.initialize(renderer); //editor system must be initialized before gui
        AtomicEditGui.initializeGui(renderer.getFrame(), renderer.getContext(), backendController, renderer);
        
        CallbackKeeper keeper = new DefaultCallbackKeeper();
        CallbackKeeper.registerCallbacks(renderer.getGlfwWindow(), keeper);
        keeper.getChainWindowCloseCallback().add(w -> keepRunning = false);
        keeper.getChainMouseButtonCallback().add((window, button, action, mods) -> {
            masterController.handleInput(isUiFocused(renderer), button, action, mods);
        });
        keeper.getChainKeyCallback().add((long window, int key, int scancode, int action, int mods) -> {
            masterController.handleInput(isUiFocused(renderer), key, action, mods);
        });
        systemEventProcessor = new SystemEventProcessor();
        systemEventProcessor.addDefaultCallbacks(keeper);
        Logger.info("\nGL Version: " + GL11.glGetString(GL11.GL_VERSION));
    }
    
    private static boolean isUiFocused(AtomicEditRenderer renderer){
        return renderer.getContext().getMouseTargetGui() != null; //is mouse over gui?
        /*
        Component focusedGui = renderer.getContext().getFocusedGui();
        return !(
               focusedGui == null 
            || focusedGui == renderer.getFrame().getContainer()
            );*/
    }
    
    private void mainLoop(){
        while(keepRunning){
            renderer.render();
            systemEventProcessor.processEvents(renderer.getFrame(), this.renderer.getContext());
            masterController.renderUpdate();
            EventProcessor.getInstance().processEvents();
            LayoutManager.getInstance().layout(renderer.getFrame());
            AnimatorProvider.getAnimator().runAnimations();
            AtomicEditGui.updateGui(renderer);
        }
    }
    
    private void cleanUp(){
        chunkLoadingThread.cleanUp();
        renderer.cleanUp();
    }
    
}
