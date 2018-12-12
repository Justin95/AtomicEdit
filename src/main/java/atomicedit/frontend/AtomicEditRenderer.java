
package atomicedit.frontend;

import atomicedit.frontend.render.Camera;
import atomicedit.frontend.render.RenderObject;
import atomicedit.frontend.render.Renderable;
import atomicedit.frontend.render.shaders.UniformLayoutFormat;
import atomicedit.logging.Logger;
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
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 *
 * @author Justin Bonner
 */
public class AtomicEditRenderer {
    
    private static final String WINDOW_TITLE_STRING = "Atomic Edit";
    private static final int GL_MAJOR_VERSION = 3;
    private static final int GL_MINOR_VERSION = 3;
    
    private Context context;
    private Renderer guiRenderer;
    private long glfwWindow;
    private Frame frame;
    private int width;
    private int height;
    private final Collection<Renderable> renderables;
    private final Collection<Renderable> toAddRenderables;
    private final Collection<Renderable> toRemoveRenderables;
    private Camera camera;
    
    public AtomicEditRenderer(){
        this.renderables = new ArrayList<>();
        this.toAddRenderables = new ArrayList<>();
        this.toRemoveRenderables = new ArrayList<>();
    }
    
    //https://github.com/LiquidEngine/legui/blob/develop/src/main/java/org/liquidengine/legui/demo/SingleClassExample.java
    public void initialize(){
        //System.setProperty("joml.nounsafe", Boolean.TRUE.toString());
        //System.setProperty("java.awt.headless", Boolean.TRUE.toString());
        if (!GLFW.glfwInit()) {
            throw new RuntimeException("Can't initialize GLFW");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, GL_MAJOR_VERSION);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, GL_MINOR_VERSION);
        GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        this.width = videoMode.width();
        this.height = videoMode.height();
        this.camera = new Camera(new Vector3f(0, 80, 0), new Vector3f(0, 0, 0), 90, width / (float)height);
        //glfwWindow = GLFW.glfwCreateWindow(width, height, WINDOW_TITLE_STRING, GLFW.glfwGetPrimaryMonitor(), NULL); //boarderless window
        glfwWindow = GLFW.glfwCreateWindow(width - 2, height - 30, WINDOW_TITLE_STRING, NULL, NULL);
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
        //glCullFace(GL_BACK); //nessessary?
    }
    
    public void render(){
        int check;
        if((check = glGetError()) != GL_NO_ERROR){
            Logger.error("OpenGL error " + check);
        }
        
        handleRenderableRemovals();
        handleRenderableAdditions();
        
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
    
    public Camera getCamera(){
        return this.camera;
    }
    
    public void addRenderables(Collection<Renderable> additions){
        synchronized(renderables){
            synchronized(toAddRenderables){
                toAddRenderables.addAll(additions);
            }
        }
    }
    
    public void removeRenderables(Collection<Renderable> removals){
        synchronized(renderables){
            synchronized(toRemoveRenderables){
                toRemoveRenderables.addAll(removals);
            }
        }
    }
    
    private void handleRenderableAdditions(){
        synchronized(renderables){
            synchronized(toAddRenderables){
                if(toAddRenderables.isEmpty()) return;
                renderables.addAll(toAddRenderables);
                toAddRenderables.clear();
            }
        }
    }
    
    private void handleRenderableRemovals(){
        synchronized(renderables){
            synchronized(toRemoveRenderables){
                if(toRemoveRenderables.isEmpty()) return;
                for(Renderable renderable : toRemoveRenderables){
                    renderable.getRenderObjects().forEach((renderObject) -> renderObject.destroy());
                }
                renderables.removeAll(toRemoveRenderables);
                toRemoveRenderables.clear();
            }
        }
    }
    
}
