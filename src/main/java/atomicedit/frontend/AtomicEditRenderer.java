
package atomicedit.frontend;

import atomicedit.AtomicEdit;
import atomicedit.frontend.render.Camera;
import atomicedit.frontend.render.RenderableStage;
import atomicedit.frontend.render.shaders.UniformLayoutFormat;
import atomicedit.logging.Logger;
import atomicedit.settings.AtomicEditSettings;
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
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
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
    private final RenderableStage renderableStage;
    private Camera camera;
    private boolean isCursorVisible;
    private boolean shouldCursorBeVisible;
    
    public AtomicEditRenderer(){
        this.renderableStage = new RenderableStage();
        this.isCursorVisible = true;
        this.shouldCursorBeVisible = true;
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
        glfwWindow = GLFW.glfwCreateWindow(width, height, WINDOW_TITLE_STRING, NULL, NULL);
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
    }
    
    public void render(){
        int check;
        if((check = glGetError()) != GL_NO_ERROR){
            Logger.error("OpenGL error " + check);
        }
        glEnable(GL_CULL_FACE); //I think LEGUI turns this off
        
        if(AtomicEdit.getSettings().getSettingValueAsBoolean(AtomicEditSettings.USE_TRANSLUCENCY)){
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }
        renderableStage.destroyOldRenderObjects();
        handleSetCursorVisible();
        
        context.updateGlfwWindow();
        Vector2i windowSize = context.getFramebufferSize();
        GL11.glClearColor(0, 0, 0, 1);
        GL11.glViewport(0, 0, windowSize.x, windowSize.y);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
        
        //render world
        UniformLayoutFormat.setUniform(UniformLayoutFormat.ProgramUniforms.VIEW_MATRIX, camera.getViewMatrix());
        UniformLayoutFormat.setUniform(UniformLayoutFormat.ProgramUniforms.PROJECTION_MATRIX, camera.getProjectionMatrix());
        
        renderableStage.renderRenderables(camera);
        
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
    
    public int getWidth(){
        return this.width;
    }
    
    public int getHeight(){
        return this.height;
    }
    
    public Camera getCamera(){
        return this.camera;
    }
    
    public void setCursorVisible(boolean visible){
        this.shouldCursorBeVisible = visible;
    }
    
    private void handleSetCursorVisible(){
        boolean setVisible = this.shouldCursorBeVisible; //shouldnt need to use locks for this
        if(setVisible != this.isCursorVisible){
            GLFW.glfwSetInputMode(glfwWindow, GLFW.GLFW_CURSOR, setVisible ? GLFW.GLFW_CURSOR_NORMAL : GLFW.GLFW_CURSOR_HIDDEN);
            this.isCursorVisible = setVisible;
        }
    }
    
    public RenderableStage getRenderableStage(){
        return this.renderableStage;
    }
    
}
