
package atomicedit.frontend.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public class LoadingUtils {
    
    /**
     * Read an input stream into a String.
     * @param input an input stream
     * @return 
     */
    public static String readInputStream(InputStream input){
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder strBuilder = new StringBuilder();
        reader.lines().forEachOrdered((String line) -> {
            strBuilder.append(line);
            strBuilder.append("\n");
        });
        return strBuilder.toString();
    }
    
    public static String gePreferedtMinecraftJarFilePath(String minecraftDirectory, String minecraftVersion){
        String directory = minecraftDirectory + "/versions";
        if (minecraftVersion.equals("latest")) {
            File jarsDirectory = new File(directory);
            File newestVersion = getHighestVersionedDir(jarsDirectory.listFiles());
            return directory + "/" + newestVersion.getName() + "/" + newestVersion.getName() + ".jar";
        } else {
            return directory + "/" + minecraftVersion + "/" + minecraftVersion + ".jar";
        }
    }
    
    private static File getHighestVersionedDir(File[] files){
        List<File> versionDirs = new ArrayList<>();
        for(File file : files){
            if(file.getName().matches("[0-9]+\\.[0-9]+(\\.[0-9]+)?")){ //ex 1.13.1 or 1.8
                versionDirs.add(file);
            }
        }
        versionDirs.sort((File a, File b) -> {
            String[] aName = a.getName().split("\\.");
            String[] bName = b.getName().split("\\.");
            int majorA = Integer.parseInt(aName[0]);
            int majorB = Integer.parseInt(bName[0]);
            if(majorA != majorB){
                return majorB - majorA;
            }
            int minorA = Integer.parseInt(aName[1]);
            int minorB = Integer.parseInt(bName[1]);
            if(minorA != minorB){
                return minorB - minorA;
            }
            if(aName.length != bName.length){
                return bName.length - aName.length;
            }
            int bugfixA = Integer.parseInt(aName[2]);
            int bugfixB = Integer.parseInt(bName[2]);
            return bugfixB - bugfixA;
        });
        return versionDirs.get(0);
    }
    
}
