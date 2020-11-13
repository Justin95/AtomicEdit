
package atomicedit.utils;

import java.io.File;
import org.liquidengine.legui.icon.ImageIcon;
import org.liquidengine.legui.image.Image;
import org.liquidengine.legui.image.StbBackedLoadableImage;

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
    
    public static ImageIcon loadIcon(String path){
        Image iconImage = new StbBackedLoadableImage(path);
        ImageIcon icon = new ImageIcon(iconImage);
        return icon;
    }
    
}
