
package atomicedit.jarreading.blockstates_v2;

import atomicedit.AtomicEdit;
import atomicedit.frontend.utils.LoadingUtils;
import atomicedit.logging.Logger;
import atomicedit.settings.AtomicEditSettings;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

/**
 *
 * @author Justin Bonner
 */
public class BlockStateJsonLoader {
    
    private static final String INTERNAL_PATH = "assets/minecraft/blockstates/";
    private static final String INTERNAL_EXT = ".json";
    
    /**
     * Loads the block state jsons from 'assets/minecraft/blockstates/'.
     * @return a map where the name of a block state is mapped to the json string
     * associated with it.
     */
    public static Map<String, String> loadBlockStateJsons(){
        Map<String, String> blockNameToJson = new HashMap<>();
        String jarFilePath = LoadingUtils.getNewestMinecraftJarFilePath(AtomicEdit.getSettings().getSettingValueAsString(AtomicEditSettings.MINECRAFT_INSTALL_LOCATION));
        Logger.info("Getting block states from minecraft version: " + jarFilePath);
        try(ZipFile jarFile = new ZipFile(jarFilePath)){
            jarFile.stream().filter(
                (entry) -> entry.getName().startsWith(INTERNAL_PATH) && entry.getName().endsWith(INTERNAL_EXT)
            ).forEach((jsonEntry) -> {
                String name = "minecraft:" + jsonEntry.getName().substring(INTERNAL_PATH.length(), jsonEntry.getName().length() - INTERNAL_EXT.length()); //remove extension
                Logger.info("Loading minecraft block state: " + name);
                String blockStateJson;
                try{
                    blockStateJson = LoadingUtils.readInputStream(jarFile.getInputStream(jsonEntry));
                    blockNameToJson.put(name, blockStateJson);
                }catch(IOException e){
                    Logger.warning("Could not read block state file: " + jsonEntry.getName(), e);
                }
            });
        }catch(IOException e){
            Logger.error("Failed to finish reading block states", e);
        }
        return blockNameToJson;
    }
    
}
