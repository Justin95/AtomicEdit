
package atomicedit.backend.chunk;

import atomicedit.backend.nbt.MalformedNbtTagException;

/**
 *
 * @author Justin Bonner
 */
public class ChunkController1_18 extends BaseChunkControllerV2 {
    
    public ChunkController1_18(Chunk chunk) throws MalformedNbtTagException{
        super(chunk);
    }

    @Override
    protected boolean useCubicBiomes() {
        return true;
    }

    
}
