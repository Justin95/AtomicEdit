
package atomicedit.operations.nbt;

import atomicedit.backend.World;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.utils.ChunkUtils;
import atomicedit.operations.Operation;
import atomicedit.operations.OperationResult;
import atomicedit.volumes.WorldVolume;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is an NBT editing operation. NBT editing operations are special because they do not
 * appear in the regular list of operations.
 * @author Justin Bonner
 */
public class EditBlockEntityOperation extends Operation {
    
    private final WorldVolume volume;
    private final List<BlockEntity> originals;
    private final List<BlockEntity> changed;
    
    /**
     * Create an EditBlockEntityOperation. The Block Entities in each list must corrispond with
     * the block entity in the other list.
     * @param volume
     * @param originalBlockEntities
     * @param changedBlockEntities 
     */
    public EditBlockEntityOperation(WorldVolume volume, List<BlockEntity> originalBlockEntities, List<BlockEntity> changedBlockEntities) {
        this.volume = volume;
        this.originals = originalBlockEntities;
        this.changed = changedBlockEntities;
    }
    
    @Override
    protected OperationResult doOperation(World world) throws Exception {
        Map<ChunkCoord, ChunkController> chunkControllers = world.getLoadedChunkStage(operationDimension).getMutableChunks(getChunkCoordsInOperation());
        /*
        Heads up, if a block entity's position is changed to outside the operation volume then it wont save.
        This could be fixed by calculating what chunks are needed for the block entities' new positions
        and using them instead. The getWorldVolume method would also need to include these chunks.
        For now we'll just say thats an unsupported use case. Block entities can be moved by copying and pasting
        schematics anyway.
        */
        ChunkUtils.removeBlockEntitiesFromChunks(chunkControllers, originals);
        ChunkUtils.writeBlockEntitiesIntoChunks(chunkControllers, changed);
        return new OperationResult(true);
    }
    
    @Override
    protected OperationResult undoOperation(World world) throws Exception {
        Collection<ChunkController> chunkControllers = world.getLoadedChunkStage(operationDimension).getMutableChunks(getChunkCoordsInOperation()).values();
        ChunkUtils.removeBlockEntitiesFromChunks(chunkControllers, changed);
        ChunkUtils.writeBlockEntitiesIntoChunks(chunkControllers, originals);
        return new OperationResult(true);
    }
    
    @Override
    public WorldVolume getWorldVolume() {
        return this.volume;
    }
    
    
    
}
