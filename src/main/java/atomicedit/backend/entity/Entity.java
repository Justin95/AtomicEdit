
package atomicedit.backend.entity;

import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtCompoundTag;
import atomicedit.backend.nbt.NbtDoubleTag;
import atomicedit.backend.nbt.NbtListTag;
import atomicedit.backend.nbt.NbtTag;
import atomicedit.backend.nbt.NbtTypes;
import java.util.ArrayList;
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
    
    public void setCoord(double x, double y, double z) {
        List<NbtTag> coordList = new ArrayList<>();
        coordList.add(new NbtDoubleTag("", x));
        coordList.add(new NbtDoubleTag("", y));
        coordList.add(new NbtDoubleTag("", z));
        NbtListTag pos = new NbtListTag("Pos", coordList, NbtTypes.TAG_DOUBLE);
        this.getNbtData().putTag(pos);
    }
    
    public NbtCompoundTag getNbtData(){
        return this.entityNbt;
    }
    
    public Entity copy() {
        return new Entity(entityNbt.copy());
    }
    
}
