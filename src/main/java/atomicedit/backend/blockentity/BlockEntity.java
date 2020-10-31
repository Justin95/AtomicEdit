
package atomicedit.backend.blockentity;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtCompoundTag;

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
    
    public String getBlockEntityId() throws MalformedNbtTagException{
        return blockEntityNbt.getStringTag("id").getPayload();
    }
    
    public NbtCompoundTag getNbtData(){
        return this.blockEntityNbt;
    }
    
    @Override
    public String toString() {
        return this.blockEntityNbt.toString(2);
    }
    
}
