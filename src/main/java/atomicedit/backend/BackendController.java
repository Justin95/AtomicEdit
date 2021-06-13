
package atomicedit.backend;

import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.ChunkReader;
import atomicedit.backend.dimension.Dimension;
import atomicedit.backend.schematic.Schematic;
import atomicedit.logging.Logger;
import atomicedit.operations.Operation;
import atomicedit.operations.OperationResult;
import atomicedit.volumes.WorldVolume;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Justin Bonner
 */
public class BackendController {
    
    private World world;
    private ReentrantLock worldLock;
    
    
    public boolean hasWorld(){
        return this.world != null;
    }
    
    public boolean worldHasUnsavedChanges(){
        return this.world.hasUnsavedChanges();
    }
    
    public void setWorld(String worldDirectoryFilepath){
        if(worldDirectoryFilepath == null || worldDirectoryFilepath.equals("")){
            Logger.notice("Tried to set null or empty file path as the world file");
            return;
        }
        try {
            this.world = new World(worldDirectoryFilepath);
        } catch (SessionLockException e) {
            Logger.error("Cannot set world.", e);
        }
    }
    
    public String getWorldPath(){
        return this.world != null ? this.world.getFilePath() : null;
    }
    
    public OperationResult applyOperation(Operation op) {
        if (world == null) {
            throw new IllegalStateException("Cannot perform operation because no world is loaded.");
        }
        return world.doOperation(op);
    }
    
    public void undoOperation(){
        if (world == null) {
            throw new IllegalStateException("Cannot undo operation because no world is loaded.");
        }
        this.world.undoLastOperation();
    }
    
    public void redoOperation() {
        if (world == null) {
            throw new IllegalStateException("Cannot undo operation because no world is loaded.");
        }
        this.world.redoLastUndo();
    }
    
    public void saveChanges() throws IOException {
        if (world == null) {
            throw new IllegalStateException("Cannot save world because no world is loaded.");
        }
        try {
            world.saveChanges();
        } catch (SessionLockException e) {
            Logger.error("Cannot save world.", e);
        }
    }
    
    public Map<ChunkCoord, ChunkReader> getReadOnlyChunks(Collection<ChunkCoord> chunkCoords, Dimension dimension) {
        return this.world.getLoadedChunkStage(dimension).getReadOnlyChunks(chunkCoords);
    }
    
    public Schematic createSchematic(WorldVolume volume) throws Exception {
        return Schematic.createSchematicFromWorld(world, world.getActiveDimension(), volume);
    }
    
    public void putSchematicIntoWorld(Schematic schematic, BlockCoord smallestCoord) throws Exception {
        Schematic.putSchematicIntoWorld(world, world.getActiveDimension(), schematic, smallestCoord);
    }
    
    public BlockState getBlockType(int blockId){
        return GlobalBlockStateMap.getBlockType(blockId);
    }
    
    public int getBlockId(BlockState blockType){
        return GlobalBlockStateMap.getBlockId(blockType);
    }
    
    public void setActiveDimension(Dimension dimension) {
        if (world == null) {
            throw new IllegalStateException("Cannot change dimension because no world is loaded.");
        }
        this.world.setActiveDimension(dimension);
    }
    
    public Dimension getActiveDimension() {
        if (world == null) {
            throw new IllegalStateException("Cannot get dimension because no world is loaded.");
        }
        return world.getActiveDimension();
    }
    
    public boolean doesChunkNeedRedraw(ChunkCoord chunkCoord, Dimension dimension){
        return this.world.doesChunkNeedRedraw(chunkCoord, dimension);
    }
    
}
