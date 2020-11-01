
package atomicedit.operations;

import atomicedit.AtomicEdit;
import atomicedit.backend.World;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.dimension.Dimension;
import atomicedit.logging.Logger;
import atomicedit.volumes.WorldVolume;
import java.util.Collection;

/**
 * This class describes a change that can be done and undone to a part of a minecraft world.
 * @author Justin Bonner
 */
public abstract class Operation {
    
    private static final Object OPERATION_LOCK = new Object();
    
    protected final Dimension operationDimension;
    
    protected Operation() {
        this.operationDimension = AtomicEdit.getBackendController().getActiveDimension();
    }
    
    public final OperationResult doSynchronizedOperation(World world){
        synchronized(OPERATION_LOCK){
            try{
                return doOperation(world);
            }catch(Exception e){
                String message = "Exception while doing operation.";
                Logger.warning(message, e);
                return new OperationResult(false, message, e);
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
                return new OperationResult(false, message, e);
            }
        }
    }
    
    protected abstract OperationResult undoOperation(World world) throws Exception;
    
    public abstract WorldVolume getWorldVolume();
    
    public Collection<ChunkCoord> getChunkCoordsInOperation(){
        return getWorldVolume().getContainedChunkCoords();
    }
    
    /**
     * Get the dimension edited in this operation. This does not change after initialization.
     * @return 
     */
    public Dimension getOperationDimension() {
        return this.operationDimension;
    }
    
}
