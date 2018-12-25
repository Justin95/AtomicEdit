
package atomicedit.frontend.render;

import atomicedit.backend.ChunkSectionCoord;
import atomicedit.backend.chunk.ChunkSection;
import atomicedit.jarreading.texture.TextureLoader;
import java.util.List;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class ChunkSectionRenderObject extends RenderObject{
    
    private final ChunkSectionCoord chunkSectionPos;
    
    public ChunkSectionRenderObject(ChunkSectionCoord pos, boolean containsTranslucent, List<Float> verticies, List<Integer> indicies){
        super(
            new Vector3f(pos.x * ChunkSection.SIDE_LENGTH, pos.y * ChunkSection.SIDE_LENGTH, pos.z * ChunkSection.SIDE_LENGTH),
            new Vector3f(0,0,0),
            TextureLoader.getMinecraftDefaultTexture(),
            containsTranslucent,
            convertFloat(verticies),
            convertInteger(indicies)
        );
        this.chunkSectionPos = pos;
    }
    
    public ChunkSectionCoord getChunkSectionCoord(){
        return this.chunkSectionPos;
    }
    
    
    
    private static float[] convertFloat(List<Float> source){
        float[] dest = new float[source.size()];
        for(int i = 0; i < source.size(); i++){
            dest[i] = source.get(i);
        }
        return dest;
    }
    
    private static int[] convertInteger(List<Integer> source){
        int[] dest = new int[source.size()];
        for(int i = 0; i < source.size(); i++){
            dest[i] = source.get(i);
        }
        return dest;
    }
    
}
