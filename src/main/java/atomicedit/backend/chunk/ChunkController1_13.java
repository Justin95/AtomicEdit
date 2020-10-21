
package atomicedit.backend.chunk;

import atomicedit.backend.nbt.MalformedNbtTagException;


/**
 *
 * @author Justin Bonner
 */
public class ChunkController1_13 extends BaseChunkControllerV1 {
    
    
    public ChunkController1_13(Chunk chunk) throws MalformedNbtTagException {
        super(chunk);
    }
    
    @Override
    protected boolean useCubicBiomes() {
        return true;
    }

    @Override
    public int chunkHeightInSections() {
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
