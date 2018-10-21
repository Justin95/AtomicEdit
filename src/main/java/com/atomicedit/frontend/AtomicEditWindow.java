
package com.atomicedit.frontend;

import org.liquidengine.legui.component.Frame;
import org.liquidengine.legui.system.context.Context;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 *
 * @author Justin Bonner
 */
public class AtomicEditWindow {
    
    private static final int WIDTH = 600;
    private static final int HEIGHT = 500;
    
    private long glfwWindow;
    private Frame frame;
    
    
    
    
    public void initialize(){
        glfwWindow = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "Atomic Edit", NULL, NULL);
        GLFW.glfwShowWindow(glfwWindow);
        GLFW.glfwMakeContextCurrent(glfwWindow);
        GL.createCapabilities();
        GLFW.glfwSwapInterval(0);
        frame = new Frame(WIDTH, HEIGHT);
    }
    
    public void render(Context context){
        
    }
    
    public long getGlfwWindow(){
        return this.glfwWindow;
    }
    
    public Frame getFrame(){
        return this.frame;
    }
    
}
