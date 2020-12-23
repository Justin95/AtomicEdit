
package atomicedit.frontend.render;

import atomicedit.frontend.render.shaders.DataBufferLayoutFormat;
import atomicedit.frontend.render.shaders.ShaderProgram;
import atomicedit.frontend.render.shaders.UniformLayoutFormat;
import atomicedit.jarreading.texture.TextureLoader;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 *
 * @author Justin Bonner
 */
public class SchematicRenderObject extends RenderObject {
    
    private Vector4f color;
    
    public SchematicRenderObject(Vector4f color, boolean containsTranslucent, float[] verticies, int[] indicies){
        super(
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            TextureLoader.getMinecraftDefaultTexture(),
            containsTranslucent,
            verticies,
            indicies
        );
        this.color = color;
        this.shaderProgram = ShaderProgram.getShaderProgram(ShaderProgram.BLOCK_COLOR_SHADER_PROGRAM);
        this.bufferFormat = DataBufferLayoutFormat.BLOCK_DATA_BUFFER_LAYOUT;
    }
    
    @Override
    protected void setUniforms() {
        super.setUniforms();
        //uniform at position 3 in vertex shader
        UniformLayoutFormat.setUniform(3, shaderProgram, color);
    }
    
}
