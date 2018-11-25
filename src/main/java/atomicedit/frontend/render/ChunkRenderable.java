
package atomicedit.frontend.render;

import atomicedit.backend.chunk.ChunkReader;
import atomicedit.frontend.ChunkRenderObjectCreator;
import java.util.Collection;

/**
 *
 * @author Justin Bonner
 */
public class ChunkRenderable implements Renderable{
    
    private Collection<RenderObject> renderObjects;
    
    public ChunkRenderable(ChunkReader chunk, Collection<ChunkReader> adjacentChunks){
        this.renderObjects = ChunkRenderObjectCreator.createRenderObjects(chunk, adjacentChunks);
    }
    
    @Override
    public Collection<RenderObject> getRenderObjects(){
        return this.renderObjects;
    }
    
}
