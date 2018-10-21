
package com.atomicedit.backend;

import com.atomicedit.backend.chunk.ChunkCoord;
import com.atomicedit.backend.chunk.Chunk;
import com.atomicedit.backend.chunk.ChunkController;
import com.atomicedit.backend.chunk.ChunkControllerFactory;
import com.atomicedit.backend.chunk.ChunkReader;
import com.atomicedit.backend.worldformats.MinecraftAnvilWorldFormat;
import com.atomicedit.backend.worldformats.WorldFormat;
import com.atomicedit.operations.Operation;
import com.atomicedit.operations.OperationResult;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

/**
 *
 * @author Justin Bonner
 */
public class World {
    
    /**
     * Hold chunks that have been modified but not yet saved to disk.
     */
    private final TreeMap<ChunkCoord, ChunkController> unsavedChunkMap;
    private final Stack<Operation> operationHistory;
    private final String filepath;
    
    
    public World(String filepath){
        this.unsavedChunkMap = new TreeMap<>();
        this.operationHistory = new Stack<>();
        this.filepath = filepath;
    }
    
    public OperationResult saveChanges(){
        //save chunks and remove from map
        //do lighting calc
        throw new UnsupportedOperationException();
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
    
}
