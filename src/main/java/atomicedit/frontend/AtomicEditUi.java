package atomicedit.frontend;

import atomicedit.backend.BackendController;
import atomicedit.frontend.ui.AtomicEditGui;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.internal.ImGuiContext;

/**
 *
 * @author justin
 */
public class AtomicEditUi {
    
    private static ImGuiImplGlfw imGuiGlfw;
    private static ImGuiImplGl3 imGuiGl3;
    private static ImGuiIO imGuiIo;
    
    
    public static void initialize(AtomicEditRenderer renderer) {
        final long glfwWindow = renderer.getGlfwWindow();
        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGl3 = new ImGuiImplGl3();
        ImGuiContext context = ImGui.createContext();
        ImGui.setCurrentContext(context);
        imGuiGlfw.init(glfwWindow, true);
        imGuiGl3.init(AtomicEditRenderer.GLSL_VERSION);
        
        //add fonts here later
        
        imGuiIo = ImGui.getIO();
        imGuiIo.setConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        imGuiIo.setIniFilename(null);
        imGuiIo.setWantSaveIniSettings(false);
        //https://github.com/SpaiR/imgui-java/blob/main/imgui-app/src/main/java/imgui/app/Window.java#L116
        
        ImGui.styleColorsDark();
        
    }
    
    public static void updateUi(AtomicEditRenderer renderer, BackendController backendController) {
        imGuiGl3.newFrame();
        imGuiGlfw.newFrame();
        ImGui.newFrame();
        
        //Do UI work
        AtomicEditGui.uiUpdate(renderer, backendController);
        
        //Draw UI
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }
    
    
    public static void shutdown() {
        imGuiGl3.shutdown();
        imGuiGlfw.shutdown();
        ImGui.destroyContext();
    }
    
    public static boolean isUiFocused() {
        return imGuiIo.getWantCaptureKeyboard() || imGuiIo.getWantCaptureMouse();
    }
    
}
