
package atomicedit.settings;

import atomicedit.logging.Logger;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.stream.Stream;

/**
 *
 * @author Justin Bonner
 */
public class AeSettingValues {
    
    private final EnumMap<AtomicEditSettings, Object> settingsValues;
    
    AeSettingValues(){
        settingsValues = new EnumMap<>(AtomicEditSettings.class);
    }
    
    public void setSetting(AtomicEditSettings setting, Object value){
        Logger.info("Setting setting value: (" + setting.DISPLAY_NAME + ", " + value.toString() + ")");
        if(!setting.isCorrectType(value)){
            throw new IllegalArgumentException("Tried to set setting of wrong type (" + setting.DISPLAY_NAME + ", " + value.toString() + ")");
        }
        this.settingsValues.put(setting, value);
    }
    
    public Stream<Entry<AtomicEditSettings, Object>> getSettingsStream(){
        return settingsValues.entrySet().stream();
    }
    
    public String getSettingValueAsString(AtomicEditSettings setting){
        if(setting == null){
            throw new NullPointerException("Cannot look up null setting");
        }
        if(setting.getDataType().BASE_TYPE != String.class){
            throw new IllegalArgumentException("The given setting is not a String");
        }
        String value = (String) settingsValues.get(setting);
        if(value == null){
            value = (String) setting.createDefaultValue();
        }
        return value;
    }
    
    public int getSettingValueAsInt(AtomicEditSettings setting){
        if(setting == null){
            throw new NullPointerException("Cannot look up null setting");
        }
        if(setting.getDataType().BASE_TYPE != Integer.class){
            throw new IllegalArgumentException("The given setting is not a String");
        }
        Integer value = (Integer) settingsValues.get(setting);
        if(value == null){
            value = (Integer) setting.createDefaultValue();
        }
        return value;
    }
    
    //add other datatypes as needed
    
}
