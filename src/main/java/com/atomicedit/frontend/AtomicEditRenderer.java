
package com.atomicedit.frontend;

import org.joml.Vector2i;
import org.liquidengine.legui.system.context.Context;
import org.liquidengine.legui.system.renderer.Renderer;
import org.liquidengine.legui.system.renderer.nvg.NvgRenderer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Justin Bonner
 */
public class AtomicEditRenderer {
    
    
    
    private Context context;
    private AtomicEditWindow atomicEditWindow;
    private Renderer renderer;
    
    //https://github.com/LiquidEngine/legui/blob/develop/src/main/java/org/liquidengine/legui/demo/SingleClassExample.java
    public void initialize(){
        System.setProperty("joml.nounsafe", Boolean.TRUE.toString());
        System.setProperty("java.awt.headless", Boolean.TRUE.toString());
        if (!GLFW.glfwInit()) {
            throw new RuntimeException("Can't initialize GLFW");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1);
        atomicEditWindow = new AtomicEditWindow();
        atomicEditWindow.initialize();
        context = new Context(atomicEditWindow.getGlfwWindow());
        renderer = new NvgRenderer();
        renderer.initialize();
    }
    
    public void render(){
        context.updateGlfwWindow();
        Vector2i windowSize = context.getFramebufferSize();
        GL11.glClearColor(1, 1, 1, 1);
        GL11.glViewport(0, 0, windowSize.x, windowSize.y);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

        // render frame
        renderer.render(atomicEditWindow.getFrame(), context);

        // poll events to callbacks
        GLFW.glfwPollEvents();
        GLFW.glfwSwapBuffers(atomicEditWindow.getGlfwWindow());
        
    }
    
    public void cleanUp(){
        renderer.destroy();
        GLFW.glfwDestroyWindow(atomicEditWindow.getGlfwWindow());
        GLFW.glfwTerminate();
    }
    
    public AtomicEditWindow getWindow(){
        return this.atomicEditWindow;
    }
    
    public Context getContext(){
        return this.context;
    }
    
}
