
package atomicedit.backend.chunk;

import atomicedit.backend.ChunkSectionCoord;

/**
 *
 * @author Justin Bonner
 */
public class ChunkSection {
    
    public static final int SIDE_LENGTH = 16;
    public static final int NUM_BLOCKS_IN_CHUNK_SECTION = SIDE_LENGTH * SIDE_LENGTH * SIDE_LENGTH;
    
    
    public final ChunkSectionCoord coord;
    private boolean dirty;
    private boolean needsRelight;
    private short[] blocks; //block ids 0 to SHORT_MAX
    private byte[] blockLight;
    private byte[] skyLight;
    
    public ChunkSection(ChunkSectionCoord coord, short[] blocks, byte[] blockLight, byte[] skyLight){
        if(blocks.length != NUM_BLOCKS_IN_CHUNK_SECTION) throw new IllegalArgumentException("Tried to make a chunk section with wrong number of blocks");
        if(blockLight.length != NUM_BLOCKS_IN_CHUNK_SECTION / 2) throw new IllegalArgumentException("Tried to make a chunk section with wrong length of block light data");
        if(skyLight.length != NUM_BLOCKS_IN_CHUNK_SECTION / 2) throw new IllegalArgumentException("Tried to make a chunk section with wrong length of sky light data");
        this.blocks = blocks;
        this.coord = coord;
        this.blockLight = blockLight;
        this.skyLight = skyLight;
        this.dirty = false;
        this.needsRelight = false;
    }
    
    public boolean isDirty(){
        return this.dirty;
    }
    
    public void setDirty(boolean dirty){
        this.dirty = dirty;
    }
    
    public boolean getNeedsRelight(){
        return this.needsRelight;
    }
    
    public void setNeedsRelight(boolean needsRelight){
        this.needsRelight = needsRelight;
    }
    
    public short[] getBlockIds(){
        return this.blocks;
    }
    
    public byte[] getBlockLightValues(){
        return this.blockLight;
    }
    
    public byte[] getSkyLightValues(){
        return this.skyLight;
    }
    
    public void setBlocks(short[] blocks) {
        this.blocks = blocks;
    }
    
}
