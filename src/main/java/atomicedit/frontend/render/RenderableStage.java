
package atomicedit.frontend.render;

import atomicedit.backend.ChunkSectionCoord;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author Justin Bonner
 */
public class RenderableStage {
    
    private List<ChunkSectionRenderObject> translucentChunkSectionRenderObjects;
    private List<ChunkSectionRenderObject> opaqueChunkSectionRenderObjects;
    private List<RenderObject> otherRenderObjects;
    private List<RenderObject> toDestroy;
    private boolean unchangedSinceLastSort;
    private ChunkSectionCoord cameraPosAtLastSort;
    
    public RenderableStage(){
        this.translucentChunkSectionRenderObjects = new ArrayList<>();
        this.opaqueChunkSectionRenderObjects = new ArrayList<>();
        this.otherRenderObjects = new ArrayList<>();
        this.toDestroy = new ArrayList<>();
        this.unchangedSinceLastSort = false;
        this.cameraPosAtLastSort = null;
    }
    
    
    public void removeChunkRenderables(Collection<ChunkRenderable> toRemove){
        if(toRemove == null || toRemove.isEmpty()){
            return;
        }
        synchronized(this){
            this.unchangedSinceLastSort = false;
            for(ChunkRenderable renderable : toRemove){
                for(ChunkSectionRenderObject renderObj : renderable.getRenderObjects()){
                    if(renderObj.containsTranslucent){
                        this.translucentChunkSectionRenderObjects.remove(renderObj);
                    }else{
                        this.opaqueChunkSectionRenderObjects.remove(renderObj);
                    }
                    toDestroy.add(renderObj);
                }
            }
        }
    }
    
    public void addChunkRenderables(Collection<ChunkRenderable> toAdd){
        if(toAdd == null || toAdd.isEmpty()){
            return;
        }
        synchronized(this){
            this.unchangedSinceLastSort = false;
            for(ChunkRenderable renderable : toAdd){
                for(ChunkSectionRenderObject renderObj : renderable.getRenderObjects()){
                    if(renderObj.containsTranslucent){
                        this.translucentChunkSectionRenderObjects.add(renderObj);
                    }else{
                        this.opaqueChunkSectionRenderObjects.add(renderObj);
                    }
                }
            }
        }
    }
    
    public void addChunkRenderable(ChunkRenderable toAdd){
        if(toAdd == null){
            return;
        }
        synchronized(this){
            this.unchangedSinceLastSort = false;
            for(ChunkSectionRenderObject renderObj : toAdd.getRenderObjects()){
                if(renderObj.containsTranslucent){
                    this.translucentChunkSectionRenderObjects.add(renderObj);
                }else{
                    this.opaqueChunkSectionRenderObjects.add(renderObj);
                }
            }
        }
    }
    
    public void addRenderables(Collection<Renderable> toAdd){
        if(toAdd == null || toAdd.isEmpty()){
            return;
        }
        synchronized(this){
            this.unchangedSinceLastSort = false;
            for(Renderable renderable : toAdd){
                this.otherRenderObjects.addAll(renderable.getRenderObjects());
            }
        }
    }
    
    public void removeRenderables(Collection<Renderable> toRemove){
        if(toRemove == null || toRemove.isEmpty()){
            return;
        }
        synchronized(this){
            this.unchangedSinceLastSort = false;
            for(Renderable renderable : toRemove){
                this.otherRenderObjects.removeAll(renderable.getRenderObjects());
                toDestroy.addAll(renderable.getRenderObjects());
            }
        }
    }
    
    public void addRenderable(Renderable toAdd){
        if(toAdd == null){
            return;
        }
        synchronized(this){
            this.unchangedSinceLastSort = false;
            this.otherRenderObjects.addAll(toAdd.getRenderObjects());
        }
    }
    
    public void removeRenderable(Renderable toRemove){
        if(toRemove == null){
            return;
        }
        synchronized(this){
            this.unchangedSinceLastSort = false;
            this.otherRenderObjects.removeAll(toRemove.getRenderObjects());
            toDestroy.addAll(toRemove.getRenderObjects());
        }
    }
    
    public void addRenderObject(RenderObject toAdd){
        if(toAdd == null){
            return;
        }
        synchronized(this){
            this.unchangedSinceLastSort = false;
            this.otherRenderObjects.add(toAdd);
        }
    }
    
    public void removeRenderObject(RenderObject toRemove){
        if(toRemove == null){
            return;
        }
        synchronized(this){
            this.unchangedSinceLastSort = false;
            this.otherRenderObjects.remove(toRemove);
            toDestroy.add(toRemove);
        }
    }
    
    private void sortTranslucentChunkRenderables(Camera camera){ //store camera's chunk coords at last sort time, if unchanged and no new chunks added/removed dont sort again
        ChunkSectionCoord cameraChunkCoord = ChunkSectionCoord.getInstanceFromWorldPos(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
        if(cameraPosAtLastSort != null && cameraChunkCoord.equals(cameraPosAtLastSort) && unchangedSinceLastSort){
            return;
        }
        if(this.translucentChunkSectionRenderObjects.isEmpty()){
            return;
        }
        cameraPosAtLastSort = cameraChunkCoord;
        //sort based on dist squared
        sortChunkList(cameraChunkCoord);
    }
    
    private void sortChunkList(ChunkSectionCoord cameraCoord){
        this.translucentChunkSectionRenderObjects.sort((sectionA, sectionB) -> {
            ChunkSectionCoord coord = sectionA.getChunkSectionCoord();
            float distA =  (coord.x - cameraCoord.x) * (coord.x - cameraCoord.x) +
                           (coord.y - cameraCoord.y) * (coord.y - cameraCoord.y) +
                           (coord.z - cameraCoord.z) * (coord.z - cameraCoord.z);
            coord = sectionB.getChunkSectionCoord();
            float distB =  (coord.x - cameraCoord.x) * (coord.x - cameraCoord.x) +
                           (coord.y - cameraCoord.y) * (coord.y - cameraCoord.y) +
                           (coord.z - cameraCoord.z) * (coord.z - cameraCoord.z);
            return distA == distB ? 0 : (distA - distB < 0 ? 1 : -1);
        });
    }
    
    public void renderRenderables(Camera camera){
        synchronized(this){
            for(ChunkSectionRenderObject renObj : this.opaqueChunkSectionRenderObjects){
                renObj.render();
            }
            for(RenderObject renObj : this.otherRenderObjects){
                renObj.render();
            }
            sortTranslucentChunkRenderables(camera);
            for(ChunkSectionRenderObject renObj : this.translucentChunkSectionRenderObjects){
                renObj.render();
            }
        }
    }
    
    public void destroyOldRenderObjects(){
        synchronized(this){
            for(RenderObject renderObj : toDestroy){
                renderObj.destroy();
            }
            toDestroy.clear();
        }
    }
    
}
