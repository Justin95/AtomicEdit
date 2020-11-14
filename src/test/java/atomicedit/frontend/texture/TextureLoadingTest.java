
package atomicedit.frontend.texture;

import atomicedit.jarreading.texture.TextureLoader;
import atomicedit.logging.Logger;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.swing.JFrame;
import org.junit.Test;

/**
 *
 * @author Justin Bonner
 */
public class TextureLoadingTest {
    
    //This test should not be run by an automatically running test suite.
    //@Test
    public void loadTextureTest() throws Exception {
        Logger.initialize();
        Map<String, BufferedImage> results = TextureLoader.testTextureLoading();
        BufferedImage superImage = MinecraftTexture.testCreateSuperImage(results, TextureLoader.testCreateUnknownTexture());
        JFrame jframe = new JFrame();
        Canvas canvas = new Canvas(){
            public void paint(Graphics g){
                g.drawImage(superImage, 0, 0, null);
            }
        };
        jframe.setSize(800, 600);
        canvas.setSize(800, 600);
        jframe.add(canvas);
        jframe.pack();
        jframe.setVisible(true);
        canvas.repaint();
        while(jframe.isVisible()){
            Thread.sleep(500);
        }
    }
    
}
