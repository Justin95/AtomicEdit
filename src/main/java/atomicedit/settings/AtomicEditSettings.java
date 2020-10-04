
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
            fileChooser.setDialogTitle("Locate .minecraft folder");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int status;
            File choice;
            do{
                status = fileChooser.showOpenDialog(null);
                choice = fileChooser.getSelectedFile();
            }while(status != JFileChooser.APPROVE_OPTION && !".minecraft".equals(choice.getName()));
            return choice.getPath();
        }
    ),
    CHUNK_RENDER_DISTANCE(
        "Render Distance",
        "chunk_render_distance",
        SettingDataType.INT,
        () -> 9
    ),
    PREFERED_MINECRAFT_VERSION(
        "Prefered Minecraft Version",
        "prefered_minecraft_version",
        SettingDataType.STRING,
        () -> "latest"
    ),
    WRITE_LOGS_TO_FILE(
        "Write Logs to File",
        "write_logs_to_file",
        SettingDataType.BOOLEAN,
        () -> false //no need to write a megabyte log file every program execution
    ),
    LOGGING_LEVEL(
        "Logging Level",
        "logging_level",
        SettingDataType.STRING,
        /*
        new SettingOption[] {
            "debug",
            "info",
            "notice",
            "warning",
            "error",
            "critical"
        },*/
        () -> "info"
    ),
    /* This setting is unnessesary it remains only as an example for how to set up a class choice setting.
    BLOCK_MODEL_CREATOR(
        "Block Model Creator",
        "block_model_creator",
        SettingDataType.CLASS_OPTION,
        new ClassInstanceOption[]{
            new ClassInstanceOption(BlockModelCreator1_13Logic.getInstance())
        },
        () -> BlockModelCreator1_13Logic.getInstance()
    )
    */
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
        return this.getDataType().getValueFromString(this, valueString);
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
        if(!isCorrectType(value)){
            throw new IllegalArgumentException("Tried to crate a value string for the wrong type");
        }
        return this.DATATYPE.storeAsString(value);
    }
    
    enum SettingDataType{
        STRING(
            String.class,
            (setting, strValue) -> strValue,
            (value) -> (String) value
        ),
        BOOLEAN(
            Boolean.class,
            (setting, strValue) -> Boolean.valueOf(strValue),
            (value) -> value.toString()
        ),
        INT(
            Integer.class,
            (setting, strValue) -> Integer.valueOf(strValue),
            (value) -> value.toString()
        ),
        FILE_LOCATION(
            String.class,
            (setting, strValue) -> strValue,
            (value) -> (String) value
        )
        ;
        
        public final Class BASE_TYPE;
        private final ValueFromStringCreator valueCreator;
        private final ValueToString valueToString;
        
        SettingDataType(Class type, ValueFromStringCreator valueCreator, ValueToString valueToString){
            this.BASE_TYPE = type;
            this.valueCreator = valueCreator;
            this.valueToString = valueToString;
        }
        
        String storeAsString(Object value){
            return valueToString.valueToString(value);
        }
        
        interface ValueToString{
            String valueToString(Object value);
        }
        
        interface ValueFromStringCreator{
            public Object getValueFromString(AtomicEditSettings setting, String stringValue);
        }
        
        Object getValueFromString(AtomicEditSettings setting, String stringValue){
            return valueCreator.getValueFromString(setting, stringValue);
        }
        
    }
    
    
}
