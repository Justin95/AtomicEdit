
package atomicedit.operations;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.World;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.volumes.Volume;
import java.util.Collection;

/**
 * This class describes a change that can be done and undone to a part of a minecraft world.
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
