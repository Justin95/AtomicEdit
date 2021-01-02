
package atomicedit.backend.schematic;

import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtCompoundTag;

/**
 *
 * @author Justin Bonner
 */
public interface SchematicFileFormat {
    
    Schematic readSchematic(NbtCompoundTag rawSchematicTag) throws MalformedNbtTagException;
    
    NbtCompoundTag writeSchematic(Schematic schematic);
    
}
