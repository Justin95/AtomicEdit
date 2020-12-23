
package atomicedit.backend.blockentity;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtCompoundTag;
import atomicedit.backend.nbt.NbtIntTag;

/**
 *
 * @author Justin Bonner
 */
public class BlockEntity {
    
    private final NbtCompoundTag blockEntityNbt;
    
    public BlockEntity(NbtCompoundTag tag){
        this.blockEntityNbt = tag;
    }
    
    public BlockCoord getBlockCoord() throws MalformedNbtTagException{
        return new BlockCoord(blockEntityNbt.getIntTag("x").getPayload(), blockEntityNbt.getIntTag("y").getPayload(), blockEntityNbt.getIntTag("z").getPayload());
    }
    
    public void setBlockCoord(BlockCoord coord) {
        setBlockCoord(coord.x, coord.y, coord.z);
    }
    
    public void setBlockCoord(int x, int y, int z) {
        this.getNbtData().putTag(new NbtIntTag("x", x));
        this.getNbtData().putTag(new NbtIntTag("y", y));
        this.getNbtData().putTag(new NbtIntTag("z", z));
    }
    
    public String getBlockEntityId() throws MalformedNbtTagException{
        return blockEntityNbt.getStringTag("id").getPayload();
    }
    
    public NbtCompoundTag getNbtData(){
        return this.blockEntityNbt;
    }
    
    public BlockEntity copy() {
        return new BlockEntity(blockEntityNbt.copy());
    }
    
    @Override
    public String toString() {
        return this.blockEntityNbt.toString(2);
    }
    
}
