
package atomicedit.backend;

import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.blockentity.BlockEntityUtils;
import atomicedit.backend.blockprovider.BlockProvider;
import atomicedit.backend.blockprovider.SchematicBlockProvider;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.Chunk;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.dimension.Dimension;
import atomicedit.backend.entity.Entity;
import atomicedit.backend.entity.EntityUtils;
import atomicedit.backend.lighting.LightingUtil;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.schematic.Schematic;
import atomicedit.backend.utils.ChunkUtils;
import atomicedit.backend.worldformats.CorruptedRegionFileException;
import atomicedit.backend.worldformats.MinecraftAnvilWorldFormat;
import atomicedit.backend.worldformats.WorldFormat;
import atomicedit.logging.Logger;
import atomicedit.operations.Operation;
import atomicedit.operations.OperationResult;
import atomicedit.utils.FileUtils;
import atomicedit.volumes.WorldVolume;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collection;
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
    private final Map<Dimension, Map<ChunkCoord, ChunkController>> dimToUnsavedChunkMap;
    private final Stack<Operation> operationHistory;
    private final Stack<Operation> undoHistory;
    private static final int MAX_UNDO_OPS = 20;
    private final String filepath;
    private long sessionStartTime;
    private Dimension activeDimension;
    private Map<Dimension, LoadedChunkStage> chunkStages;
    
    
    public World(String filepath) throws SessionLockException {
        this.dimToUnsavedChunkMap = new HashMap<>();
        this.operationHistory = new Stack<>();
        this.undoHistory = new Stack<>();
        this.filepath = filepath;
        this.sessionStartTime = ZonedDateTime.now().toInstant().toEpochMilli();
        writeSessionLock(sessionStartTime, filepath);
        this.chunkStages = new HashMap<>();
        for (Dimension dim : Dimension.getDimensions()) {
            chunkStages.put(dim, new LoadedChunkStage(FileUtils.concatPaths(filepath, dim.getSubPathToDimFolder())));
            dimToUnsavedChunkMap.put(dim, new HashMap<>());
        }
        this.activeDimension = Dimension.DEFAULT_DIMENSION;
    }
    
    public void saveChanges() throws IOException, SessionLockException {
        //save chunks and remove from unsaved map
        doLightingCalculation();
        for (Map<ChunkCoord, ChunkController> chunkMap : dimToUnsavedChunkMap.values()) {
            for (ChunkController chunk : chunkMap.values()) {
                chunk.flushCacheToChunkNbt();
            }
        }
        if (!isSessionValid()) {
            Logger.warning("Session lock is invalid. The world will not be saved, and the session lock will be reaquired.");
            this.sessionStartTime = ZonedDateTime.now().toInstant().toEpochMilli();
            writeSessionLock(sessionStartTime, filepath);
            return;
        }
        for (Dimension dimension : this.dimToUnsavedChunkMap.keySet()) {
            WorldFormat worldFormat = new MinecraftAnvilWorldFormat(FileUtils.concatPaths(filepath, dimension.getSubPathToDimFolder()));
            try{
                worldFormat.writeChunks(this.dimToUnsavedChunkMap.get(dimension));
            }catch(CorruptedRegionFileException e){
                Logger.error("Bad region file.", e);
            }
        }
        for (Map<ChunkCoord, ChunkController> unsavedChunkMap : dimToUnsavedChunkMap.values()) {
            unsavedChunkMap.clear();
        }
    }
    
    /**
     * Perform a lighting calculation on all unsaved modified chunks and their adjacent chunks.
     * Chunks that are modified by this lighting calculation will be added to the unsaved chunk map.
     */
    public void doLightingCalculation(){
        Logger.info("Beginning lighting calc.");
        for (Dimension dimension : this.dimToUnsavedChunkMap.keySet()) {
            List<ChunkCoord> chunkCoordsToLight = ChunkCoord.expandToAdjacentCoords(this.dimToUnsavedChunkMap.get(dimension).entrySet().stream().filter(
                (entry) -> entry.getValue().needsLightingCalc()
            ).map(
                (entry) -> entry.getKey()
            ).collect(Collectors.toList()));
            if(chunkCoordsToLight.isEmpty()) {
                continue;
            }
            Map<ChunkCoord, ChunkController> toLight;
            try {
                toLight = this.getLoadedChunkStage(dimension).getMutableChunks(chunkCoordsToLight);
                LightingUtil.doLightingCalculation(toLight);
            } catch(MalformedNbtTagException e) {
                Logger.error("Cannot finish lighting calculation.", e);
                return; //this will leave the chunks unlit
            }
        }
        Logger.info("Finished lighting calc.");
    }
    
    public OperationResult doOperation(Operation op){
        OperationResult result = op.doSynchronizedOperation(this);
        Map<ChunkCoord, ChunkController> operationChunks;
        try{
            operationChunks = getLoadedChunkStage(op.getOperationDimension()).getMutableChunks(op.getChunkCoordsInOperation());
        }catch(MalformedNbtTagException e){
            return new OperationResult(false, e);
        }
        operationChunks.forEach((ChunkCoord coord, ChunkController chunkController) -> {
            if(chunkController.getChunk().needsSaving()){
                this.dimToUnsavedChunkMap.get(op.getOperationDimension()).put(coord, chunkController);
            }
        });
        if(operationHistory.size() >= MAX_UNDO_OPS){
            operationHistory.remove(0); //hopefully 0 is bottom of the stack?
        }
        operationHistory.push(op);
        undoHistory.clear();
        return result;
    }
    
    public OperationResult undoLastOperation(){
        if(operationHistory.empty()){
            return new OperationResult(false, "No operations to undo");
        }
        Operation lastOp = operationHistory.pop();
        OperationResult result = lastOp.undoSynchronizedOperation(this);
        Map<ChunkCoord, ChunkController> operationChunks;
        try{
            operationChunks = getLoadedChunkStage(lastOp.getOperationDimension()).getMutableChunks(lastOp.getChunkCoordsInOperation());
        }catch(MalformedNbtTagException e){
            return new OperationResult(false, e);
        }
        operationChunks.forEach((ChunkCoord coord, ChunkController chunkController) -> {
            if(chunkController.getChunk().needsSaving()){
                this.dimToUnsavedChunkMap.get(lastOp.getOperationDimension()).put(coord, chunkController);
            }
        });
        if(undoHistory.size() >= MAX_UNDO_OPS){
            undoHistory.remove(0); //hopefully 0 is bottom of the stack?
        }
        undoHistory.push(lastOp);
        return result;
    }
    
    public OperationResult redoLastUndo() {
        if(undoHistory.empty()){
            return new OperationResult(false, "No operations to redo");
        }
        Operation lastOp = undoHistory.pop();
        OperationResult result = lastOp.doSynchronizedOperation(this);
        Map<ChunkCoord, ChunkController> operationChunks;
        try{
            operationChunks = getLoadedChunkStage(lastOp.getOperationDimension()).getMutableChunks(lastOp.getChunkCoordsInOperation());
        }catch(MalformedNbtTagException e){
            return new OperationResult(false, e);
        }
        operationChunks.forEach((ChunkCoord coord, ChunkController chunkController) -> {
            if(chunkController.getChunk().needsSaving()){
                this.dimToUnsavedChunkMap.get(lastOp.getOperationDimension()).put(coord, chunkController);
            }
        });
        if(operationHistory.size() >= MAX_UNDO_OPS){
            operationHistory.remove(0); //hopefully 0 is bottom of the stack?
        }
        operationHistory.push(lastOp);
        return result;
    }
    
    public OperationResult putSchematicIntoWorld(Schematic schematic, Dimension dim, BlockCoord smallestCoord) throws Exception {
        BlockProvider provider = new SchematicBlockProvider(schematic);
        WorldVolume volume = new WorldVolume(schematic.volume, smallestCoord);
        Map<ChunkCoord, ChunkController> chunkControllers = this.getLoadedChunkStage(dim).getMutableChunks(volume.getContainedChunkCoords());
        ChunkUtils.writeBlocksIntoChunks(chunkControllers.values(), provider, smallestCoord);
        Collection<BlockEntity> blockEntitiesToRemove = ChunkUtils.readBlockEntitiesFromChunkControllers(chunkControllers.values(), volume);
        ChunkUtils.removeBlockEntitiesFromChunks(chunkControllers, blockEntitiesToRemove);
        //Do not have to remove all entities from schematic destination
        //update entity and block entity positions
        Collection<BlockEntity> blockEntities = BlockEntityUtils.translateBlockEntityCoordsToWorld(schematic.getBlockEntities(), volume);
        Collection<Entity> entities = EntityUtils.translateEntityCoordsToWorld(schematic.getEntities(), volume);
        ChunkUtils.writeBlockEntitiesIntoChunks(chunkControllers, blockEntities);
        ChunkUtils.writeEntitiesIntoChunks(chunkControllers, entities);
        chunkControllers.forEach((ChunkCoord coord, ChunkController chunkController) -> {
            if(chunkController.getChunk().needsSaving()){
                this.dimToUnsavedChunkMap.get(dim).put(coord, chunkController);
            }
        });
        return new OperationResult(true);
    }
    
    private static void writeSessionLock(long time, String filepath) throws SessionLockException {
        String sessionPath = FileUtils.concatPaths(filepath, "session.lock");
        File file = new File(sessionPath);
        try (DataOutputStream output = new DataOutputStream(new FileOutputStream(file, false))) {
            output.writeLong(time);
        } catch (IOException e) {
            Logger.error("Exception writing session.lock file.", e);
            throw new SessionLockException("Could not write session lock.", e);
        }
        Logger.info("Wrote session lock: " + time);
    }
    
    private boolean isSessionValid() {
        String sessionPath = FileUtils.concatPaths(filepath, "session.lock");
        File file = new File(sessionPath);
        try (DataInputStream input = new DataInputStream(new FileInputStream(file))) {
            long readTime = input.readLong();
            return readTime == this.sessionStartTime;
        } catch (IOException e) {
            Logger.error("Could not read session.lock file.", e);
            return false;
        }
    }
    
    public LoadedChunkStage getLoadedChunkStage(Dimension dimension) {
        return this.chunkStages.get(dimension);
    }
    
    public Iterator<Chunk> getAllChunksInWorld(){
        throw new UnsupportedOperationException();
    }
    
    public String getFilePath(){
        return this.filepath;
    }
    
    public boolean hasUnsavedChanges(){
        for (Map<ChunkCoord, ChunkController> unsavedChunkMap : dimToUnsavedChunkMap.values()) {
            if (!unsavedChunkMap.isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    public Dimension getActiveDimension() {
        return this.activeDimension;
    }
    
    public void setActiveDimension(Dimension dimension) {
        this.activeDimension = dimension;
    }
    
    public boolean doesChunkNeedRedraw(ChunkCoord chunkCoord, Dimension dimension){
        return this.dimToUnsavedChunkMap.get(dimension).containsKey(chunkCoord) 
            && this.dimToUnsavedChunkMap.get(dimension).get(chunkCoord).needsRedraw();
    }
    
}
