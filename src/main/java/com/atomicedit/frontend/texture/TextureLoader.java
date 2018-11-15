
package com.atomicedit.frontend.texture;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 *
 * @author Justin Bonner
 */
public class TextureLoader {
    
    private static Texture defaultTexture;
    private static final Object TEXTURE_LOCK = new Object();
    
    /**
     * Load the default textures from an installed minecraft jar. Checks in the default location for the latest versioned jar.
     * @return 
     */
    public static Texture getMinecraftDefaultTexture(){
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
    
    //temp stub
    private static Texture createDefaultTexture(){
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.setColor(new Color(50, 200, 150));
        g.drawRect(0, 0, 16, 16);
        g.setColor(Color.red);
        g.drawString("B", 8, 8);
        return new Texture(image);
    }
    
}
