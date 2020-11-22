
package atomicedit.frontend.render;

import atomicedit.frontend.render.shaders.DataBufferLayoutFormat;
import atomicedit.frontend.render.shaders.ShaderProgram;
import atomicedit.frontend.render.shaders.UniformLayoutFormat;
import atomicedit.frontend.render.utils.RenderMatrixUtils;
import atomicedit.frontend.render.utils.VaoCreater;
import atomicedit.frontend.texture.Texture;
import atomicedit.logging.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL20.glUseProgram;
import org.lwjgl.opengl.GL30;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

/**
 *
 * @author Justin Bonner
 */
public class RenderObject {
    
    public static DataBufferLayoutFormat DEFAULT_BUFFER_FORMAT = DataBufferLayoutFormat.DEFAULT_DATA_BUFFER_LAYOUT;
    
    protected Vector3f position;
    protected Vector3f rotation;
    protected final boolean containsTranslucent;
    protected Matrix4f modelMatrix;
    protected Texture texture;
    protected int vao;
    protected int shaderProgram;
    protected int numIndicies;
    protected int drawingShape;
    protected DataBufferLayoutFormat bufferFormat;
    protected boolean openGlInitialized;
    protected boolean destroyed;
    protected float[] vertexData;
    protected int[] indicies;
    
    public RenderObject(Vector3f pos, Vector3f rot, Texture texture, boolean containsTranslucent, float[] vertexData, int[] indicies){
        this.position = pos;
        this.rotation = rot;
        this.containsTranslucent = containsTranslucent;
        this.modelMatrix = RenderMatrixUtils.createModelMatrix(pos, rot);
        this.texture = texture;
        this.shaderProgram = ShaderProgram.getShaderProgram(ShaderProgram.DEFAULT_SHADER_PROGRAM); //can add a choice here later
        this.numIndicies = indicies.length;
        this.openGlInitialized = false;
        this.destroyed = false;
        this.vertexData = vertexData;
        this.indicies = indicies;
        this.drawingShape = GL_TRIANGLES;
        this.bufferFormat = DEFAULT_BUFFER_FORMAT;
    }
    
    public void initialize(){
        if(this.openGlInitialized){
            Logger.warning("Tried to double initialize render object");
            return;
        }
        this.openGlInitialized = true;
        this.vao = VaoCreater.createVao(this.bufferFormat, vertexData, indicies);
        this.vertexData = null;
        this.indicies = null; //dont need this anymore, let it be GC
    }
    
    public void destroy(){
        if(this.destroyed){
            Logger.warning("Tried to destroy already destroyed RenderObject");
            return;
        }
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
    
    protected void setUniforms() {
        UniformLayoutFormat.setUniform(UniformLayoutFormat.ProgramUniforms.MODEL_MATRIX, shaderProgram, modelMatrix);
    }
    
    public void render(){
        if(!this.openGlInitialized){
            initialize();
        }
        if(this.destroyed){
            Logger.error("Tried to draw destroyed render object");
            return;
        }
        setUniforms();
        if(texture != null){
            texture.bind(0); //bind to texture 0
        } 
        glUseProgram(shaderProgram);
        GL30.glBindVertexArray(vao);
        glDrawElements(this.drawingShape, numIndicies, GL_UNSIGNED_INT, 0);
    }
    
    
}
