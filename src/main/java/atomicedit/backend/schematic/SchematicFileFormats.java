
package atomicedit.backend.schematic;

import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtCompoundTag;

/**
 *
 * @author Justin Bonner
 */
public enum SchematicFileFormats {
    AE_SCHEMATIC_FORMAT(
        AeSchematicFormatV1::getInstance
    ),
    LEGACY_SCHEMATIC_FORMAT(
        LegacySchematicFormat::getInstance
    ),
    ;
    
    private final FormatSupplier formatSupplier;
    
    SchematicFileFormats(FormatSupplier formatSupplier) {
        this.formatSupplier = formatSupplier;
    }
    
    private static interface FormatSupplier {
        SchematicFileFormat getFormat();
    }
    
    private SchematicFileFormat getFormat() {
        return this.formatSupplier.getFormat();
    }
    
    /**
     * Determine the file format of the given schematic tag.
     * @param schematicTag
     * @return 
     * @throws MalformedNbtTagException
     */
    public static SchematicFileFormat determineFileFormat(NbtCompoundTag schematicTag) throws MalformedNbtTagException {
        if (schematicTag.contains("version")) {
            String version = schematicTag.getStringTag("version").getPayload();
            if ("v2.0".equals(version)) {
                return AE_SCHEMATIC_FORMAT.getFormat();
            } else {
                throw new MalformedNbtTagException("Unrecognized Schematic Version: " + version);
            }
        } else {
            return LEGACY_SCHEMATIC_FORMAT.getFormat();
        }
    }
    
}
