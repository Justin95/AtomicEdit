
package atomicedit;

import atomicedit.backend.BackendController;
import atomicedit.frontend.AtomicEditFrontEnd;
import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.frontend.texture.TextureLoader;
import atomicedit.settings.AeSettingValues;
import atomicedit.settings.AtomicEditSettingsCreator;


/**
 *
 * @author Justin Bonner
 */
public class AtomicEdit {
    
    private AtomicEditFrontEnd frontEnd;
    private static AeSettingValues settings;
    private static BackendController backendController;
    private AtomicEditRenderer renderer;
    
    public AtomicEdit(){
        initializeSettings();
        backendController = new BackendController();
        renderer = new AtomicEditRenderer();
        frontEnd = new AtomicEditFrontEnd(renderer, backendController);
    }
    
    private void initialize(){
        TextureLoader.getMinecraftDefaultTexture(); //force load textures
    }
    
    public void run(){
        initialize();
        frontEnd.run();
    }
    
    public static AeSettingValues getSettings(){
        return settings;
    }
    
    public static void initializeSettings(){
        settings = AtomicEditSettingsCreator.createSettings();
    }
    
    public static BackendController getBackendController(){
        return backendController;
    }
    
}
