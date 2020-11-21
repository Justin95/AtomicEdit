
package atomicedit.operations.nbt;

import atomicedit.backend.World;
import atomicedit.backend.entity.Entity;
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
public class EditEntityOperation extends Operation {
    
    private final WorldVolume volume;
    private List<Entity> originals;
    private final List<Entity> changes;
    private final boolean replaceExisting;
    
    /**
     * Create an EditEntityOperation.
     * @param volume
     * @param blockEntities 
     * @param replaceExisting true if all entities in this volume should be removed when the new ones are added
     */
    public EditEntityOperation(WorldVolume volume, List<Entity> blockEntities, boolean replaceExisting) {
        this.volume = volume;
        this.changes = blockEntities;
        this.replaceExisting = replaceExisting;
    }
    
    @Override
    protected OperationResult doOperation(World world) throws Exception {
        Map<ChunkCoord, ChunkController> chunkControllers = world.getLoadedChunkStage(operationDimension).getMutableChunks(getChunkCoordsInOperation());
        if (originals == null) {
            originals = ChunkUtils.readEntitiesFromChunkControllers(chunkControllers.values(), volume);
        }
        /*
        Heads up, if an entity's position is changed to outside the operation volume then it wont save.
        This could be fixed by calculating what chunks are needed for the entities' new positions
        and using them instead. The getWorldVolume method would also need to include these chunks.
        For now we'll just say thats an unsupported use case. Entities can be moved by copying and pasting
        schematics anyway.
        */
        if (replaceExisting) {
            ChunkUtils.removeEntitiesFromChunks(chunkControllers, originals);
        }
        ChunkUtils.writeEntitiesIntoChunks(chunkControllers, changes);
        return new OperationResult(true);
    }
    
    @Override
    protected OperationResult undoOperation(World world) throws Exception {
        Collection<ChunkController> chunkControllers = world.getLoadedChunkStage(operationDimension).getMutableChunks(getChunkCoordsInOperation()).values();
        ChunkUtils.removeEntitiesFromChunks(chunkControllers, changes);
        if (replaceExisting) {
            ChunkUtils.writeEntitiesIntoChunks(chunkControllers, originals);
        }
        return new OperationResult(true);
    }
    
    @Override
    public WorldVolume getWorldVolume() {
        return this.volume;
    }
    
    
    
}
