
package atomicedit.backend;

import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.Chunk;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.lighting.LightingUtil;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.worldformats.CorruptedRegionFileException;
import atomicedit.backend.worldformats.MinecraftAnvilWorldFormat;
import atomicedit.backend.worldformats.WorldFormat;
import atomicedit.logging.Logger;
import atomicedit.operations.Operation;
import atomicedit.operations.OperationResult;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 *
 * @author Justin Bonner
 */
public class World {
    
    /**
     * Hold chunks that have been modified but not yet saved to disk.
     */
    private final Map<ChunkCoord, ChunkController> unsavedChunkMap;
    private final LoadedChunkStage loadedChunkStage;
    private final Stack<Operation> operationHistory;
    private static final int MAX_UNDO_OPS = 20;
    private final String filepath;
    
    
    public World(String filepath){
        this.unsavedChunkMap = new HashMap<>();
        this.operationHistory = new Stack<>();
        this.filepath = filepath;
        this.loadedChunkStage = new LoadedChunkStage(filepath);
    }
    
    public void saveChanges() throws IOException{
        //save chunks and remove from unsaved map
        doLightingCalculation();
        WorldFormat worldFormat = new MinecraftAnvilWorldFormat(filepath);
        try{
            worldFormat.writeChunks(unsavedChunkMap);
        }catch(CorruptedRegionFileException e){
            Logger.error("Bad region file.", e);
        }
        unsavedChunkMap.clear();
    }
    
    /**
     * Perform a lighting calculation on all unsaved modified chunks and their adjacent chunks.
     * Chunks that are modified by this lighting calculation will be added to the unsaved chunk map.
     */
    public void doLightingCalculation(){
        Logger.info("Beginning lighting calc.");
        List<ChunkCoord> chunkCoordsToLight = ChunkCoord.expandToAdjacentCoords(unsavedChunkMap.entrySet().stream().filter(
            (entry) -> entry.getValue().needsLightingCalc()
        ).map(
            (entry) -> entry.getKey()
        ).collect(Collectors.toList()));
        if(chunkCoordsToLight.isEmpty()) {
            return;
        }
        Map<ChunkCoord, ChunkController> toLight;
        try {
            toLight = this.loadedChunkStage.getMutableChunks(chunkCoordsToLight);
        } catch(MalformedNbtTagException e) {
            Logger.error("Exception while trying to do lighting calculation.", e);
            return; //this will leave the chunks unlit
        }
        LightingUtil.doLightingCalculation(toLight);
        for(ChunkController chunk : toLight.values()) {
            chunk.clearNeedsLightingCalc();
        }
        Logger.info("Finished lighting calc.");
    }
    
    public OperationResult doOperation(Operation op){
        OperationResult result = op.doSynchronizedOperation(this);
        Map<ChunkCoord, ChunkController> operationChunks;
        try{
            operationChunks = loadedChunkStage.getMutableChunks(op.getChunkCoordsInOperation());
        }catch(MalformedNbtTagException e){
            return new OperationResult(false, e);
        }
        operationChunks.forEach((ChunkCoord coord, ChunkController chunkController) -> {
            if(chunkController.getChunk().needsSaving()){
                this.unsavedChunkMap.put(coord, chunkController);
            }
        });
        if(operationHistory.size() >= MAX_UNDO_OPS){
            operationHistory.remove(0); //hopefully 0 is bottom of the stack?
        }
        operationHistory.push(op);
        return result;
    }
    
    public OperationResult undoLastOperation(){
        if(operationHistory.empty()){
            return new OperationResult(false, "No operations to undo");
        }
        Operation lastOp = operationHistory.pop();
        OperationResult result;
        try{
            result = lastOp.undoSynchronizedOperation(this);
        }catch(Exception e){
            return new OperationResult(false, e);
        }
        return result;
    }
    
    public LoadedChunkStage getLoadedChunkStage(){
        return this.loadedChunkStage;
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
