
package atomicedit.backend.chunk;

import atomicedit.backend.nbt.MalformedNbtTagException;


/**
 * Changes since ChunkController_1_13:
 * LIGHT_BLOCKING height map removed from chunk format
 * @author Justin Bonner
 */
public class ChunkController1_14 extends BaseChunkControllerV1 {
    
    
    public ChunkController1_14(Chunk chunk) throws MalformedNbtTagException{
        super(chunk);
    }

    @Override
    protected boolean useCubicBiomes() {
        return true;
    }

    @Override
    protected int chunkHeightInSections() {
        return 16;
    }

    @Override
    protected void writeBiomes(int[] biomes) throws MalformedNbtTagException {
        VersionBehaviors.writeBiomes_1_13(chunkNbt, biomes);
    }

    @Override
    protected int[] getBiomes() throws MalformedNbtTagException {
        return VersionBehaviors.parseBiomes_1_13(chunkNbt);
    }

    @Override
    protected long[] packBlockIds(int[] localBlockIds, int indexSize) {
        return VersionBehaviors.packBlockIds_1_13(localBlockIds, indexSize);
    }

    @Override
    protected int readBlockIdFromPackedIds(int elementSize, int offset, long[] source) {
        return VersionBehaviors.readBlockIdFromPackedIds_1_13(elementSize, offset, source);
    }
    
}
