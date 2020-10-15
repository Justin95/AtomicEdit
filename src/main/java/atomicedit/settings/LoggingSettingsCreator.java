
package atomicedit.settings;

import atomicedit.logging.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class LoggingSettingsCreator {
    
    public static LoggingSettingValues createSettings(){
        File settingsFile = new File(AtomicEditConstants.LOGGING_SETTINGS_FILEPATH);
        if(settingsFile.exists()){
            try{
                LoggingSettingValues settings = readSettings(settingsFile);
                return settings;
            }catch(Exception e){
                Logger.notice("Unable to read settings because: " + e.getLocalizedMessage());
            }
        }
        LoggingSettingValues settings = createDefaultSettings();
        try{
            writeSettingsFile(settings, settingsFile);
        }catch(Exception e){
            Logger.notice("Unable to write settings file because: " + e.getMessage());
        }
        return settings;
    }
    
    private static LoggingSettingValues readSettings(File file) throws Exception{
        StringBuilder settingsJson = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.lines().forEach((String line) -> settingsJson.append(line).append("\n"));
        }
        return parseSettings(settingsJson.toString());
    }
    
    private static LoggingSettingValues parseSettings(String settingJson){
        LoggingSettingValues settings = new LoggingSettingValues();
        JsonObject root = new JsonParser().parse(settingJson).getAsJsonObject();
        for(LoggingSettings setting : LoggingSettings.values()){
            if(root.has(setting.settingId)){
                settings.setSetting(setting, setting.parseFromJson(root.get(setting.settingId)));
            }else{
                settings.setSetting(setting, setting.createDefaultValue());
            }
        }
        return settings;
    }
    
    private static LoggingSettingValues createDefaultSettings(){
        LoggingSettingValues settings = new LoggingSettingValues();
        for(LoggingSettings setting : LoggingSettings.values()){
            settings.setSetting(setting, setting.createDefaultValue());
        }
        return settings;
    }
    
    private static void writeSettingsFile(LoggingSettingValues settings, File settingsFile) throws IOException {
        String settingsJson = createJson(settings);
        if(!new File(AtomicEditConstants.ATOMIC_EDIT_INSTALL_PATH).exists()){
            new File(AtomicEditConstants.ATOMIC_EDIT_INSTALL_PATH).mkdir();
        }
        settingsFile.createNewFile();//create new file if it doesnt exist
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(settingsFile))) {
            writer.append(settingsJson);
            writer.flush();
        }
    }
    
    private static String createJson(LoggingSettingValues settings){
        JsonObject root = new JsonObject();
        settings.getSettingsStream().forEach((Map.Entry<LoggingSettings, Object> settingAndValue) -> {
            LoggingSettings setting = settingAndValue.getKey();
            Object value = settingAndValue.getValue();
            root.add(setting.settingId, setting.parseToJson(value));
        });
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(root);
    }
    
}
