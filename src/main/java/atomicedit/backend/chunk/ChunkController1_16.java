
package atomicedit.backend.chunk;


/**
 * Changes since ChunkController_1_14:
 * Block ids are packed differently.
 * @author Justin Bonner
 */
public class ChunkController1_16 extends BaseChunkInterpreterV1 {
    
    @Override
    protected long[] packBlockIds(int[] localBlockIds, int indexSize) {
        return VersionBehaviors.packBlockIds_1_16(localBlockIds, indexSize);
    }
    
    @Override
    protected int readBlockIdFromPackedIds(int elementSize, int offset, long[] source) {
        return VersionBehaviors.readBlockIdFromPackedIds_1_16(elementSize, offset, source);
    }
    
    @Override
    public int chunkHeightInSections() {
        return 16;
    }
    
    @Override
    public boolean usesCubicBiomes() {
        return true;
    }
    
}
