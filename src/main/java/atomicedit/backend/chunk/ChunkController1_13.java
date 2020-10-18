
package atomicedit.backend.chunk;

import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtCompoundTag;


/**
 *
 * @author Justin Bonner
 */
public class ChunkController1_13 extends BaseChunkInterpreterV1 {
    
    
    @Override
    protected long[] packBlockIds(int[] localBlockIds, int indexSize) {
        return VersionBehaviors.packBlockIds_1_13(localBlockIds, indexSize);
    }
    
    @Override
    protected int readBlockIdFromPackedIds(int elementSize, int offset, long[] source) {
        return VersionBehaviors.readBlockIdFromPackedIds_1_13(elementSize, offset, source);
    }
    
    @Override
    public int chunkHeightInSections() {
        return 16;
    }
    
    @Override
    public boolean usesCubicBiomes() {
        return false;
    }
    
    @Override
    public int[] getBiomes(NbtCompoundTag chunkTag) throws MalformedNbtTagException {
        return VersionBehaviors.parseBiomes_1_13(chunkTag);
    }
    
    @Override
    public void writeBiomes(NbtCompoundTag chunkTag, int[] biomes) throws MalformedNbtTagException {
        VersionBehaviors.writeBiomes_1_13(chunkTag, biomes);
    }
    
}
