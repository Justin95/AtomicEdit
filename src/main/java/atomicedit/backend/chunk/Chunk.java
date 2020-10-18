
package atomicedit.backend.chunk;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.entity.Entity;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtCompoundTag;
import java.util.List;

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
    
    public final ChunkCoord chunkCoord;
    private final boolean cubicBiomes;
    private ChunkSection[] chunkSections;
    private NbtCompoundTag chunkTag;
    private List<Entity> entities;
    private List<BlockEntity> blockEntities;
    private int[] biomes;
    private boolean needsSave;
    private boolean needsLightingCalc;
    private boolean needsRedraw;
    
    
    public Chunk(
        ChunkCoord chunkCoord,
        boolean cubicBiomes,
        ChunkSection[] chunkSections,
        NbtCompoundTag chunkTag,
        List<Entity> entities,
        List<BlockEntity> blockEntities,
        int[] biomes
    ){
        this.chunkCoord = chunkCoord;
        this.chunkSections = chunkSections;
        this.cubicBiomes = cubicBiomes;
        this.chunkTag = chunkTag;
        this.entities = entities;
        this.blockEntities = blockEntities;
        this.biomes = biomes;
        this.needsLightingCalc = false;
        this.needsSave = false;
        this.needsRedraw = false;
    }
    
    public NbtCompoundTag getChunkTag(){
        return this.chunkTag;
    }
    
    void setChunkTag(NbtCompoundTag chunkTag){
        this.chunkTag = chunkTag;
    }
    
    public ChunkCoord getChunkCoord() {
        return chunkCoord;
    }
    
    public int getBiomeAt(BlockCoord coord) throws MalformedNbtTagException {
        throw new UnsupportedOperationException("TODO");
    }
    
    int[] getBiomes() {
        return this.biomes;
    }
    
    ChunkSection[] getChunkSections() {
        return this.chunkSections;
    }
    
    /**
     * Get the blocks for the given chunk section.
     * @param subChunkIndex
     * @return
     * @throws MalformedNbtTagException 
     */
    public short[] getBlocks(int subChunkIndex) throws MalformedNbtTagException {
        return this.chunkSections[subChunkIndex].getBlockIds();
    }
    
    public ChunkSection getChunkSection(int subChunkIndex) throws MalformedNbtTagException {
        return this.chunkSections[subChunkIndex];
    }
    
    public List<Entity> getEntities() throws MalformedNbtTagException { //read only list
        return this.entities;
    }
    
    public List<BlockEntity> getBlockEntities() throws MalformedNbtTagException { //read only list
        return this.blockEntities;
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
    
    public boolean needsRedraw(){
        return this.needsRedraw;
    }
    
    public void setNeedsRedraw(boolean needsRedraw){
        this.needsRedraw = needsRedraw;
    }
    
}
