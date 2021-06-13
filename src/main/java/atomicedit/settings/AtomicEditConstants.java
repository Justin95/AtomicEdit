
package atomicedit.settings;

/**
 *
 * @author Justin Bonner
 */
public class AtomicEditConstants {
    
    private static final String HOME_DIR = System.getProperty("user.home");
    public static final String ATOMIC_EDIT_INSTALL_PATH = HOME_DIR + (HOME_DIR.endsWith("/") ? "" : "/") + ".AtomicEdit";
    public static final String SETTINGS_FILEPATH = ATOMIC_EDIT_INSTALL_PATH + "/atomicEditSettings.json";
    public static final String LOGGING_SETTINGS_FILEPATH = ATOMIC_EDIT_INSTALL_PATH + "/loggingSettings.json";
    public static final String DEFAULT_LOG_FILE_PATH = ATOMIC_EDIT_INSTALL_PATH + "/AtomicEditLastLog.log";
    public static final String SCHEMATIC_DIR_PATH = ATOMIC_EDIT_INSTALL_PATH + "/schematics";
    
}
