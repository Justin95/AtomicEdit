
package atomicedit.backend;

import atomicedit.backend.chunk.Chunk;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.chunk.ChunkControllerFactory;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.ChunkReader;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.worldformats.MinecraftAnvilWorldFormat;
import atomicedit.backend.worldformats.WorldFormat;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allow different parts of the program to checkout different chunks.
 * Chunks remain loaded as long as there is a reference out to them.
 * @author Justin Bonner
 */
public class LoadedChunkStage {
    
    private final String worldFilepath;
    /**
     * A list of chunk controllers. This should be sorted later for look up speed. TODO
     */
    private final ArrayList<WeakReference<ChunkController>> controllerList;
    private final Object STAGE_LOCK = new Object();
    
    public LoadedChunkStage(String worldFilepath){
        this.worldFilepath = worldFilepath;
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
    
    private void addChunkControllerToCache(ChunkController controller) throws MalformedNbtTagException{
        WeakReference<ChunkController> controllerRef = new WeakReference<>(controller);
        synchronized(STAGE_LOCK){
            this.controllerList.add(controllerRef);
        }
    }
    
    private Map<ChunkCoord, ChunkController> getCachedChunkControllers(Collection<ChunkCoord> chunkCoords) throws MalformedNbtTagException{
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
                    if(controller.getChunkCoord().equals(coord)){
                        controllers.put(coord, controller);
                        continue CoordLoop;
                    }
                }
                controllers.put(coord, null);
            }
        }
        
        return controllers;
    }
    
    public Map<ChunkCoord, ChunkController> getMutableChunks(Collection<ChunkCoord> chunkCoords) throws MalformedNbtTagException{
        Map<ChunkCoord, ChunkController> chunkControllers = getCachedChunkControllers(chunkCoords);
        List<ChunkCoord> chunksToReadIn = new ArrayList<>();
        for(ChunkCoord coord : chunkCoords){
            if(chunkControllers.get(coord) == null){
                chunksToReadIn.add(coord);
            }
        }
        Map<ChunkCoord, ChunkController> readInControllers = getChunkControllers(chunksToReadIn); //TODO will need to make sure the same chunk cant be read in twice here
        for(ChunkCoord coord : readInControllers.keySet()){
            ChunkController controller = readInControllers.get(coord);
            chunkControllers.put(coord, controller);
            addChunkControllerToCache(controller);
        }
        return chunkControllers;
    }
    
    public Map<ChunkCoord, ChunkReader> getReadOnlyChunks(Collection<ChunkCoord> chunkCoords) throws MalformedNbtTagException{
        return (Map<ChunkCoord, ChunkReader>)(Map)getMutableChunks(chunkCoords);
    }
    
    private Map<ChunkCoord, ChunkController> getChunkControllers(Collection<ChunkCoord> chunkCoords) throws MalformedNbtTagException{
        Map<ChunkCoord, ChunkController> coordToController = new HashMap<>();
        Map<ChunkCoord, Chunk> coordToChunk = readChunks(chunkCoords);
        for(ChunkCoord coord : coordToChunk.keySet()){
            coordToController.put(coord, ChunkControllerFactory.getChunkController(coordToChunk.get(coord)));
        }
        return coordToController;
    }
    
    private Map<ChunkCoord, Chunk> readChunks(Collection<ChunkCoord> chunkCoords) throws MalformedNbtTagException{
        WorldFormat worldFormat = new MinecraftAnvilWorldFormat(worldFilepath);
        Map<ChunkCoord, Chunk> coordToChunk = worldFormat.readChunks(chunkCoords);
        return coordToChunk;
    }
    
}
