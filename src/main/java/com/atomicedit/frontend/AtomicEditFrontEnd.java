
package com.atomicedit.frontend;

import com.atomicedit.backend.World;
import java.util.concurrent.locks.ReentrantLock;
import org.liquidengine.legui.animation.Animator;
import org.liquidengine.legui.component.Frame;
import org.liquidengine.legui.layout.LayoutManager;
import org.liquidengine.legui.listener.processor.EventProcessor;
import org.liquidengine.legui.system.context.CallbackKeeper;
import org.liquidengine.legui.system.context.Context;
import org.liquidengine.legui.system.context.DefaultCallbackKeeper;
import org.liquidengine.legui.system.handler.processor.SystemEventProcessor;
import org.lwjgl.opengl.GL11;


/**
 *
 * @author Justin Bonner
 */
public class AtomicEditFrontEnd {
    
    //window
    private String currentWorldPath;
    private World loadedWorld;
    private ReentrantLock worldLock;
    private AtomicEditRenderer renderer;
    private SystemEventProcessor systemEventProcessor;
    private boolean keepRunning;
    
    public AtomicEditFrontEnd(){
        this.renderer = new AtomicEditRenderer();
        this.keepRunning = false;
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
        CallbackKeeper.registerCallbacks(renderer.getWindow().getGlfwWindow(), keeper);
        keeper.getChainWindowCloseCallback().add(w -> keepRunning = false);
        systemEventProcessor = new SystemEventProcessor();
        systemEventProcessor.addDefaultCallbacks(keeper);
        System.out.println("\nGL Version: " + GL11.glGetString(GL11.GL_VERSION));
    }
    
    private void mainLoop(){
        while(keepRunning){
            renderer.render();
            systemEventProcessor.processEvents(renderer.getWindow().getFrame(), this.renderer.getContext());
            EventProcessor.getInstance().processEvents();
            LayoutManager.getInstance().layout(renderer.getWindow().getFrame());
            Animator.getInstance().runAnimations();
        }
        cleanUp();
    }
    
    private void cleanUp(){
        renderer.cleanUp();
    }
    
}
