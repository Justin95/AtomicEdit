
package atomicedit.frontend.worldmaintinance;

import atomicedit.AtomicEdit;
import atomicedit.backend.BackendController;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.ChunkReader;
import atomicedit.backend.dimension.Dimension;
import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.frontend.render.Camera;
import atomicedit.frontend.render.ChunkRenderable;
import atomicedit.logging.Logger;
import atomicedit.settings.AtomicEditSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This thread is responsible for keeping the set of loaded chunks up to date.
 * It must load new chunks as they come into range, update chunks in the renderer as they are updated
 * in the backend, and unload chunks as they are no longer in range.
 * This thread only affects the visual loading of chunks on the front end, it does not effect chunks kept in memory in the backend.
 * 
 * @author Justin Bonner
 */
public class ChunkLoadingThread extends Thread {
    
    private static final long CHUNK_LOAD_THREAD_REST_TIME_MS = 50;
    private static final String CHUNK_LOAD_THREAD_NAME = "Chunk Loading Thread";
    private final AtomicEditRenderer renderer;
    private String worldPath;
    private final Map<ChunkCoord, ChunkRenderable> loadedChunks;
    private Dimension drawnDimension;
    private volatile boolean keepRunning;
    private int renderDistInChunks;
    
    public ChunkLoadingThread(AtomicEditRenderer renderer){
        this.renderer = renderer;
        this.loadedChunks = new HashMap<>();
        this.keepRunning = true;
        this.renderDistInChunks = AtomicEdit.getSettings().getSettingValueAsInt(AtomicEditSettings.CHUNK_RENDER_DISTANCE);
        this.setName(CHUNK_LOAD_THREAD_NAME);
        this.worldPath = AtomicEdit.getBackendController().getWorldPath();
        this.drawnDimension = Dimension.DEFAULT_DIMENSION;
    }
    
