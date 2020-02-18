
package atomicedit;

import atomicedit.backend.BackendController;
import atomicedit.backend.BlockState;
import atomicedit.backend.GcThread;
import atomicedit.frontend.AtomicEditFrontEnd;
import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.jarreading.blockmodels.BlockModelLookup;
import atomicedit.jarreading.blockstates.BlockStateModelLookup;
import atomicedit.jarreading.texture.TextureLoader;
import atomicedit.settings.AeSettingValues;
import atomicedit.settings.AtomicEditSettingsCreator;


/**
 *
 * @author Justin Bonner
 */
public class AtomicEdit {
    
    private static final AtomicEdit INSTANCE = new AtomicEdit();
    
    private AtomicEditFrontEnd frontEnd;
    private static AeSettingValues settings;
    private static BackendController backendController;
    private AtomicEditRenderer renderer;
    private final GcThread gcThread;
    
    private AtomicEdit(){
        initializeSettings();
        backendController = new BackendController();
        renderer = new AtomicEditRenderer();
        frontEnd = new AtomicEditFrontEnd(renderer, backendController);
        gcThread = new GcThread();
    }
    
    public static AtomicEdit getInstance(){
        return INSTANCE;
    }
    
    private void initialize(){
        BlockState.loadKnownBlockStates();
        TextureLoader.getMinecraftDefaultTexture(); //force load textures
        BlockModelLookup.initialize();
        BlockStateModelLookup.initialize();
        gcThread.start();
    }
    
    public void cleanUp() {
        gcThread.shutdown();
    }
    
    public void run(){
        initialize();
        frontEnd.run();
        cleanUp();
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
