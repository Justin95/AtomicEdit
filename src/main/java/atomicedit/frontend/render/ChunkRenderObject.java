
package atomicedit.frontend.render;

import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.ChunkSection;
import atomicedit.frontend.render.shaders.DataBufferLayoutFormat;
import atomicedit.frontend.render.shaders.ShaderProgram;
import atomicedit.jarreading.texture.TextureLoader;
import org.joml.Vector3f;

/**
 * This class is intended to be rendered in a ChunkRenderable with other ROs.
 * @author Justin Bonner
 */
public class ChunkRenderObject extends RenderObject{
    
    private final ChunkCoord chunkPos;
    
    public ChunkRenderObject(ChunkCoord pos, boolean containsTranslucent, float[] verticies, int[] indicies){
        super(
            new Vector3f(pos.x * ChunkSection.SIDE_LENGTH, 0, pos.z * ChunkSection.SIDE_LENGTH),
            new Vector3f(0,0,0),
            new Vector3f(1,1,1),
            TextureLoader.getMinecraftDefaultTexture(),
            containsTranslucent,
            verticies,
            indicies
        );
        this.shaderProgram = ShaderProgram.getShaderProgram(ShaderProgram.BLOCK_SHADER_PROGRAM);
        this.bufferFormat = DataBufferLayoutFormat.BLOCK_DATA_BUFFER_LAYOUT;
        this.chunkPos = pos;
    }
    
    public ChunkCoord getChunkCoord(){
        return this.chunkPos;
    }
    
}
