
package atomicedit.backend.entity;

import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtCompoundTag;
import atomicedit.backend.nbt.NbtDoubleTag;
import java.util.List;

/**
 * Reference: https://minecraft.gamepedia.com/Chunk_format#Entity_format
 * @author Justin Bonner
 */
public class Entity {
    
    private final NbtCompoundTag entityNbt;
    
    public Entity(NbtCompoundTag entityNbt) {
        this.entityNbt = entityNbt;
    }
    
    
    public EntityCoord getCoord() throws MalformedNbtTagException {
        List<NbtDoubleTag> pos = this.entityNbt.getListTag("Pos").getDoubleTags();
        return new EntityCoord(pos.get(0).getPayload(), pos.get(1).getPayload(), pos.get(2).getPayload());
    }
    
    public NbtCompoundTag getNbtData(){
        return this.entityNbt;
    }
    
    public Entity copy() {
        return new Entity(entityNbt.copy());
    }
    
}
