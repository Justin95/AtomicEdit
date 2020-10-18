
package atomicedit.backend.chunk;

import atomicedit.backend.nbt.NbtCompoundTag;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.entity.Entity;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public class ChunkParserV1 implements ChunkParser {
    
    public static final ChunkParserV1 INSTANCE = new ChunkParserV1();
    
    private ChunkParserV1() {
        
    }
    
    public static ChunkParserV1 getInstance() {
        return INSTANCE;
    }
    
    @Override
    public Chunk parseChunk(NbtCompoundTag chunkTag) throws MalformedNbtTagException {
        ChunkNbtInterpreter nbtInterpreter = ChunkControllerFactory.getChunkNbtInterpreter(chunkTag);
        ChunkSection[] chunkSections = nbtInterpreter.getChunkSections(chunkTag);
        ChunkCoord chunkCoord = nbtInterpreter.getChunkCoord(chunkTag);
        boolean cubicBiomes = nbtInterpreter.usesCubicBiomes();
        List<Entity> entities = nbtInterpreter.getEntities(chunkTag);
        List<BlockEntity> blockEntities = nbtInterpreter.getBlockEntities(chunkTag);
        int[] biomes = nbtInterpreter.getBiomes(chunkTag);
        return new Chunk(chunkCoord, cubicBiomes, chunkSections, chunkTag, entities, blockEntities, biomes);
        
    }
    
    @Override
    public NbtCompoundTag writeToNbt(Chunk chunk) throws MalformedNbtTagException {
        NbtCompoundTag chunkTag = chunk.getChunkTag();
        ChunkNbtInterpreter nbtInterpreter = ChunkControllerFactory.getChunkNbtInterpreter(chunkTag);
        nbtInterpreter.writeBiomes(chunkTag, chunk.getBiomes());
        nbtInterpreter.writeBlockEntities(chunkTag, chunk.getBlockEntities());
        nbtInterpreter.writeEntities(chunkTag, chunk.getEntities());
        nbtInterpreter.writeChunkSections(chunkTag, chunk.getChunkSections());
        return chunkTag;
    }
    
    
    
}
