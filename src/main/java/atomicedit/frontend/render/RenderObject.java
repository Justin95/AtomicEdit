
package atomicedit.frontend.render;

import atomicedit.frontend.render.shaders.ShaderProgram;
import atomicedit.frontend.render.shaders.UniformLayoutFormat;
import atomicedit.frontend.render.utils.RenderMatrixUtils;
import atomicedit.frontend.render.utils.VaoCreater;
import atomicedit.frontend.texture.Texture;
import atomicedit.logging.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL20.glUseProgram;
import org.lwjgl.opengl.GL30;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

/**
 *
 * @author Justin Bonner
 */
public class RenderObject {
    
    protected Vector3f position;
    protected Vector3f rotation;
    protected Matrix4f modelMatrix;
    protected Texture texture;
    protected int vao;
    protected int shaderProgram;
    protected int numIndicies;
    protected boolean openGlInitialized;
    protected boolean destroyed;
    protected float[] vertexData;
    protected short[] indicies;
    
    public RenderObject(Vector3f pos, Vector3f rot, Texture texture, float[] vertexData, short[] indicies){
        this.position = pos;
        this.rotation = rot;
        this.modelMatrix = RenderMatrixUtils.createModelMatrix(pos, rot);
        this.texture = texture;
        this.shaderProgram = ShaderProgram.getShaderProgram(ShaderProgram.DEFAULT_SHADER_PROGRAM); //can add a choice here later
        this.numIndicies = indicies.length;
        this.openGlInitialized = false;
        this.destroyed = false;
        this.vertexData = vertexData;
        this.indicies = indicies;
    }
    
    public void initialize(){
        if(this.openGlInitialized){
            Logger.warning("Tried to double initialize render object");
            return;
        }
        this.openGlInitialized = true;
        this.vao = VaoCreater.createVao(vertexData, indicies);
        this.vertexData = null;
        this.indicies = null; //dont need this anymore, let it be GC
    }
    
    public void destroy(){
        this.destroyed = true;
        glDeleteVertexArrays(vao); //assume vbos were deleted at vao creation
    }
    
    public void updatePosition(Vector3f pos){
        this.position = pos;
        this.modelMatrix = RenderMatrixUtils.createModelMatrix(this.position, this.rotation);
    }
    
    public void updateRotation(Vector3f rot){
        this.rotation = rot;
        this.modelMatrix = RenderMatrixUtils.createModelMatrix(this.position, this.rotation);
    }
    
    public void updatePositionAndRotation(Vector3f pos, Vector3f rot){
        this.position = pos;
        this.rotation = rot;
        this.modelMatrix = RenderMatrixUtils.createModelMatrix(this.position, this.rotation);
    }
    
    public void render(){
        if(!this.openGlInitialized){
            initialize();
        }
        if(this.destroyed){
            Logger.error("Tried to draw destroyed render object");
            return;
        }
        UniformLayoutFormat.setUniform(UniformLayoutFormat.ProgramUniforms.MODEL_MATRIX, shaderProgram, modelMatrix);
        texture.bind(0); //bind to texture 0
        glUseProgram(shaderProgram);
        GL30.glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, numIndicies, GL_UNSIGNED_SHORT, 0);
    }
    
    
}
