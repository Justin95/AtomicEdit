
package atomicedit.backend.chunk;

import atomicedit.backend.entity.Entity;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtCompoundTag;
import java.util.List;

/**
 * The methods that must be implemented to parse a chunk format.
 * @author Justin Bonner
 */
interface ChunkNbtInterpreter {
    
    ChunkCoord getChunkCoord(NbtCompoundTag chunkTag) throws MalformedNbtTagException;
    
    boolean usesCubicBiomes();
    
    int[] getBiomes(NbtCompoundTag chunkTag) throws MalformedNbtTagException;
    
    ChunkSection[] getChunkSections(NbtCompoundTag chunkTag) throws MalformedNbtTagException;
    
    List<Entity> getEntities(NbtCompoundTag chunkTag) throws MalformedNbtTagException; //read only list
    
    List<BlockEntity> getBlockEntities(NbtCompoundTag chunkTag) throws MalformedNbtTagException; //read only list
    
    //write methods
    
    void writeChunkSections(NbtCompoundTag chunkTag, ChunkSection[] sections) throws MalformedNbtTagException;
    
    void writeBiomes(NbtCompoundTag chunkTag, int[] biomes) throws MalformedNbtTagException;
    
    void writeEntities(NbtCompoundTag chunkTag, List<Entity> entities) throws MalformedNbtTagException;
    
    void writeBlockEntities(NbtCompoundTag chunkTag, List<BlockEntity> blockEntities) throws MalformedNbtTagException;
    
}
