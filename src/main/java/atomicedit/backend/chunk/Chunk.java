
package atomicedit.backend.chunk;

import atomicedit.backend.nbt.NbtCompoundTag;

/**
 * 
 * Reference: https://minecraft.gamepedia.com/Chunk_format
 * @author Justin Bonner
 */
public class Chunk {
    
    public static final int X_LENGTH = 16;
    public static final int Z_LENGTH = 16;
    public static final int NUM_CHUNK_SECTIONS_IN_CHUNK = 16;
    public static final int NUM_COLUMNS_IN_CHUNK = X_LENGTH * Z_LENGTH;
    public static final int MAX_LEGAL_BLOCK_Y = NUM_CHUNK_SECTIONS_IN_CHUNK * ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION - 1;
    private NbtCompoundTag chunkTag;
    private boolean needsSave;
    private boolean needsLightingCalc;
    
    
    public Chunk(NbtCompoundTag chunkTag){
        this.chunkTag = chunkTag;
        this.needsLightingCalc = false;
        this.needsSave = false;
    }
    
    NbtCompoundTag getChunkTag(){
        return this.chunkTag;
    }
    
    void setChunkTag(NbtCompoundTag chunkTag){
        this.chunkTag = chunkTag;
    }
    
    public boolean needsSaving(){
        return this.needsSave;
    }
    
    public void setNeedsSaving(boolean needsSave){
        this.needsSave = needsSave;
    }
    
    public boolean needsLightingCalc(){
        return this.needsLightingCalc;
    }
    
    public void setNeedsLightingCalc(boolean needsLightingCalc){
        this.needsLightingCalc = needsLightingCalc;
    }
    
}
