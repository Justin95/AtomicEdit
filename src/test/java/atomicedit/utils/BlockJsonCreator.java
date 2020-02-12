
package atomicedit.utils;

import atomicedit.backend.BackendController;
import atomicedit.backend.BlockState;
import atomicedit.backend.GlobalBlockStateMap;
import atomicedit.settings.AeSettingValues;
import atomicedit.settings.AtomicEditSettings;
import atomicedit.settings.AtomicEditSettingsCreator;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public class BlockJsonCreator {
    
    private static final String DEBUG_WORLD_NAME = "Debug World";
    
    /**
     * This is not a test. This creates a default block info json.
     */
    public void createDefaultBlockInfoJson(){
        AeSettingValues settings = AtomicEditSettingsCreator.createSettings();
        BackendController backend = new BackendController();
        String worldPath = settings.getSettingValueAsString(AtomicEditSettings.MINECRAFT_INSTALL_LOCATION) + "/saves/" + DEBUG_WORLD_NAME;
        backend.setWorld(worldPath);
        //need to load relevant chunks still
        List<BlockState> loadedBlockStates = GlobalBlockStateMap.getBlockTypes();
    }
    
}
