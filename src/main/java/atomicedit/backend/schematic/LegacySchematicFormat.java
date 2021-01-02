
package atomicedit.backend.schematic;

import atomicedit.backend.nbt.NbtCompoundTag;

/**
 * For reference: https://minecraft.gamepedia.com/Schematic_file_format
 * @author Justin Bonner
 */
public class LegacySchematicFormat implements SchematicFileFormat {
    
    private static final LegacySchematicFormat INSTANCE = new LegacySchematicFormat();
    
    private LegacySchematicFormat() {
        
    }
    
    public static LegacySchematicFormat getInstance() {
        return INSTANCE;
    }
    
    @Override
    public Schematic readSchematic(NbtCompoundTag rawSchematicTag) {
        //TODO legacy_block_ids.json needs double checking, silver_shunker_box, grass, dirt, 1.7 stones, all variant blocks
        throw new UnsupportedOperationException("TODO");
    }
    
    @Override
    public NbtCompoundTag writeSchematic(Schematic schematic) {
        throw new UnsupportedOperationException("TODO");
    }
    
    
    
}
