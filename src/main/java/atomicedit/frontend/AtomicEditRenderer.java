
package atomicedit.frontend;

import atomicedit.AtomicEdit;
import atomicedit.frontend.render.Camera;
import atomicedit.frontend.render.RenderableStage;
import atomicedit.frontend.render.shaders.UniformLayoutFormat;
import atomicedit.logging.Logger;
import atomicedit.settings.AtomicEditSettings;
import atomicedit.utils.VersionUtils;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
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
    
    private static final String WINDOW_TITLE_STRING = "Atomic Edit " + VersionUtils.getCurrentVersion();
    private static final int GL_MAJOR_VERSION = 3;
    private static final int GL_MINOR_VERSION = 3;
    public static final String GLSL_VERSION = "#version 330";
    
    private long glfwWindow;
    private int width;
    private int height;
    private final RenderableStage renderableStage;
    private Camera camera;
    private boolean isCursorVisible;
    private boolean shouldCursorBeVisible;
    private int targetFps;
    private long frameStartTime;
    
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
        
        int[] monXPosBuf = new int[1];
        int[] monYPosBuf = new int[1];
        int[] monWidthBuf = new int[1];
        int[] monHeightBuf = new int[1];
        GLFW.glfwGetMonitorWorkarea(GLFW.glfwGetPrimaryMonitor(), monXPosBuf, monYPosBuf, monWidthBuf, monHeightBuf);
        this.targetFps = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()).refreshRate();
        this.frameStartTime = System.currentTimeMillis();
        this.width = monWidthBuf[0];
        this.height = monHeightBuf[0];
        this.camera = new Camera(new Vector3f(0, 80, 0), new Vector3f(0, 0, 0), 90, width / (float)height);
        //glfwWindow = GLFW.glfwCreateWindow(width, height, WINDOW_TITLE_STRING, GLFW.glfwGetPrimaryMonitor(), NULL); //boarderless window
        glfwWindow = GLFW.glfwCreateWindow(width, height, WINDOW_TITLE_STRING, NULL, NULL);
        GLFW.glfwShowWindow(glfwWindow);
        GLFW.glfwMakeContextCurrent(glfwWindow);
        GLFW.glfwFocusWindow(glfwWindow);
        GLFW.glfwSetWindowPos(glfwWindow, monXPosBuf[0], monYPosBuf[0]);
        GL.createCapabilities();
        GLFW.glfwSwapInterval(0);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }
    
    public void render(){
        int check;
        if((check = glGetError()) != GL_NO_ERROR){
            Logger.error("OpenGL error " + check);
        }
        
        //enable translucency
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        handleSetCursorVisible();
        
        int[] widthBuf = new int[1];
        int[] heightBuf = new int[1];
        GLFW.glfwGetWindowSize(glfwWindow, widthBuf, heightBuf);
        this.width = widthBuf[0];
        this.height = heightBuf[0];
        camera.setAspectRatio(width / (float)height);
        GL11.glClearColor(0f, 0f, 0f, 1);
        GL11.glViewport(0, 0, width, height);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
        
        //render world
        UniformLayoutFormat.setUniform(UniformLayoutFormat.ProgramUniforms.VIEW_MATRIX, camera.getViewMatrix());
        UniformLayoutFormat.setUniform(UniformLayoutFormat.ProgramUniforms.PROJECTION_MATRIX, camera.getProjectionMatrix());
        
        renderableStage.housekeeping();
        renderableStage.renderRenderables(camera);
    }
    
    public void pollInput() {
        // poll events to callbacks
        try {
            GLFW.glfwPollEvents();
        } catch (Exception e) {
            Logger.error("Exception Polling Events.", e); //if an exception is thrown in callbacks
        }
    }
    
    public void swapBuffers() {
        GLFW.glfwSwapBuffers(glfwWindow);
    }
    
    public void sleep() {
        if (!AtomicEdit.getSettings().getSettingValueAsBoolean(AtomicEditSettings.FRAME_RATE_LIMIT)) {
            return;
        }
        float targetDelayMs = 1000f / targetFps;
        long frameEndTime = System.currentTimeMillis();
        long frameTime = frameEndTime - frameStartTime;
        float sleepTime = targetDelayMs - frameTime;
        if (sleepTime > 1) {
            try {
                Thread.sleep((long) sleepTime); //truncate any fractions of a ms
            } catch (InterruptedException e) {

            }
        }
        frameStartTime = System.currentTimeMillis();
    }
    
    public void cleanUp(){
        GLFW.glfwDestroyWindow(glfwWindow);
        GLFW.glfwTerminate();
    }
    
    public long getGlfwWindow(){
        return this.glfwWindow;
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
        if(setVisible != this.isCursorVisible) {
            GLFW.glfwSetInputMode(glfwWindow, GLFW.GLFW_CURSOR, setVisible ? GLFW.GLFW_CURSOR_NORMAL : GLFW.GLFW_CURSOR_DISABLED);
            this.isCursorVisible = setVisible;
        }
    }
    
    public RenderableStage getRenderableStage(){
        return this.renderableStage;
    }
    
}
