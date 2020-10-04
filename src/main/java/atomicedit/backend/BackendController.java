
package atomicedit.backend;

import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.ChunkReader;
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
        this.world = new World(worldDirectoryFilepath);
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
    
    public void saveChanges() throws IOException {
        if (world == null) {
            throw new IllegalStateException("Cannot save world because no world is loaded.");
        }
        world.saveChanges();
    }
    
    public Map<ChunkCoord, ChunkReader> getReadOnlyChunks(Collection<ChunkCoord> chunkCoords) throws Exception{
        return this.world.getLoadedChunkStage().getReadOnlyChunks(chunkCoords);
    }
    
    public Schematic createSchematic(WorldVolume volume) throws Exception{
        return Schematic.createSchematicFromWorld(world, volume);
    }
    
    public BlockState getBlockType(short blockRuntimeId){
        return GlobalBlockStateMap.getBlockType(blockRuntimeId);
    }
    
    public short getBlockId(BlockState blockType){
        return GlobalBlockStateMap.getBlockId(blockType);
    }
    
    public boolean doesChunkNeedRedraw(ChunkCoord chunkCoord){
        return this.world.doesChunkNeedRedraw(chunkCoord);
    }
    
    public boolean doesChunkNeedSaving(ChunkCoord chunkCoord){
        return this.world.doesChunkNeedSaving(chunkCoord);
    }
    
}
