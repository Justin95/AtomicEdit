
package atomicedit.jarreading.blockstates;

import atomicedit.AtomicEdit;
import atomicedit.frontend.utils.LoadingUtils;
import atomicedit.logging.Logger;
import atomicedit.settings.AtomicEditSettings;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

/**
 *
 * @author Justin Bonner
 */
public class BlockStateLoader {
    
    private static Map<String, List<BlockStateDataPrecursor>> loadedBlockNameToBlockStateDataPossibilities;
    private static final Object LOADER_LOCK = new Object();
    
    public static Map<String, List<BlockStateDataPrecursor>> getBlockNameToBlockStateDataPossibilities(){
        if(loadedBlockNameToBlockStateDataPossibilities != null){
            return loadedBlockNameToBlockStateDataPossibilities;
        }
        synchronized(LOADER_LOCK){
            if(loadedBlockNameToBlockStateDataPossibilities != null){ //have this check twice, first time to avoid sync everytime, second time to avoid concurrency issue
                return loadedBlockNameToBlockStateDataPossibilities;
            }
            loadedBlockNameToBlockStateDataPossibilities = loadBlockStates();
            return loadedBlockNameToBlockStateDataPossibilities;
        }
    }
    
    
    private static final String INTERNAL_PATH = "assets/minecraft/blockstates/";
    private static final String INTERNAL_EXT = ".json";
    private static Map<String, List<BlockStateDataPrecursor>> loadBlockStates(){
        String jarFilePath = LoadingUtils.getNewestMinecraftJarFilePath(AtomicEdit.getSettings().getSettingValueAsString(AtomicEditSettings.MINECRAFT_INSTALL_LOCATION));
        Logger.info("Getting block states from minecraft version: " + jarFilePath);
        Map<String, List<BlockStateDataPrecursor>> blockNameToPossibilities = new HashMap<>();
        try{
            ZipFile jarFile = new ZipFile(jarFilePath);
            jarFile.stream().filter(
                (entry) -> entry.getName().startsWith(INTERNAL_PATH) && entry.getName().endsWith(INTERNAL_EXT)
            ).forEach((jsonEntry) -> {
                String name = "minecraft:" + jsonEntry.getName().substring(INTERNAL_PATH.length(), jsonEntry.getName().length() - INTERNAL_EXT.length()); //remove extension
                Logger.info("Loading minecraft block state: " + name);
                String blockStateJson;
                try{
                    blockStateJson = LoadingUtils.readInputStream(jarFile.getInputStream(jsonEntry));
                    List<BlockStateDataPrecursor> possibilities = BlockStateDataParser.parseJson(blockStateJson);
                    blockNameToPossibilities.put(name, possibilities);
                }catch(IOException e){
                    Logger.warning("Could not read block state file: " + jsonEntry.getName(), e);
                }
            });
        }catch(IOException e){
            Logger.error("Failed to finish reading block states", e);
        }
        return blockNameToPossibilities;
    }
    
}
