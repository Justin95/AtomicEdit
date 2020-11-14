
package atomicedit.utils;

import atomicedit.logging.Logger;
import java.io.IOException;

/**
 *
 * @author Justin Bonner
 */
public class VersionUtils {
    
    private static final String VERSION;
    
    static {
        VERSION = readVersion();
    }
    
    private static String readVersion() {
        try {
            return FileUtils.readResourceFile("/version.txt");
        } catch (IOException e) {
            Logger.error("Cannot read version.", e);
            return null;
        }
    }
    
    public static String getCurrentVersion() {
        return VERSION;
    }
    
    //TODO check github for a newer version and alert user
    
}
