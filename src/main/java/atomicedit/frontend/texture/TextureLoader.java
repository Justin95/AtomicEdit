
package atomicedit.frontend.texture;

import atomicedit.AtomicEdit;
import atomicedit.logging.Logger;
import atomicedit.settings.AtomicEditSettings;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/**
 *
 * @author Justin Bonner
 */
public class TextureLoader {
    
    private static MinecraftTexture defaultTexture;
    private static BufferedImage unknownTexture;
    private static final Object TEXTURE_LOCK = new Object();
    
    /**
     * Load the default textures from an installed minecraft jar. Checks in the default location for the latest versioned jar.
     * @return 
     */
    public static MinecraftTexture getMinecraftDefaultTexture(){
        if(defaultTexture != null){
            return defaultTexture;
        }
        synchronized(TEXTURE_LOCK){
            if(defaultTexture != null){ //have this check twice, first time to avoid sync everytime, second time to avoid concurrency issue
                return defaultTexture;
            }
            defaultTexture = createDefaultTexture();
            return defaultTexture;
        }
        
    }
    
    private static MinecraftTexture createDefaultTexture(){
        return new MinecraftTexture(loadBlockTextures(), createUnknownTexture());
    }
    
    /**
     * Only use this method in test cases.
     * @return 
     */
    public static Map<String, BufferedImage> testTextureLoading(){
        return loadBlockTextures();
    }
    
    private static final String INTERNAL_PATH = "assets/minecraft/textures/block/";
    private static final String INTERNAL_EXT = ".png";
    private static Map<String, BufferedImage> loadBlockTextures() {
        Map<String, BufferedImage> imageMap = new HashMap<>();
        try{
            ZipFile jarFile = new ZipFile(getNewestMinecraftJarFilePath());
            jarFile.stream().filter(
                (entry) -> entry.getName().startsWith(INTERNAL_PATH) && entry.getName().endsWith(INTERNAL_EXT)
            ).forEach((pngEntry) -> {
                String name = "minecraft:" + pngEntry.getName().substring(INTERNAL_PATH.length(), pngEntry.getName().length() - INTERNAL_EXT.length()); //remove .png
                Logger.info("Loading minecraft texture: " + name);
                BufferedImage texture;
                try{
                    texture = readPngToSquareImage(jarFile.getInputStream(pngEntry));
                }catch(IOException e){
                    Logger.warning("Could not read png: " + pngEntry.getName());
                    texture = createUnknownTexture();
                }
                imageMap.put(name, texture);
            });
        }catch(IOException e){
            Logger.error("IOException while trying to read minecraft textures: " + e.getLocalizedMessage());
        }
        return imageMap;
    }
    
    private static BufferedImage readPngToSquareImage(InputStream pngFile) throws IOException{
        BufferedImage wholeImage = ImageIO.read(pngFile);
        BufferedImage texture = new BufferedImage(MinecraftTexture.TEXTURE_RES, MinecraftTexture.TEXTURE_RES, BufferedImage.TYPE_INT_ARGB);
        texture.getGraphics().drawImage(wholeImage, 0, 0, MinecraftTexture.TEXTURE_RES, MinecraftTexture.TEXTURE_RES, null); //copy texture square
        return texture;
    }
    
    private static String getNewestMinecraftJarFilePath(){
        String directory = getMinecraftDirectory() + "/versions";
        File jarsDirectory = new File(directory);
        File newestVersion = getHighestVersionedDir(jarsDirectory.listFiles());
        Logger.info("Getting textures from minecraft version: " + newestVersion.getName());
        return directory + "/" + newestVersion.getName() + "/" + newestVersion.getName() + ".jar";
    }
    
    private static String getMinecraftDirectory(){
        return AtomicEdit.getSettings().getSettingValueAsString(AtomicEditSettings.MINECRAFT_INSTALL_LOCATION);
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
    
    public static BufferedImage testCreateUnknownTexture(){
        return createUnknownTexture();
    }
    
    private static BufferedImage createUnknownTexture(){
        if(unknownTexture != null){
            return unknownTexture;
        }
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.setColor(new Color(100, 200, 100));
        g.drawRect(0, 0, 15, 15);
        g.setColor(Color.red);
        g.drawString("B", 3, 12);
        unknownTexture = image;
        return image;
    }
    
    
    
}
