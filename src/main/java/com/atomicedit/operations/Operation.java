
package com.atomicedit.operations;

import com.atomicedit.backend.BlockCoord;
import com.atomicedit.backend.World;
import com.atomicedit.backend.chunk.ChunkCoord;
import com.atomicedit.volumes.Volume;
import java.util.Collection;

/**
 *
 * @author Justin Bonner
 */
public abstract class Operation {
    
    public final int MAX_OPERATION_HISTORY_SIZE = 16; //make configurable later
    
    private static final Object OPERATION_LOCK = new Object();
    
    public final OperationResult doSynchronizedOperation(World world){
        synchronized(OPERATION_LOCK){
            return doOperation(world);
        }
    }
    
    protected abstract OperationResult doOperation(World world);
    
    public final OperationResult undoSynchronizedOperation(World world){
        synchronized(OPERATION_LOCK){
            return undoOperation(world);
        }
    }
    
    protected abstract OperationResult undoOperation(World world);
    
    public abstract Volume getVolume();
    
    public Collection<ChunkCoord> getChunkCoordsInOperation(BlockCoord smallestCoord){
        return getVolume().getContainedChunkCoords(smallestCoord);
    }
    
}
