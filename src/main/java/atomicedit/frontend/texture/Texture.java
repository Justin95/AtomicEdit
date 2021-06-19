
package atomicedit.frontend.texture;

import atomicedit.logging.Logger;
import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

/**
 *
 * @author Justin Bonner
 */
public class Texture {
    
    private int textureID;
    private boolean initialized;
    private BufferedImage image; //not needed after GL upload
	
	public Texture(BufferedImage image){
        initialized = false;
		this.image = image;
	}
	
    private void initialize(){
        this.initialized = true;
        this.textureID = GL11.glGenTextures();
		ByteBuffer textureData = imageToByteBuffer(image);
		uploadTexture(textureID, image.getWidth(), image.getHeight(), textureData, 0);
        this.image = null;
    }
	
	public void bind(int textureNumber){
        if(!initialized){
            initialize();
        }
		if(textureNumber < 0 || textureNumber > GL13.GL_MAX_TEXTURE_UNITS) Logger.error("Invalid call to bind texture");
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureNumber);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureID);
	}
	
	
	/**
	 * 
	 * @return the openGL recognized integer textureID for this Texture
	 */
	public int getTextureID(){
		return this.textureID;
	}
	
	
	/**
	 * Gives this texture object to openGL with the name id.
	 * @param id the openGL id to use for this texture
	 */
	private void uploadTexture(int id, int width, int height, ByteBuffer image, int mipmapLevel){
		GL11.glBindTexture(GL11.GL_TEXTURE_2D,id);
		setupOpenGLTextureParameters();
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, mipmapLevel, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image);
	}
	
	
	/**
	 * Sets up the proper Texture Parameters for this openGL texture.
	 */
	private void setupOpenGLTextureParameters(){
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
	}
	
	
	
	private static ByteBuffer imageToByteBuffer(BufferedImage image){ //assume ARGB
		int bytesPerPixel = 4;
		int width = image.getWidth();
		int height = image.getHeight();
		int[] pixels = new int[width * height];
		image.getRGB(0, 0, width, height, pixels, 0, width);
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bytesPerPixel);
		for(int pixel : pixels){
			buffer.put((byte) ((pixel >> 16) & 0xFF));  // Red
			buffer.put((byte) ((pixel >>  8) & 0xFF));  // Green
			buffer.put((byte) ((pixel >>  0) & 0xFF));  // Blue
			buffer.put((byte) ((pixel >> 24) & 0xFF));  // Alpha
		}
		((Buffer)buffer).flip(); //cast to a Buffer to fix java 8 compatability issue: https://stackoverflow.com/questions/61267495/exception-in-thread-main-java-lang-nosuchmethoderror-java-nio-bytebuffer-flip
		return buffer;
	}
    
}
