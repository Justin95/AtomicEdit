
package atomicedit.backend.chunk;

import atomicedit.backend.nbt.MalformedNbtTagException;


/**
 * Changes since ChunkController1_14:
 * Block Ids are packed into a long array differently.
 * @author Justin Bonner
 */
public class ChunkController1_16 extends BaseChunkControllerV1 {
    
    
    public ChunkController1_16(Chunk chunk) throws MalformedNbtTagException{
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
    
}
