
package atomicedit.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * These settings need to be separate from the main settings because loading regular settings makes logging calls
 * and initializing the logger needs to read settings.
 * @author Justin Bonner
 */
public enum LoggingSettings {
    WRITE_LOGS_TO_FILE(
        "Write Logs to File",
        "write_logs_to_file",
        () -> false, //no need to write a megabyte log file every program execution
        (json) -> json.getAsBoolean(),
        (value) -> new JsonPrimitive((Boolean)value)
    ),
    LOGGING_LEVEL(
        "Logging Level",
        "logging_level",
        /*
        new SettingOption[] {
            "debug",
            "info",
            "notice",
            "warning",
            "error",
            "critical"
        },*/
        () -> "info",
        (json) -> json.getAsString(),
        (value) -> new JsonPrimitive((String)value)
    ),
    LOG_FILE_PATH(
        "Logging File Path",
        "log_file_path",
        () -> AtomicEditConstants.DEFAULT_LOG_FILE_PATH,
        (json) -> json.getAsString(),
        (value) -> new JsonPrimitive((String)value)
    ),
    
    ;
    
    public final String displayName;
    public final String settingId;
    private final DefaultValueCreator defaultValueCreator;
    private final FromJson fromJsonMethod;
    private final ToJson toJsonMethod;
    
    LoggingSettings(String displayName, String settingId, DefaultValueCreator defaultValueCreator, FromJson fromJson, ToJson toJson) {
        this.displayName = displayName;
        this.settingId = settingId;
        this.defaultValueCreator = defaultValueCreator;
        this.fromJsonMethod = fromJson;
        this.toJsonMethod = toJson;
    }
    
    private interface DefaultValueCreator {
        Object createDefaultValue();
    }
    
    private interface FromJson {
        Object fromJson(JsonElement jsonElem);
    }
    
    private interface ToJson {
        JsonElement toJson(Object value);
    }
    
    public Object createDefaultValue() {
        return defaultValueCreator.createDefaultValue();
    }
    
    public Object parseFromJson(JsonElement jsonElement) {
        return fromJsonMethod.fromJson(jsonElement);
    }
    
    public JsonElement parseToJson(Object value) {
        return toJsonMethod.toJson(value);
    }
    
}
