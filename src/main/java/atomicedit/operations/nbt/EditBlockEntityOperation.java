
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
    private List<BlockEntity> originals;
    private final List<BlockEntity> changes;
    private final boolean replaceExisting;
    
    /**
     * Create an EditBlockEntityOperation.
     * @param volume
     * @param blockEntities 
     * @param replaceExisting true if all block entities in this volume should be removed when the new ones are added
     */
    public EditBlockEntityOperation(WorldVolume volume, List<BlockEntity> blockEntities, boolean replaceExisting) {
        this.volume = volume;
        this.changes = blockEntities;
        this.replaceExisting = replaceExisting;
    }
    
    @Override
    protected OperationResult doOperation(World world) throws Exception {
        Map<ChunkCoord, ChunkController> chunkControllers = world.getLoadedChunkStage(operationDimension).getMutableChunks(getChunkCoordsInOperation());
        if (originals == null) {
            originals = ChunkUtils.readBlockEntitiesFromChunkControllers(chunkControllers.values(), volume);
        }
        /*
        Heads up, if a block entity's position is changed to outside the operation volume then it wont save.
        This could be fixed by calculating what chunks are needed for the block entities' new positions
        and using them instead. The getWorldVolume method would also need to include these chunks.
        For now we'll just say thats an unsupported use case. Block entities can be moved by copying and pasting
        schematics anyway.
        */
        if (replaceExisting) {
            ChunkUtils.removeBlockEntitiesFromChunks(chunkControllers, originals);
        }
        ChunkUtils.writeBlockEntitiesIntoChunks(chunkControllers, changes);
        return new OperationResult(true);
    }
    
    @Override
    protected OperationResult undoOperation(World world) throws Exception {
        Collection<ChunkController> chunkControllers = world.getLoadedChunkStage(operationDimension).getMutableChunks(getChunkCoordsInOperation()).values();
        ChunkUtils.removeBlockEntitiesFromChunks(chunkControllers, changes);
        if (replaceExisting) {
            ChunkUtils.writeBlockEntitiesIntoChunks(chunkControllers, originals);
        }
        return new OperationResult(true);
    }
    
    @Override
    public WorldVolume getWorldVolume() {
        return this.volume;
    }
    
    
    
}
