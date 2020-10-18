
package atomicedit.backend;

import atomicedit.backend.chunk.Chunk;
import atomicedit.backend.chunk.ChunkCoord;
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
    private final ArrayList<WeakReference<Chunk>> chunkList;
    private final Object STAGE_LOCK = new Object();
    
    public LoadedChunkStage(String worldFilepath){
        this.worldFilepath = worldFilepath;
        this.chunkList = new ArrayList<>();
    }
    
    private void purgeChunkList(){
        synchronized(STAGE_LOCK){
            List<WeakReference<Chunk>> toRemove = new ArrayList<>();
            for(WeakReference<Chunk> chunkRef : chunkList){
                if(chunkRef.get() == null){
                    toRemove.add(chunkRef);
                }
            }
            chunkList.removeAll(toRemove);
            //Logger.info("Purged " + toRemove.size() + " chunks from cache. Chunk cache now has " + controllerList.size() + " chunks.");
        }
    }
    
    private void addChunkControllerToCache(Chunk chunk) throws MalformedNbtTagException{
        WeakReference<Chunk> controllerRef = new WeakReference<>(chunk);
        synchronized(STAGE_LOCK){
            this.chunkList.add(controllerRef);
        }
    }
    
    private Map<ChunkCoord, Chunk> getCachedChunks(Collection<ChunkCoord> chunkCoords) throws MalformedNbtTagException{
        purgeChunkList();
        Map<ChunkCoord, Chunk> chunks = new HashMap<>();
        synchronized(STAGE_LOCK){
            CoordLoop:
            for(ChunkCoord coord : chunkCoords){
                for(WeakReference<Chunk> chunkRef : chunkList){
                    Chunk chunk = chunkRef.get();
                    if(chunk == null){
                        continue;
                    }
                    if(chunk.getChunkCoord().equals(coord)){
                        chunks.put(coord, chunk);
                        continue CoordLoop;
                    }
                }
                chunks.put(coord, null);
            }
        }
        
        return chunks;
    }
    
    public Map<ChunkCoord, Chunk> getMutableChunks(Collection<ChunkCoord> chunkCoords) throws MalformedNbtTagException{
        Map<ChunkCoord, Chunk> chunkControllers = getCachedChunks(chunkCoords);
        List<ChunkCoord> chunksToReadIn = new ArrayList<>();
        for(ChunkCoord coord : chunkCoords){
            if(chunkControllers.get(coord) == null){
                chunksToReadIn.add(coord);
            }
        }
        Map<ChunkCoord, Chunk> readInControllers = readChunks(chunksToReadIn); //TODO will need to make sure the same chunk cant be read in twice here
        for(ChunkCoord coord : readInControllers.keySet()){
            Chunk controller = readInControllers.get(coord);
            chunkControllers.put(coord, controller);
            addChunkControllerToCache(controller);
        }
        return chunkControllers;
    }
    
    public Map<ChunkCoord, Chunk> getChunks(Collection<ChunkCoord> chunkCoords) throws MalformedNbtTagException {
        return getMutableChunks(chunkCoords);
    }
    
    
    private Map<ChunkCoord, Chunk> readChunks(Collection<ChunkCoord> chunkCoords) throws MalformedNbtTagException{
        WorldFormat worldFormat = new MinecraftAnvilWorldFormat(worldFilepath);
        Map<ChunkCoord, Chunk> coordToChunk = worldFormat.readChunks(chunkCoords);
        return coordToChunk;
    }
    
}
