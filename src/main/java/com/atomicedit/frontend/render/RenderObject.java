
package com.atomicedit.frontend.render;

import com.atomicedit.frontend.render.shaders.ShaderProgram;
import com.atomicedit.frontend.render.shaders.UniformLayoutFormat;
import com.atomicedit.frontend.render.utils.RenderMatrixUtils;
import com.atomicedit.frontend.render.utils.VaoCreater;
import com.atomicedit.frontend.texture.Texture;
import com.atomicedit.logging.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL20.glUseProgram;
import org.lwjgl.opengl.GL30;

/**
 *
 * @author Justin Bonner
 */
public class RenderObject {
    
    private Vector3f position;
    private Vector3f rotation;
    private Matrix4f modelMatrix;
    private Texture texture;
    private int vao;
    private int shaderProgram;
    private int numIndicies;
    
    public RenderObject(Vector3f pos, Vector3f rot, Texture texture, float[] vertexData, short[] indicies){
        this.position = pos;
        this.rotation = rot;
        this.modelMatrix = RenderMatrixUtils.createModelMatrix(pos, rot);
        this.texture = texture;
        this.shaderProgram = ShaderProgram.getShaderProgram(ShaderProgram.DEFAULT_SHADER_PROGRAM); //can add a choice here later
        this.numIndicies = indicies.length;
        this.vao = VaoCreater.createVao(vertexData, indicies);
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
        UniformLayoutFormat.setUniform(UniformLayoutFormat.ProgramUniforms.MODEL_MATRIX, shaderProgram, modelMatrix);
        texture.bind(0); //bind to texture 0
        glUseProgram(shaderProgram);
        GL30.glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, numIndicies, GL_UNSIGNED_SHORT, 0);
    }
    
    
}
