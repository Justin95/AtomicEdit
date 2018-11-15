
package com.atomicedit.frontend.render.utils;

import com.atomicedit.frontend.render.shaders.DataBufferLayoutFormat;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 *
 * @author Justin Bonner
 */
public class VaoCreater {
    
    
    /**
     * Create a VAO and the VBOs in it.
     * @param vertexData vertex data in the format specified in DataBufferLayoutFormat.java
     * @param indicies
     * @return a valid VAO id
     */
    public static int createVao(float[] vertexData, short[] indicies){
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);
        
        int vertexVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexVbo);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
        
        int indiciesVbo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indiciesVbo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicies, GL_STATIC_DRAW);
        
        DataBufferLayoutFormat.defineBufferLayout();
        
        glBindVertexArray(0);
        return vao;
    }
    
    
    
}
