
package atomicedit.jarreading.blockmodels;

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
public class BlockModelJsonLoader {
    
    private static final String INTERNAL_PATH = "assets/minecraft/models/block/";
    private static final String INTERNAL_EXT = ".json";
    public static Map<String, String> loadBlockModelJsonMap(){
        Map<String, String> blockModeMap = new HashMap<>();
        String jarFilePath = LoadingUtils.getNewestMinecraftJarFilePath(AtomicEdit.getSettings().getSettingValueAsString(AtomicEditSettings.MINECRAFT_INSTALL_LOCATION));
        Logger.info("Getting block models from minecraft version: " + jarFilePath);
        try{
            ZipFile jarFile = new ZipFile(jarFilePath);
            jarFile.stream().filter(
                (entry) -> entry.getName().startsWith(INTERNAL_PATH) && entry.getName().endsWith(INTERNAL_EXT)
            ).forEach((jsonEntry) -> {
                String modelName = "block/" + jsonEntry.getName().substring(INTERNAL_PATH.length(), jsonEntry.getName().length() - INTERNAL_EXT.length());
                Logger.info("Loading minecraft model: " + modelName);
                String modelJson;
                try{
                    modelJson = LoadingUtils.readInputStream(jarFile.getInputStream(jsonEntry));
                }catch(IOException e){
                    Logger.warning("Unable to read block model json: " + modelName, e);
                    modelJson = null;
                }
                blockModeMap.put(modelName, modelJson);
            });
        }catch(IOException e){
            Logger.warning("Exception while loading block models", e);
        }
        return blockModeMap;
    }
    
    
}
