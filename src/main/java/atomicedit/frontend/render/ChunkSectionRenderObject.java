
package atomicedit.frontend.render;

import atomicedit.backend.ChunkSectionCoord;
import atomicedit.backend.chunk.ChunkSection;
import atomicedit.jarreading.texture.TextureLoader;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class ChunkSectionRenderObject extends RenderObject{
    
    private final ChunkSectionCoord chunkSectionPos;
    
    public ChunkSectionRenderObject(ChunkSectionCoord pos, boolean containsTranslucent, float[] verticies, int[] indicies){
        super(
            new Vector3f(pos.x * ChunkSection.SIDE_LENGTH, pos.y * ChunkSection.SIDE_LENGTH, pos.z * ChunkSection.SIDE_LENGTH),
            new Vector3f(0,0,0),
            TextureLoader.getMinecraftDefaultTexture(),
            containsTranslucent,
            verticies,
            indicies
        );
        this.chunkSectionPos = pos;
    }
    
    public ChunkSectionCoord getChunkSectionCoord(){
        return this.chunkSectionPos;
    }
    
}
