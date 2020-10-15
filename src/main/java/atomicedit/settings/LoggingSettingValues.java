
package atomicedit.settings;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 * @author Justin Bonner
 */
public class LoggingSettingValues {
    
    private final EnumMap<LoggingSettings, Object> settingsValues;
    
    LoggingSettingValues() {
        settingsValues = new EnumMap<>(LoggingSettings.class);
    }
    
    public void setSetting(LoggingSettings setting, Object value){
        this.settingsValues.put(setting, value);
    }
    
    public Stream<Map.Entry<LoggingSettings, Object>> getSettingsStream(){
        return settingsValues.entrySet().stream();
    }
    
    public String getSettingValueAsString(LoggingSettings setting){
        if(setting == null){
            throw new NullPointerException("Cannot look up null setting");
        }
        String value = (String) settingsValues.get(setting);
        if(value == null){
            value = (String) setting.createDefaultValue();
            settingsValues.put(setting, value);
        }
        return value;
    }
    
    public boolean getSettingValueAsBoolean(LoggingSettings setting){
        if(setting == null){
            throw new NullPointerException("Cannot look up null setting");
        }
        Boolean value = (Boolean) settingsValues.get(setting);
        if(value == null){
            value = (Boolean) setting.createDefaultValue();
            settingsValues.put(setting, value);
        }
        return value;
    }
    
}
