
package atomicedit.frontend.render.shaders;

import atomicedit.logging.Logger;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 *
 * @author Justin Bonner
 */
public class UniformLayoutFormat {
    
    //https://www.khronos.org/opengl/wiki/GLAPI/glUniform
    public static void setUniform(ProgramUniforms uniform, int shaderProgram, Matrix4f matrix){
        if(uniform.DATA_TYPE_CLASS != Matrix4f.class){
            Logger.error("update uniform called with wrong parameter");
            throw new IllegalArgumentException();
        }
        glUseProgram(shaderProgram);
        glUniformMatrix4fv(uniform.LOCATION, false, matrix.get(new float[16])); //do not transpose because matrix.get fills the array in column major order
    }
    
    public static void setUniform(int uniformLocation, int shaderProgram, Vector4f vec4) {
        glUseProgram(shaderProgram);
        glUniform4f(uniformLocation, vec4.x, vec4.y, vec4.z, vec4.w);
    }
    
    public static void setUniform(ProgramUniforms uniform, Matrix4f matrix){
        for(ShaderProgram program : ShaderProgram.values()){
            setUniform(uniform, ShaderProgram.getShaderProgram(program), matrix);
        }
    }
    
    public static enum ProgramUniforms {
        MODEL_MATRIX(
            0,
            Matrix4f.class
        ),
        VIEW_MATRIX(
            1,
            Matrix4f.class
        ),
        PROJECTION_MATRIX(
            2,
            Matrix4f.class
        ),
        ;
        
        public final int LOCATION;
        public final Class DATA_TYPE_CLASS;
        
        ProgramUniforms(int location, Class dataTypeClass){
            this.LOCATION = location;
            this.DATA_TYPE_CLASS = dataTypeClass;
        }
        
    }
    
    
}
