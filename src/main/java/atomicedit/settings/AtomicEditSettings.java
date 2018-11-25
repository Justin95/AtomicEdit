
package atomicedit.settings;

import java.io.File;
import javax.swing.JFileChooser;

/**
 *
 * @author Justin Bonner
 */
public enum AtomicEditSettings {
    
    MINECRAFT_INSTALL_LOCATION(
        "Minecraft Install Directory",
        "minecraft_install_directory",
        SettingDataType.FILE_LOCATION,
        () -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setName("Locate .minecraft folder");
            int status;
            File choice;
            do{
                status = fileChooser.showOpenDialog(null);
                choice = fileChooser.getSelectedFile();
            }while(status != JFileChooser.APPROVE_OPTION && !".minecraft".equals(choice.getName()));
            return choice.getPath();
        }
    )
    ;
    
    private static final String HOME_DIR = System.getProperty("user.home");
    public static final String ATOMIC_EDIT_INSTALL_PATH = HOME_DIR + (HOME_DIR.endsWith("/") ? "" : "/") + ".AtomicEdit";
    public static final String SETTINGS_FILEPATH = ATOMIC_EDIT_INSTALL_PATH + "/atomicEditSettings.json";
    
    public final String DISPLAY_NAME;
    public final String SETTING_ID;
    private final SettingDataType DATATYPE;
    private final DefaultValueCreator DEFAULT_VALUE_CREATOR;
    
    AtomicEditSettings(
        String displayName,
        String settingId,
        SettingDataType dataType,
        DefaultValueCreator defaultValueCreator
    ){
        this.DISPLAY_NAME = displayName;
        this.SETTING_ID = settingId;
        this.DATATYPE = dataType;
        this.DEFAULT_VALUE_CREATOR = defaultValueCreator;
    }
    
    SettingDataType getDataType(){
        return this.DATATYPE;
    }
    
    Object createDefaultValue(){
        Object value = this.DEFAULT_VALUE_CREATOR.createDefaultValue();
        if(!isCorrectType(value)){
            throw new AssertionError("Wrong return type in Settings default value creator!");
        }
        return value;
    }
    
    private interface DefaultValueCreator {
        public Object createDefaultValue();
    }
    
    public Object createValueFromString(String valueString){
        return valueString; //TODO create each type when more settings are needed
    }
    
    public boolean isCorrectType(Object value){
        if(value != null){
            if(!this.DATATYPE.BASE_TYPE.isAssignableFrom(value.getClass())){
                return false;
            }
        }
        return true;
    }
    
    public String createValueId(Object value){
        return value.toString(); //TODO create each type when more settings are added
    }
    
    enum SettingDataType{
        STRING(String.class),
        BOOLEAN(Boolean.class),
        INT(Integer.class),
        CLASS_OPTION(SettingSelectableClass.class),
        FILE_LOCATION(String.class)
        ;
        
        public final Class BASE_TYPE;
        
        SettingDataType(Class type){
            this.BASE_TYPE = type;
        }
        
    }
    
    
}
