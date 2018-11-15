
package com.atomicedit.frontend;

import com.atomicedit.frontend.render.Camera;
import com.atomicedit.frontend.render.RenderObject;
import com.atomicedit.frontend.render.Renderable;
import com.atomicedit.frontend.render.shaders.UniformLayoutFormat;
import com.atomicedit.frontend.texture.TextureLoader;
import com.atomicedit.logging.Logger;
import java.util.ArrayList;
import java.util.Collection;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.liquidengine.legui.component.Frame;
import org.liquidengine.legui.system.context.Context;
import org.liquidengine.legui.system.renderer.Renderer;
import org.liquidengine.legui.system.renderer.nvg.NvgRenderer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 *
 * @author Justin Bonner
 */
public class AtomicEditRenderer {
    
    
    
    private Context context;
    private Renderer guiRenderer;
    private long glfwWindow;
    private Frame frame;
    private int width;
    private int height;
    private final Collection<Renderable> renderables;
    private Camera camera;
    
    public AtomicEditRenderer(){
        this.renderables = new ArrayList<>();
    }
    
    //https://github.com/LiquidEngine/legui/blob/develop/src/main/java/org/liquidengine/legui/demo/SingleClassExample.java
    public void initialize(){
        System.setProperty("joml.nounsafe", Boolean.TRUE.toString());
        System.setProperty("java.awt.headless", Boolean.TRUE.toString());
        if (!GLFW.glfwInit()) {
            throw new RuntimeException("Can't initialize GLFW");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        this.width = videoMode.width() - 6;
        this.height = videoMode.height() - 80;
        this.camera = new Camera(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 90, width / height);
        glfwWindow = GLFW.glfwCreateWindow(width, height, "Atomic Edit", NULL, NULL);
        GLFW.glfwShowWindow(glfwWindow);
        GLFW.glfwMakeContextCurrent(glfwWindow);
        GLFW.glfwFocusWindow(glfwWindow);
        GL.createCapabilities();
        GLFW.glfwSwapInterval(0);
        frame = new Frame(width, height);
        context = new Context(glfwWindow);
        guiRenderer = new NvgRenderer();
        guiRenderer.initialize();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        /*ArrayList<RenderObject> test = new ArrayList<>();
        test.add(new RenderObject(new Vector3f(0, 0, -5), new Vector3f(0,0,0), TextureLoader.getMinecraftDefaultTexture(), new float[]{
            0,0,0, 0,0, .5f,.5f,.5f,.9f,
            1,0,0, 1,0, .9f,.5f,.5f,1,
            1,1,0, 1,1, .7f,.2f,.3f,1
        }, new short[]{
            0,1,2,2,1,0
        }));
        renderables.add(() -> test);*/
    }
    
    public void render(){
        int check;
        if((check = glGetError()) != GL_NO_ERROR){
            Logger.error("OpenGL error " + check);
        }
        context.updateGlfwWindow();
        Vector2i windowSize = context.getFramebufferSize();
        GL11.glClearColor(0, 0, 0, 1);
        GL11.glViewport(0, 0, windowSize.x, windowSize.y);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
        
        //render world
        UniformLayoutFormat.setUniform(UniformLayoutFormat.ProgramUniforms.VIEW_MATRIX, camera.getViewMatrix());
        UniformLayoutFormat.setUniform(UniformLayoutFormat.ProgramUniforms.PROJECTION_MATRIX, camera.getProjectionMatrix());
        synchronized(renderables){
            for(Renderable renderable : renderables){
                for(RenderObject renderObject : renderable.getRenderObjects()){
                    renderObject.render();
                }
            }
        }
        
        // render frame / GUI
        guiRenderer.render(frame, context);

        // poll events to callbacks
        GLFW.glfwPollEvents();
        GLFW.glfwSwapBuffers(glfwWindow);
        
    }
    
    public void cleanUp(){
        guiRenderer.destroy();
        GLFW.glfwDestroyWindow(glfwWindow);
        GLFW.glfwTerminate();
    }
    
    public long getGlfwWindow(){
        return this.glfwWindow;
    }
    
    public Frame getFrame(){
        return this.frame;
    }
    
    public Context getContext(){
        return this.context;
    }
    
    public void addRenderables(Collection<Renderable> additions){
        synchronized(renderables){
            renderables.addAll(additions);
        }
    }
    
    public void removeRenderables(Collection<Renderable> removals){
        synchronized(renderables){
            renderables.removeAll(removals);
        }
    }
    
}
