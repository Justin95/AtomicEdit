
package atomicedit.frontend.render;

import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.ChunkReader;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.frontend.render.blockmodelcreation.ChunkRenderObjectCreator;
import atomicedit.logging.Logger;
import java.util.Collection;

/**
 *
 * @author Justin Bonner
 */
public class ChunkRenderable {
    
    private Collection<ChunkSectionRenderObject> chunkSectionRenderObjects;
    private Collection<RenderObject> miscRenderObjects;
    private ChunkCoord chunkCoord;
    
    public ChunkRenderable(ChunkReader chunk, ChunkReader xMinus, ChunkReader xPlus, ChunkReader zMinus, ChunkReader zPlus){
        this.chunkSectionRenderObjects = ChunkRenderObjectCreator.createRenderObjects(chunk, xMinus, xPlus, zMinus, zPlus);
        this.miscRenderObjects = ChunkRenderObjectCreator.createMiscRenderObjects(chunk);
        this.chunkCoord = null;
        try{
            this.chunkCoord = chunk.getChunkCoord();
        }catch(MalformedNbtTagException e){
            Logger.error("Could not read chunk coord in ChunkRenderable constructor");
        }
        
    }
    
    
    public Collection<ChunkSectionRenderObject> getChunkSectionRenderObjects(){
        return this.chunkSectionRenderObjects;
    }
    
    public Collection<RenderObject> getMiscRenderObjects() {
        return this.miscRenderObjects;
    }
    
    public ChunkCoord getChunkCoord(){
        return this.chunkCoord;
    }
    
    @Override
    public String toString(){
        return "ChunkRenderable:" + chunkCoord;
    }
    
}
