
package atomicedit.backend;

import atomicedit.backend.chunk.Chunk;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.chunk.ChunkControllerFactory;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.ChunkReader;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.worldformats.MinecraftAnvilWorldFormat;
import atomicedit.backend.worldformats.WorldFormat;
import atomicedit.logging.Logger;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Allow different parts of the program to checkout different chunks.
 * Chunks remain loaded as long as there is a reference out to them.
 * @author Justin Bonner
 */
public class LoadedChunkStage {
    
    private final String dimensionFilepath;
    /**
     * A list of chunk controllers. This should be sorted later for look up speed. TODO
     */
    private final ArrayList<WeakReference<ChunkController>> controllerList;
    private final Object STAGE_LOCK = new Object();
    
    public LoadedChunkStage(String dimensionFilepath){
        this.dimensionFilepath = dimensionFilepath;
        this.controllerList = new ArrayList<>();
    }
    
    private void purgeControllerList(){
        synchronized(STAGE_LOCK){
            List<WeakReference<ChunkController>> toRemove = new ArrayList<>();
            for(WeakReference<ChunkController> controllerRef : controllerList){
                if(controllerRef.get() == null){
                    toRemove.add(controllerRef);
                }
            }
            controllerList.removeAll(toRemove);
            //Logger.info("Purged " + toRemove.size() + " chunks from cache. Chunk cache now has " + controllerList.size() + " chunks.");
        }
    }
    
    private void addChunkControllerToCache(ChunkController controller) {
        WeakReference<ChunkController> controllerRef = new WeakReference<>(controller);
        synchronized(STAGE_LOCK){
            this.controllerList.add(controllerRef);
        }
    }
    
    private Map<ChunkCoord, ChunkController> getCachedChunkControllers(Collection<ChunkCoord> chunkCoords) {
        purgeControllerList();
        Map<ChunkCoord, ChunkController> controllers = new HashMap<>();
        synchronized(STAGE_LOCK){
            CoordLoop:
            for(ChunkCoord coord : chunkCoords){
                for(WeakReference<ChunkController> controllerRef : controllerList){
                    ChunkController controller = controllerRef.get();
                    if(controller == null){
                        continue;
                    }
                    ChunkCoord controllerCoord;
                    try {
                        controllerCoord = controller.getChunkCoord();
                    } catch (MalformedNbtTagException e) {
                        Logger.error("Could not read chunk coord while trying to get cached chunk controller.", e);
                        continue;
                    }
                    if(controllerCoord.equals(coord)){
                        controllers.put(coord, controller);
                        continue CoordLoop;
                    }
                }
                controllers.put(coord, null);
            }
        }
        
        return controllers;
    }
    
    /**
     * Get chunk controllers for the chunks at each of the given coords. If a chunk cannot be
     * loaded / parsed then it's controller will be null.
     * @param chunkCoords the chunk coordinates of chunks to load
     * @return
     */
    public Map<ChunkCoord, ChunkController> getMutableChunks(Collection<ChunkCoord> chunkCoords) {
        Map<ChunkCoord, ChunkController> chunkControllers = getCachedChunkControllers(chunkCoords);
        Set<ChunkCoord> chunksToReadIn = new HashSet<>(); //no duplicates allowed
        for(ChunkCoord coord : chunkCoords){
            if(chunkControllers.get(coord) == null){
                chunksToReadIn.add(coord);
            }
        }
        Map<ChunkCoord, ChunkController> readInControllers = getChunkControllers(chunksToReadIn);
        for(ChunkCoord coord : readInControllers.keySet()){
            ChunkController controller = readInControllers.get(coord);
            chunkControllers.put(coord, controller);
            addChunkControllerToCache(controller);
        }
        return chunkControllers;
    }
    
    public Map<ChunkCoord, ChunkReader> getReadOnlyChunks(Collection<ChunkCoord> chunkCoords) {
        return (Map<ChunkCoord, ChunkReader>)(Map)getMutableChunks(chunkCoords);
    }
    
    private Map<ChunkCoord, ChunkController> getChunkControllers(Collection<ChunkCoord> chunkCoords) {
        Map<ChunkCoord, ChunkController> coordToController = new HashMap<>();
        Map<ChunkCoord, Chunk> coordToChunk = readChunks(chunkCoords);
        for(ChunkCoord coord : coordToChunk.keySet()){
            ChunkController controller = null;
            try {
                controller = ChunkControllerFactory.getChunkController(coordToChunk.get(coord));
            } catch (MalformedNbtTagException e) {
                Logger.notice("Cannot load chunk at " + coord, e);
            }
            coordToController.put(coord, controller);
        }
        return coordToController;
    }
    
    private Map<ChunkCoord, Chunk> readChunks(Collection<ChunkCoord> chunkCoords) {
        WorldFormat worldFormat = new MinecraftAnvilWorldFormat(dimensionFilepath);
        Map<ChunkCoord, Chunk> coordToChunk = worldFormat.readChunks(chunkCoords);
        return coordToChunk;
    }
    
}
