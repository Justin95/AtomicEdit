
package atomicedit.backend.entity;

import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtCompoundTag;
import atomicedit.backend.nbt.NbtDoubleTag;
import atomicedit.backend.nbt.NbtTag;
import atomicedit.backend.nbt.NbtTypes;
import java.util.List;

/**
 * Reference: https://minecraft.gamepedia.com/Chunk_format#Entity_format
 * @author Justin Bonner
 */
public class Entity {
    
    private NbtCompoundTag entityNbt;
    private EntityCoord coord;
    
    public Entity(NbtTag entityNbt) throws MalformedNbtTagException{
        this.entityNbt = NbtTypes.getAsCompoundTag(entityNbt);
        List<NbtDoubleTag> pos = this.entityNbt.getListTag("Pos").getDoubleTags();
        this.coord = new EntityCoord(pos.get(0).getPayload(), pos.get(1).getPayload(), pos.get(2).getPayload());
    }
    
    
    public EntityCoord getCoord(){
        return this.coord;
    }
    
    public NbtCompoundTag getNbtData(){
        return this.entityNbt;
    }
    
    
}
