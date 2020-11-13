
package atomicedit.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    
    public static String readResourceFile(String filepath) throws IOException {
        StringBuilder str = new StringBuilder();
        InputStream input = FileUtils.class.getResourceAsStream(filepath);
        if (input == null) {
            throw new FileNotFoundException(filepath + " was not found.");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            reader.lines().forEach((String line) -> str.append(line).append("\n"));
        } finally {
            input.close();
        }
        return str.toString();
    }
    
}
