
package atomicedit.frontend.render;

import atomicedit.frontend.render.shaders.DataBufferLayoutFormat;
import atomicedit.frontend.render.shaders.ShaderProgram;
import atomicedit.frontend.render.shaders.UniformLayoutFormat;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 *
 * @author Justin Bonner
 */
public class OnlyPositionRenderObject extends RenderObject {
    
    private final static int COLOR_UNIFORM_LOC = 3;
    private final Vector4f color;
    
    public OnlyPositionRenderObject(Vector3f pos, Vector3f rot, Vector3f scale, Vector4f color, boolean containsTranslucent, float[] vertexData, int[] indicies){
        super(pos, rot, scale, null, containsTranslucent, vertexData, indicies);
        this.color = color;
        this.bufferFormat = DataBufferLayoutFormat.ONLY_POSITION_DATA_BUFFER_LAYOUT;
        this.shaderProgram = ShaderProgram.getShaderProgram(ShaderProgram.ONLY_POSITION_SHADER_PROGRAM);
    }
    
    @Override
    protected void setUniforms() {
        super.setUniforms();
        UniformLayoutFormat.setUniform(COLOR_UNIFORM_LOC, shaderProgram, color);
    }
    
}
