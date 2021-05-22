
package atomicedit.frontend.render;

import atomicedit.frontend.render.shaders.DataBufferLayoutFormat;
import atomicedit.frontend.render.shaders.ShaderProgram;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class NoTextureRenderObject extends RenderObject{
    
    public NoTextureRenderObject(Vector3f pos, Vector3f rot, Vector3f scale, boolean containsTranslucent, float[] vertexData, int[] indicies){
        super(pos, rot, scale, null, containsTranslucent, vertexData, indicies);
        this.bufferFormat = DataBufferLayoutFormat.NO_TEXTURE_DATA_BUFFER_LAYOUT;
        this.shaderProgram = ShaderProgram.getShaderProgram(ShaderProgram.NO_TEXTURE_SHADER_PROGRAM);
    }
    
}
