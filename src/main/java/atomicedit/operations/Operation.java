
package atomicedit.operations;

import atomicedit.backend.World;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.logging.Logger;
import atomicedit.volumes.WorldVolume;
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
            try{
                return doOperation(world);
            }catch(Exception e){
                String message = "Exception while doing operation.";
                Logger.warning(message, e);
                return new OperationResult(false, message);
            }
        }
    }
    
    protected abstract OperationResult doOperation(World world) throws Exception;
    
    public final OperationResult undoSynchronizedOperation(World world){
        synchronized(OPERATION_LOCK){
            try{
                return undoOperation(world);
            }catch(Exception e){
                String message = "Exception while undoing operation.";
                Logger.warning(message, e);
                return new OperationResult(false, message);
            }
        }
    }
    
    protected abstract OperationResult undoOperation(World world) throws Exception;
    
    public abstract WorldVolume getWorldVolume();
    
    public Collection<ChunkCoord> getChunkCoordsInOperation(){
        return getWorldVolume().getContainedChunkCoords();
    }
    
}
