
package atomicedit;

import atomicedit.backend.BackendController;
import atomicedit.frontend.AtomicEditFrontEnd;
import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.jarreading.blockmodels.GlobalBlockModelDataLookup;
import atomicedit.jarreading.blockmodels_v2.BlockModelLookup;
import atomicedit.jarreading.blockstates.GlobalBlockStateDataLookup;
import atomicedit.jarreading.blockstates_v2.BlockStateModelLookup;
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
    
    private AtomicEdit(){
        initializeSettings();
        backendController = new BackendController();
        renderer = new AtomicEditRenderer();
        frontEnd = new AtomicEditFrontEnd(renderer, backendController);
    }
    
    public static AtomicEdit getInstance(){
        return INSTANCE;
    }
    
    private void initialize(){
        //GlobalBlockStateDataLookup.initialize(); //force load block state jsons
        //GlobalBlockModelDataLookup.initialize(); //force load block models
        TextureLoader.getMinecraftDefaultTexture(); //force load textures
        BlockModelLookup.initialize();
        BlockStateModelLookup.initialize();
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
