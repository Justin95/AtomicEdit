
package atomicedit.backend;

import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.Chunk;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.chunk.ChunkControllerFactory;
import atomicedit.backend.chunk.ChunkReader;
import atomicedit.backend.worldformats.MinecraftAnvilWorldFormat;
import atomicedit.backend.worldformats.WorldFormat;
import atomicedit.operations.Operation;
import atomicedit.operations.OperationResult;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 *
 * @author Justin Bonner
 */
public class World {
    
    /**
     * Hold chunks that have been modified but not yet saved to disk.
     */
    private final Map<ChunkCoord, ChunkController> unsavedChunkMap;
    private final Stack<Operation> operationHistory;
    private final String filepath;
    
    
    public World(String filepath){
        this.unsavedChunkMap = new HashMap<>();
        this.operationHistory = new Stack<>();
        this.filepath = filepath;
    }
    
    public OperationResult saveChanges(){
        //save chunks and remove from map
        //do lighting calc
        throw new UnsupportedOperationException(); //TODO
    }
    
    public OperationResult doOperation(Operation op){
        OperationResult result;
        Map<ChunkCoord, ChunkController> operationChunks = null;
        try{
            result = op.doSynchronizedOperation(this);
        }catch(Exception e){
            return new OperationResult(false, e);
        }
        if(operationChunks != null){
            operationChunks.forEach((ChunkCoord coord, ChunkController chunkController) -> {
                if(chunkController.getChunk().needsSaving()){
                    this.unsavedChunkMap.put(coord, chunkController);
                }
            });
        }
        return result;
    }
    
    public OperationResult undoLastOperation(){
        throw new UnsupportedOperationException(); //TODO
    }
    
    public Map<ChunkCoord, ChunkController> getMutableChunks(Collection<ChunkCoord> chunkCoords) throws Exception{
        WorldFormat worldFormat = new MinecraftAnvilWorldFormat(filepath);
        Map<ChunkCoord, ChunkController> coordToController = new HashMap<>();
        Map<ChunkCoord, Chunk> coordToChunk = worldFormat.readChunks(chunkCoords);
        for(ChunkCoord coord : coordToChunk.keySet()){
            coordToController.put(coord, ChunkControllerFactory.getChunkController(coordToChunk.get(coord)));
        }
        return coordToController;
    }
    
    public Map<ChunkCoord, ChunkReader> getReadOnlyChunks(Collection<ChunkCoord> chunkCoords) throws Exception{
        WorldFormat worldFormat = new MinecraftAnvilWorldFormat(filepath);
        Map<ChunkCoord, ChunkReader> coordToController = new HashMap<>();
        Map<ChunkCoord, Chunk> coordToChunk = worldFormat.readChunks(chunkCoords);
        for(ChunkCoord coord : coordToChunk.keySet()){
            coordToController.put(coord, ChunkControllerFactory.getChunkController(coordToChunk.get(coord)));
        }
        return coordToController;
    }
    
    public Iterator<Chunk> getAllChunksInWorld(){
        throw new UnsupportedOperationException();
    }
    
    public String getFilePath(){
        return this.filepath;
    }
    
    public boolean hasUnsavedChanges(){
        return this.unsavedChunkMap.isEmpty();
    }
    
    public boolean doesChunkNeedSaving(ChunkCoord chunkCoord){
        return this.unsavedChunkMap.containsKey(chunkCoord) && this.unsavedChunkMap.get(chunkCoord).needsSaving();
    }
    
    public boolean doesChunkNeedRedraw(ChunkCoord chunkCoord){
        return this.unsavedChunkMap.containsKey(chunkCoord) && this.unsavedChunkMap.get(chunkCoord).needsRedraw();
    }
    
    public boolean doesChunkNeedLightingCalc(ChunkCoord chunkCoord){
        return this.unsavedChunkMap.containsKey(chunkCoord) && this.unsavedChunkMap.get(chunkCoord).needsLightingCalc();
    }
    
}
