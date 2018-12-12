
package atomicedit.settings;

import atomicedit.frontend.render.blockmodelcreation.BlockModelCreator1_13Logic;
import atomicedit.logging.Logger;
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
        null,
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
        null,
        () -> 9
    ),
    BLOCK_MODEL_CREATOR(
        "Block Model Creator",
        "block_model_creator",
        SettingDataType.CLASS_OPTION,
        new ClassInstanceOption[]{
            new ClassInstanceOption(BlockModelCreator1_13Logic.getInstance())
        },
        () -> BlockModelCreator1_13Logic.getInstance()
    )
    ;
    
    private static final String HOME_DIR = System.getProperty("user.home");
    public static final String ATOMIC_EDIT_INSTALL_PATH = HOME_DIR + (HOME_DIR.endsWith("/") ? "" : "/") + ".AtomicEdit";
    public static final String SETTINGS_FILEPATH = ATOMIC_EDIT_INSTALL_PATH + "/atomicEditSettings.json";
    
    public final String DISPLAY_NAME;
    public final String SETTING_ID;
    private final SettingDataType DATATYPE;
    private final SettingOption[] OPTIONS;
    private final DefaultValueCreator DEFAULT_VALUE_CREATOR;
    
    AtomicEditSettings(
        String displayName,
        String settingId,
        SettingDataType dataType,
        SettingOption[] options,
        DefaultValueCreator defaultValueCreator
    ){
        this.DISPLAY_NAME = displayName;
        this.SETTING_ID = settingId;
        this.DATATYPE = dataType;
        this.OPTIONS = options;
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
        CLASS_OPTION(
            SettingSelectableClass.class,
            (setting, strValue) -> {
                for(int i = 0; i < setting.OPTIONS.length; i++){
                    SettingSelectableClass classInstance = (SettingSelectableClass)setting.OPTIONS[i].getValue();
                    if(classInstance.getIdentifierString().equals(strValue)){
                        return classInstance;
                    }
                }
                Logger.warning("Could not recognize option " + strValue + " for setting " + setting.DISPLAY_NAME + " chosing default");
                return setting.createDefaultValue();
            },
            (value) -> ((SettingSelectableClass)value).getIdentifierString()
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
