
package atomicedit.frontend.render;

import atomicedit.backend.ChunkSectionCoord;
import atomicedit.logging.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author Justin Bonner
 */
public class RenderableStage {
    
    private final Object QUEUE_LOCK;
    private final List<ChunkRenderable> chunkRenderablesToAdd;
    private final List<ChunkRenderable> chunkRenderablesToRemove;
    private final List<RenderObject> renderObjectsToAdd;
    private final List<RenderObject> renderObjectsToRemove;
    
    private final List<ChunkSectionRenderObject> translucentChunkSectionRenderObjects;
    private final List<ChunkSectionRenderObject> opaqueChunkSectionRenderObjects;
    private final List<ChunkRenderObject> opaqueChunkRenderObjects;
    private final List<RenderObject> otherRenderObjects;
    private final List<RenderObject> toDestroy;
    private boolean unchangedSinceLastSort;
    private ChunkSectionCoord cameraPosAtLastSort;
    
    public RenderableStage() {
        this.QUEUE_LOCK = new Object();
        this.translucentChunkSectionRenderObjects = new ArrayList<>();
        this.opaqueChunkSectionRenderObjects = new ArrayList<>();
        this.opaqueChunkRenderObjects = new ArrayList<>();
        this.otherRenderObjects = new ArrayList<>();
        this.toDestroy = new ArrayList<>();
        this.chunkRenderablesToAdd = new ArrayList<>();
        this.chunkRenderablesToRemove = new ArrayList<>();
        this.renderObjectsToAdd = new ArrayList<>();
        this.renderObjectsToRemove = new ArrayList<>();
        this.unchangedSinceLastSort = false;
        this.cameraPosAtLastSort = null;
    }
    
    
    public void removeChunkRenderables(Collection<ChunkRenderable> toRemove){
        if(toRemove == null || toRemove.isEmpty()){
            return;
        }
        synchronized(QUEUE_LOCK){
            for (ChunkRenderable renderable : toRemove) {
                if (renderable != null) {
                    this.chunkRenderablesToRemove.add(renderable);
                }
            }
        }
    }
    
    public void addChunkRenderables(Collection<ChunkRenderable> toAdd){
        if(toAdd == null || toAdd.isEmpty()){
            return;
        }
        synchronized(QUEUE_LOCK){
            for (ChunkRenderable renderable : toAdd) {
                if (renderable != null) {
                    this.chunkRenderablesToAdd.add(renderable);
                }
            }
        }
    }
    
    public void addChunkRenderable(ChunkRenderable toAdd){
        if(toAdd == null){
            return;
        }
        synchronized(QUEUE_LOCK) {
            this.chunkRenderablesToAdd.add(toAdd);
        }
    }
    
    
    
    public void addRenderables(Collection<Renderable> toAdd){
        if(toAdd == null || toAdd.isEmpty()){
            return;
        }
        synchronized(QUEUE_LOCK){
            for (Renderable renderable : toAdd) {
                if (renderable == null) {
                    continue;
                }
                this.renderObjectsToAdd.addAll(renderable.getRenderObjects());
            }
        }
    }
    
    public void removeRenderables(Collection<Renderable> toRemove){
        if(toRemove == null || toRemove.isEmpty()){
            return;
        }
        synchronized(this){
            for(Renderable renderable : toRemove){
                if (renderable == null) {
                    continue;
                }
                this.renderObjectsToRemove.addAll(renderable.getRenderObjects());
            }
        }
    }
    
    public void addRenderable(Renderable toAdd){
        if(toAdd == null){
            return;
        }
        synchronized(QUEUE_LOCK){
            this.renderObjectsToAdd.addAll(toAdd.getRenderObjects());
        }
    }
    
    public void removeRenderable(Renderable toRemove){
        if(toRemove == null){
            return;
        }
        synchronized(QUEUE_LOCK) {
            this.renderObjectsToRemove.addAll(toRemove.getRenderObjects());
        }
    }
    
    public void addRenderObject(RenderObject toAdd){
        if(toAdd == null){
            return;
        }
        synchronized(QUEUE_LOCK){
            this.renderObjectsToAdd.add(toAdd);
        }
    }
    
    public void removeRenderObject(RenderObject toRemove){
        if(toRemove == null){
            return;
        }
        synchronized (QUEUE_LOCK) {
            this.renderObjectsToRemove.add(toRemove);
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
    
    private void sortChunkList(ChunkSectionCoord cameraCoord) {
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
    
    /**
     * Do any house keeping of internals. This must be called from the rendering thread only.
     */
    public void housekeeping() {
        synchronized(QUEUE_LOCK){
            //add any new chunk renderables
            if (!this.chunkRenderablesToAdd.isEmpty()) {
                this.unchangedSinceLastSort = false;
            }
            for(ChunkRenderable renderable : this.chunkRenderablesToAdd){
                for(ChunkSectionRenderObject renderObj : renderable.getChunkSectionRenderObjects()){
                    if(renderObj.containsTranslucent){
                        this.translucentChunkSectionRenderObjects.add(renderObj);
                    }else{
                        this.opaqueChunkSectionRenderObjects.add(renderObj);
                    }
                }
                if (renderable.hasChunkRenderObject()) {
                    this.opaqueChunkRenderObjects.add(renderable.getChunkRenderObject());
                }
                this.otherRenderObjects.addAll(renderable.getMiscRenderObjects());
            }
            this.chunkRenderablesToAdd.clear();
            
            //remove any old chunk renderables
            if (!this.chunkRenderablesToRemove.isEmpty()) {
                this.unchangedSinceLastSort = false;
            }
            for(ChunkRenderable renderable : this.chunkRenderablesToRemove){
                for(ChunkSectionRenderObject renderObj : renderable.getChunkSectionRenderObjects()){
                    if(renderObj.containsTranslucent){
                        this.translucentChunkSectionRenderObjects.remove(renderObj);
                    }else{
                        this.opaqueChunkSectionRenderObjects.remove(renderObj);
                    }
                    toDestroy.add(renderObj);
                }
                if (renderable.hasChunkRenderObject()) {
                    this.opaqueChunkRenderObjects.remove(renderable.getChunkRenderObject());
                    this.toDestroy.add(renderable.getChunkRenderObject());
                }
                this.otherRenderObjects.removeAll(renderable.getMiscRenderObjects());
                this.toDestroy.addAll(renderable.getMiscRenderObjects());
            }
            this.chunkRenderablesToRemove.clear();
            
            //add any other render objects
            if (!this.renderObjectsToAdd.isEmpty()) {
                this.unchangedSinceLastSort = false;
            }
            this.otherRenderObjects.addAll(this.renderObjectsToAdd);
            this.renderObjectsToAdd.clear();
            
            //remove any other render objects, this must be done after additions incase the same object is added and removed
            if (!this.renderObjectsToRemove.isEmpty()) {
                this.unchangedSinceLastSort = false;
            }
            this.otherRenderObjects.removeAll(this.renderObjectsToRemove);
            toDestroy.addAll(this.renderObjectsToRemove);
            this.renderObjectsToRemove.clear();
        }
        destroyOldRenderObjects();
    }
    
    public void renderRenderables(Camera camera) {
        for(ChunkSectionRenderObject renObj : this.opaqueChunkSectionRenderObjects){
            renObj.render();
        }
        for(ChunkRenderObject renObj : this.opaqueChunkRenderObjects){
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
    
    private void destroyOldRenderObjects(){
        for(RenderObject renderObj : toDestroy){
            renderObj.destroy();
        }
        toDestroy.clear();
    }
    
}
