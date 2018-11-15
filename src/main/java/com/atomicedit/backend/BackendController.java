
package com.atomicedit.backend;

import com.atomicedit.backend.chunk.ChunkCoord;
import com.atomicedit.backend.chunk.ChunkReader;
import com.atomicedit.backend.schematic.Schematic;
import com.atomicedit.operations.Operation;
import com.atomicedit.operations.OperationResult;
import com.atomicedit.volumes.Volume;
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
        this.world = new World(worldDirectoryFilepath);
    }
    
    public OperationResult applyOperation(Operation op){
        return world.doOperation(op);
    }
    
    public void undoOperation(){
        this.world.undoLastOperation();
    }
    
    public void saveChanges(){
        world.saveChanges();
    }
    
    public Map<ChunkCoord, ChunkReader> getReadOnlyChunks(Collection<ChunkCoord> chunkCoords) throws Exception{
        return this.world.getReadOnlyChunks(chunkCoords);
    }
    
    public Schematic createSchematic(Volume volume, BlockCoord smallestPoint){
        return Schematic.createSchematicFromWorld(world, volume, smallestPoint);
    }
    
    
    
}
