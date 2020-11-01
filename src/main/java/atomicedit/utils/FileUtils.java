
package atomicedit.utils;

import java.io.File;

/**
 *
 * @author justin
 */
public class FileUtils {
    
    public static String concatPaths(String path, String path2) {
        if (path2.startsWith(File.pathSeparator)) {
            path2 = path2.substring(1);
        }
        return path + (path.endsWith(File.pathSeparator) ? "" : "/") + path2;
    }
    
}
