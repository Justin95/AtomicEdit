
package atomicedit;

import atomicedit.backend.BackendController;
import atomicedit.backend.BlockState;
import atomicedit.backend.GcThread;
import atomicedit.backend.LegacyBlockIdMap;
import atomicedit.frontend.AtomicEditFrontEnd;
import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.jarreading.blockmodels.BlockModelLookup;
import atomicedit.jarreading.blockstates.BlockStateModelLookup;
import atomicedit.jarreading.texture.TextureLoader;
import atomicedit.logging.Logger;
import atomicedit.settings.AeSettingValues;
import atomicedit.settings.AtomicEditSettingsCreator;
import atomicedit.utils.VersionUtils;
import org.liquidengine.legui.style.font.FontRegistry;
import org.liquidengine.legui.theme.Themes;


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
    
    private AtomicEdit() {
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
        Logger.info("AtomicEdit version: " + VersionUtils.getCurrentVersion());
        FontRegistry.setDefaultFont(FontRegistry.ROBOTO_REGULAR);
        //Themes.setDefaultTheme(Themes.FLAT_DARK);
        BlockState.loadKnownBlockStates();
        TextureLoader.getMinecraftDefaultTexture(); //force load textures
        BlockModelLookup.initialize();
        BlockStateModelLookup.initialize();
        BlockState.postModelLoadingInitialization();
        LegacyBlockIdMap.initialize();
        gcThread.start();
    }
    
    private void cleanUp() {
        gcThread.shutdown();
        try {
            gcThread.join();
        } catch (InterruptedException e) {
            //pass
        }
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
