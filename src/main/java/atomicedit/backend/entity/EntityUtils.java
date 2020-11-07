
package atomicedit.backend.entity;

import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtDoubleTag;
import atomicedit.backend.nbt.NbtListTag;
import atomicedit.backend.nbt.NbtTag;
import atomicedit.volumes.WorldVolume;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public class EntityUtils {
    
    public static Collection<Entity> translateEntityCoordsToVolume(Collection<Entity> toUpdate, WorldVolume volume) throws MalformedNbtTagException {
        if (toUpdate == null) {
            return null;
        }
        List<Entity> updated = new ArrayList<>();
        for (Entity entity : toUpdate) {
            entity = entity.copy();
            EntityCoord coord = entity.getCoord();
            double x = coord.x - volume.getSmallestPoint().x;
            double y = coord.y - volume.getSmallestPoint().y;
            double z = coord.z - volume.getSmallestPoint().z;
            List<NbtTag> coordList = new ArrayList<>();
            coordList.add(new NbtDoubleTag("", x));
            coordList.add(new NbtDoubleTag("", y));
            coordList.add(new NbtDoubleTag("", z));
            NbtListTag pos = new NbtListTag("Pos", coordList);
            entity.getNbtData().putTag(pos);
            updated.add(entity);
        }
        return updated;
    }

    public static Collection<Entity> translateEntityCoordsToWorld(Collection<Entity> toUpdate, WorldVolume volume) throws MalformedNbtTagException {
        if (toUpdate == null) {
            return null;
        }
        List<Entity> updated = new ArrayList<>();
        for (Entity entity : toUpdate) {
            entity = entity.copy();
            EntityCoord coord = entity.getCoord();
            double x = coord.x + volume.getSmallestPoint().x;
            double y = coord.y + volume.getSmallestPoint().y;
            double z = coord.z + volume.getSmallestPoint().z;
            List<NbtTag> coordList = new ArrayList<>();
            coordList.add(new NbtDoubleTag("", x));
            coordList.add(new NbtDoubleTag("", y));
            coordList.add(new NbtDoubleTag("", z));
            NbtListTag pos = new NbtListTag("Pos", coordList);
            entity.getNbtData().putTag(pos);
            updated.add(entity);
        }
        return updated;
    }
    
}