    @Override
    public void run(){
        BackendController backendController = AtomicEdit.getBackendController();
        while(keepRunning){
            Camera camera = renderer.getCamera();
            if(!backendController.hasWorld()) continue;
            if(camera == null) continue;
            ChunkCoord cameraCoord = ChunkCoord.getInstanceFromWorldPos(camera.getPosition().x, camera.getPosition().z);
            ChunkCoord maxCoord = ChunkCoord.getInstance(cameraCoord.x + renderDistInChunks, cameraCoord.z + renderDistInChunks);
            ChunkCoord minCoord = ChunkCoord.getInstance(cameraCoord.x - renderDistInChunks, cameraCoord.z - renderDistInChunks);
            ArrayList<ChunkRenderable> toRemove = new ArrayList<>();
            Set<ChunkCoord> neededChunks = new HashSet<>();
            String currWorldPath = backendController.getWorldPath();
            if(currWorldPath != null && !currWorldPath.equals(worldPath)){
                toRemove.addAll(loadedChunks.values());
                this.loadedChunks.clear();
                this.worldPath = currWorldPath;
            }
            ArrayList<ChunkCoord> removeFromLoadedChunks = new ArrayList<>();
            Dimension currentDim = backendController.getActiveDimension();
            if (!currentDim.equals(this.drawnDimension)) {
                removeFromLoadedChunks.addAll(loadedChunks.keySet());
                toRemove.addAll(loadedChunks.values());
                this.drawnDimension = currentDim;
            } else {
                for(ChunkCoord coord : loadedChunks.keySet()){ 
                    if(isNotInBounds(coord, minCoord, maxCoord)){
                        removeFromLoadedChunks.add(coord);
                        toRemove.add(loadedChunks.get(coord));
                    }
                }
            }
            
            removeFromLoadedChunks.forEach((ChunkCoord coord) -> loadedChunks.remove(coord));
            for(int x = minCoord.x; x <= maxCoord.x; x++){
                for(int z = minCoord.z; z <= maxCoord.z; z++){
                    ChunkCoord coord = ChunkCoord.getInstance(x, z);
                    if(!loadedChunks.containsKey(coord)){
                        neededChunks.add(coord);
                    }else if(backendController.doesChunkNeedRedraw(coord, this.drawnDimension)){
                        toRemove.add(loadedChunks.get(coord));
                        neededChunks.add(coord);
                    }
                }
            }
            Set<ChunkCoord> neededAndAdjacentChunks = expandToAdjacentChunks(neededChunks);
            Map<ChunkCoord, ChunkReader> neededChunkReaders = null;
            try{
                neededChunkReaders = backendController.getReadOnlyChunks(neededAndAdjacentChunks, this.drawnDimension);
            }catch(Exception e){
                Logger.error("Exception while trying to load chunks for drawing", e);
            }
            if(!toRemove.isEmpty()){
                renderer.getRenderableStage().removeChunkRenderables(toRemove);
            }
            if(neededChunkReaders != null && !neededChunks.isEmpty()) {
                ChunkCoord chunkCoord = neededChunks.stream().sorted(
                    (a, b) -> (int)(a.distanceSquaredFrom(cameraCoord.x, cameraCoord.z) - b.distanceSquaredFrom(cameraCoord.x, cameraCoord.z))
                ).collect(Collectors.toList()).get(0);
                ChunkReader chunk = neededChunkReaders.get(chunkCoord);
                if(chunk == null) {
                    continue;
                }
                if(chunk.needsRedraw()){
                    chunk.clearNeedsRedraw();
                }
                ChunkReader xMinus = neededChunkReaders.get(ChunkCoord.getInstance(chunkCoord.x - 1, chunkCoord.z));
                ChunkReader xPlus =  neededChunkReaders.get(ChunkCoord.getInstance(chunkCoord.x + 1, chunkCoord.z));
                ChunkReader zMinus = neededChunkReaders.get(ChunkCoord.getInstance(chunkCoord.x, chunkCoord.z - 1));
                ChunkReader zPlus =  neededChunkReaders.get(ChunkCoord.getInstance(chunkCoord.x, chunkCoord.z + 1));
                ChunkRenderable renderable = new ChunkRenderable(chunk, xMinus, xPlus, zMinus, zPlus);
                loadedChunks.put(chunkCoord, renderable);
                renderer.getRenderableStage().addChunkRenderable(renderable);
                if (!keepRunning) {
                    break; //no need to finish this if we're shutting down
                }
            }
            if (neededChunkReaders == null || neededChunkReaders.size() <= 1) {
                //read thread if no new chunks are needed right now
                restThread();
            }
        }
    }
    
    /**
     * Test if coord in in the bounds between (inclusive) min and max coord.
     * @param coord
     * @param minCoord
     * @param maxCoord
     * @return 
     */
    private static boolean isNotInBounds(ChunkCoord coord, ChunkCoord minCoord, ChunkCoord maxCoord) {
        return coord.x > maxCoord.x || coord.x < minCoord.x || coord.z > maxCoord.z || coord.z < minCoord.z;
    }
    
    private static Set<ChunkCoord> expandToAdjacentChunks(Set<ChunkCoord> originals){
        Set<ChunkCoord> originalsAndAdjacents = new HashSet<>(originals);
        originals.forEach((ChunkCoord coord) -> {
            originalsAndAdjacents.add(ChunkCoord.getInstance(coord.x + 1, coord.z));
            originalsAndAdjacents.add(ChunkCoord.getInstance(coord.x - 1, coord.z));
            originalsAndAdjacents.add(ChunkCoord.getInstance(coord.x, coord.z + 1));
            originalsAndAdjacents.add(ChunkCoord.getInstance(coord.x, coord.z - 1));
        });
        return originalsAndAdjacents;
    }
    
    private static void restThread(){
        try{
            Thread.sleep(CHUNK_LOAD_THREAD_REST_TIME_MS);
        }catch(Exception e){

        }
    }
    
    public void shutdown(){
        this.keepRunning = false;
    }
    
    public void setRenderDistance(int distance){
        this.renderDistInChunks = distance;
    }
}
