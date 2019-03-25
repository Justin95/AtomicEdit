
package atomicedit.frontend.render;

import atomicedit.frontend.render.shaders.DataBufferLayoutFormat;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL11.GL_LINES;

/**
 * Draw a render object made only of lines. This render object will not use a texture.
 * As such remember to reference the NO_TEXTURE_DATA_BUFFER_LAYOUT when creating vertex data.
 * @author Justin Bonner
 */
public class LinesRenderObject extends RenderObject {
    
    public LinesRenderObject(Vector3f pos, Vector3f rot, boolean containsTranslucent, float[] vertexData, int[] indicies){
        super(pos, rot, null, containsTranslucent, vertexData, indicies);
        this.drawingShape = GL_LINES;
        this.bufferFormat = DataBufferLayoutFormat.NO_TEXTURE_DATA_BUFFER_LAYOUT;
    }
    
}
