
package atomicedit.settings;

import atomicedit.logging.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;

/**
 *
 * @author Justin Bonner
 */
public class AtomicEditSettingsCreator {
    
    public static AeSettingValues createSettings(){
        File settingsFile = new File(AtomicEditSettings.SETTINGS_FILEPATH);
        if(settingsFile.exists()){
            try{
                return readSettings(settingsFile);
            }catch(Exception e){
                Logger.notice("Unable to read settings because: " + e.getLocalizedMessage());
            }
        }
        AeSettingValues settings = createDefaultSettings();
        try{
            writeSettingsFile(settings, settingsFile);
        }catch(Exception e){
            Logger.notice("Unable to write settings file because: " + e.getMessage());
        }
        return settings;
    }
    
    private static AeSettingValues readSettings(File file) throws Exception{
        StringBuilder settingsJson = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        reader.lines().forEach((String line) -> settingsJson.append(line).append("\n"));
        reader.close();
        return parseSettings(settingsJson.toString());
    }
    
    private static AeSettingValues parseSettings(String settingJson){
        AeSettingValues settings = new AeSettingValues();
        JsonObject root = new JsonParser().parse(settingJson).getAsJsonObject();
        for(AtomicEditSettings setting : AtomicEditSettings.values()){
            if(root.has(setting.SETTING_ID)){
                settings.setSetting(setting, setting.createValueFromString(root.get(setting.SETTING_ID).getAsString()));
            }else{
                settings.setSetting(setting, setting.createDefaultValue());
            }
        }
        return settings;
    }
    
    private static AeSettingValues createDefaultSettings(){
        AeSettingValues settings = new AeSettingValues();
        for(AtomicEditSettings setting : AtomicEditSettings.values()){
            settings.setSetting(setting, setting.createDefaultValue());
        }
        return settings;
    }
    
    private static void writeSettingsFile(AeSettingValues settings, File settingsFile) throws IOException{
        String settingsJson = createJson(settings);
        Logger.info(settingsJson);
        if(!new File(AtomicEditSettings.ATOMIC_EDIT_INSTALL_PATH).exists()){
            new File(AtomicEditSettings.ATOMIC_EDIT_INSTALL_PATH).mkdir();
        }
        settingsFile.createNewFile();//create new file if it doesnt exist
        BufferedWriter writer = new BufferedWriter(new FileWriter(settingsFile));
        writer.append(settingsJson);
        writer.flush();
        writer.close();
    }
    
    private static String createJson(AeSettingValues settings){
        JsonObject root = new JsonObject();
        settings.getSettingsStream().forEach((Entry<AtomicEditSettings, Object> settingAndValue) -> {
            AtomicEditSettings setting = settingAndValue.getKey();
            Object value = settingAndValue.getValue();
            root.addProperty(setting.SETTING_ID, setting.createValueId(value));
        });
        return root.toString();
    }
    
}
