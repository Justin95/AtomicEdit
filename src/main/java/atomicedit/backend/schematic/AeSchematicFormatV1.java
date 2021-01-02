
package atomicedit.backend.schematic;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.BlockState;
import atomicedit.backend.BlockStateProperty;
import atomicedit.backend.GlobalBlockStateMap;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.entity.Entity;
import atomicedit.backend.entity.EntityCoord;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtByteArrayTag;
import atomicedit.backend.nbt.NbtByteTag;
import atomicedit.backend.nbt.NbtCompoundTag;
import atomicedit.backend.nbt.NbtIntArrayTag;
import atomicedit.backend.nbt.NbtIntTag;
import atomicedit.backend.nbt.NbtListTag;
import atomicedit.backend.nbt.NbtLongArrayTag;
import atomicedit.backend.nbt.NbtShortTag;
import atomicedit.backend.nbt.NbtStringTag;
import atomicedit.backend.nbt.NbtTag;
import atomicedit.backend.nbt.NbtTypes;
import atomicedit.backend.utils.BitArray;
import atomicedit.logging.Logger;
import atomicedit.volumes.Box;
import atomicedit.volumes.Volume;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Justin Bonner
 */
public class AeSchematicFormatV1 implements SchematicFileFormat {
    
    /*
    Atomic Edit Schematic Format Version 1 is an NBT Compound Tag with the following fields
    
    [Nbt_String_Tag] "Version": "v2.0"  //orignal legacy schematics would be version 1.0 even though they dont have a version tag
    [Nbt_String_Tag] "Source": "AtomicEdit" //other programs put their own name here
    [Nbt_Short_Tag] "Width":  X axis length
    [Nbt_Short_Tag] "Height": Y axis length
    [Nbt_Short_Tag] "Length": Z axis length
    Optional: [Nbt_Long_Array_Tag] "Included_Set": A bit array describing what blocks are included in the schematic.
    The bit order in the longs is lowest to highest. The bit at index n refers to the block at index n. If this tag is absent then all blocks are included.
    A 1 bit means the block at the matching index in "Blocks" is a part of this schematic, a 0 bit means to ignore that block.
    [Nbt_List_Tag] "Palette": 
        [Nbt_Compound_Tag] :
            [Nbt_String_Tag] "Name": The block string id. i.e. "minecraft:stone"
            Optional: [Nbt_Compound_Tag] "Properties": 
                [Nbt_String_Tag] The Name of the property : The value of the property (All minecraft block properties are strings)
                If minecraft ever supports non string block properties then this tag could be of a non string type.
    [Nbt_Int_Array or Nbt_Byte_Array] "Blocks": The values in this array match an index in "Palette".
    Values are not compressed like in the minecraft chunk format.
    The type of "Blocks" may be a byte array if the number of different block states is less than 128 otherwise it is an int array.
    The length of the "Blocks" array will equal "Width" * "Height" * "Length".
    The "Blocks" values are stored in YZX format, the same as a minecraft chunk.
    Optional: [Nbt_List_Tag] "Entities" The entities in this schematic. All entities positions must be schematic relative.
        [Nbt_Compound_Tag] An Entity
    Optional: [Nbt_List_Tag] "BlockEntities" The block entities in this schematic. All positions must be schematic relative.
        [Nbt_Compound_Tag] A Block Entity
    */
    
    private static final AeSchematicFormatV1 INSTANCE = new AeSchematicFormatV1();
    
    private AeSchematicFormatV1() {
        
    }
    
    public static AeSchematicFormatV1 getInstance() {
        return INSTANCE;
    }
    
    @Override
    public Schematic readSchematic(NbtCompoundTag rawSchematicTag) throws MalformedNbtTagException {
        //read width
        if (!rawSchematicTag.contains("Width")) {
            throw new MalformedNbtTagException("Schematic does not contain 'Width'.");
        }
        if (rawSchematicTag.getTag("Width").getType() != NbtTypes.TAG_SHORT) {
            throw new MalformedNbtTagException("'Width' tag in schematic is not of type short.");
        }
        final int xLen = rawSchematicTag.getShortTag("Width").getPayload();
        
        //read height
        if (!rawSchematicTag.contains("Height")) {
            throw new MalformedNbtTagException("Schematic does not contain 'Height'.");
        }
        if (rawSchematicTag.getTag("Height").getType() != NbtTypes.TAG_SHORT) {
            throw new MalformedNbtTagException("'Height' tag in schematic is not of type short.");
        }
        final int yLen = rawSchematicTag.getShortTag("Height").getPayload();
        
        //read length
        if (!rawSchematicTag.contains("Length")) {
            throw new MalformedNbtTagException("Schematic does not contain 'Length'.");
        }
        if (rawSchematicTag.getTag("Length").getType() != NbtTypes.TAG_SHORT) {
            throw new MalformedNbtTagException("'Length' tag in schematic is not of type short.");
        }
        final int zLen = rawSchematicTag.getShortTag("Length").getPayload();
        
        //read included set
        long[] rawIncludedSet = null;
        if (rawSchematicTag.contains("Included_Set")) {
            if (rawSchematicTag.getTag("Included_Set").getType() != NbtTypes.TAG_LONG_ARRAY) {
                throw new MalformedNbtTagException("'Included_Set' tag in schematic is not of type Long Array.");
            }
            rawIncludedSet = rawSchematicTag.getLongArrayTag("Included_Set").getPayload();
        }
        
        //read palette
        if (!rawSchematicTag.contains("Palette")) {
            throw new MalformedNbtTagException("Schematic does not contain 'Palette'.");
        }
        if (rawSchematicTag.getTag("Palette").getType() != NbtTypes.TAG_LIST) {
            throw new MalformedNbtTagException("'Palette' tag in schematic is not of type list.");
        }
        NbtListTag paletteTag = rawSchematicTag.getListTag("Palette");
        short[] schemIdToInternalId = new short[paletteTag.getPayloadSize()];
        for (int i = 0; i < paletteTag.getPayloadSize(); i++) {
            NbtTag tag = paletteTag.getPayload().get(i);
            if (tag.getType() != NbtTypes.TAG_COMPOUND) {
                throw new MalformedNbtTagException("Block State Tag is not a Compound Tag");
            }
            NbtCompoundTag blockStateTag = (NbtCompoundTag) tag;
            String blockName = blockStateTag.getStringTag("Name").getPayload();
            BlockStateProperty[] properties = null;
            if(blockStateTag.contains("Properties")){
                NbtCompoundTag propertiesTag = blockStateTag.getCompoundTag("Properties");
                properties = new BlockStateProperty[propertiesTag.getPayloadSize()];
                for(int j = 0; j < properties.length; j++){
                    NbtTag property = propertiesTag.getPayload().get(j);
                    switch(property.getType()){
                        case TAG_INT:
                            properties[j] = BlockStateProperty.getInstance(property.getName(), NbtTypes.getInt(property));
                            break;
                        case TAG_BYTE: //assume byte used for boolean
                            properties[j] = BlockStateProperty.getInstance(property.getName(), NbtTypes.getByte(property) != 0); //assume non zero value means true boolean
                            break;
                        case TAG_STRING:
                            properties[j] = BlockStateProperty.getInstance(property.getName(), NbtTypes.getString(property));
                            break;
                        default:
                            Logger.warning("Unexpected NBT type in block properties: " + property.getName() + " " + property.getType());
                    }
                }
            }
            schemIdToInternalId[i] = GlobalBlockStateMap.getBlockId(BlockState.getBlockState(blockName, properties));
        }
        
        //read blocks
        short[] blocks = new short[xLen * yLen * zLen];
        switch (rawSchematicTag.getTag("Blocks").getType()) {
            case TAG_BYTE_ARRAY:
                byte[] rawBlocksB = rawSchematicTag.getByteArrayTag("Blocks").getPayload();
                if (rawBlocksB.length != blocks.length) {
                    throw new MalformedNbtTagException("'Blocks' tag of wrong length: " + rawBlocksB.length + " should be: " + blocks.length);
                }
                for (int i = 0; i < rawBlocksB.length; i++) {
                    blocks[i] = rawBlocksB[i];
                }
                break;
            case TAG_INT_ARRAY:
                int[] rawBlocksI = rawSchematicTag.getIntArrayTag("Blocks").getPayload();
                if (rawBlocksI.length != blocks.length) {
                    throw new MalformedNbtTagException("'Blocks' tag of wrong length: " + rawBlocksI.length + " should be: " + blocks.length);
                }
                for (int i = 0; i < rawBlocksI.length; i++) {
                    if (rawBlocksI[i] > Short.MAX_VALUE) {
                        throw new MalformedNbtTagException("Value in 'Blocks' is invalid: " + rawBlocksI[i]);
                    }
                    blocks[i] = (short)rawBlocksI[i];
                }
                break;
            default:
                throw new MalformedNbtTagException("'Blocks' tag is not a byte array or an int array.");
        }
        
        //read entities
        List<Entity> entities = new ArrayList<>();
        if (rawSchematicTag.contains("Entities")) {
            if (rawSchematicTag.getTag("Entities").getType() != NbtTypes.TAG_LIST) {
                throw new MalformedNbtTagException("'Entities' tag is not a list.");
            }
            NbtListTag entitiesTag = rawSchematicTag.getListTag("Entities");
            for (NbtTag tag : entitiesTag.getPayload()) {
                if (tag.getType() != NbtTypes.TAG_COMPOUND) {
                    throw new MalformedNbtTagException("Entity tag in schematic is not a compound tag.");
                }
                Entity entity = new Entity((NbtCompoundTag)tag);
                EntityCoord coord = entity.getCoord();
                if (coord.x < 0 || coord.y < 0 || coord.z < 0 || coord.x > xLen || coord.y > yLen || coord.z > zLen) {
                    Logger.warning("Entity in schematic is outside bounds of schematic " + coord + ". This may cause issues placing the entity.");
                }
                entities.add(entity);
            }
        }
        
        //read block entities
        List<BlockEntity> blockEntities = new ArrayList<>();
        if (rawSchematicTag.contains("BlockEntities")) {
            if (rawSchematicTag.getTag("BlockEntities").getType() != NbtTypes.TAG_LIST) {
                throw new MalformedNbtTagException("'BlockEntities' tag is not a list.");
            }
            NbtListTag entitiesTag = rawSchematicTag.getListTag("BlockEntities");
            for (NbtTag tag : entitiesTag.getPayload()) {
                if (tag.getType() != NbtTypes.TAG_COMPOUND) {
                    throw new MalformedNbtTagException("BlockEntity tag in schematic is not a compound tag.");
                }
                BlockEntity blockEntity = new BlockEntity((NbtCompoundTag)tag);
                BlockCoord coord = blockEntity.getBlockCoord();
                if (coord.x < 0 || coord.y < 0 || coord.z < 0 || coord.x > xLen || coord.y > yLen || coord.z > zLen) {
                    Logger.warning("BlockEntity in schematic is outside bounds of schematic " + coord + ". This may cause issues placing the block entity.");
                }
                blockEntities.add(blockEntity);
            }
        }
        
        //create schematic
        BitArray includedSet;
        if (rawIncludedSet != null) {
            int size = blocks.length;
            if (rawIncludedSet.length < (size % 64 == 0 ? size / 64 : (size / 64) + 1)) {
                throw new MalformedNbtTagException("'Included_Set' tag is too small for number of blocks.");
            }
            includedSet = new BitArray(blocks.length, rawIncludedSet);
        } else {
            includedSet = new BitArray(blocks.length);
        }
        Box enclosingBox = new Box(xLen, yLen, zLen);
        Volume volume = new Volume(enclosingBox, includedSet);
        return new Schematic(volume, blocks, entities, blockEntities);
    }
    
    @Override
    public NbtCompoundTag writeSchematic(Schematic schematic) {
        final int xLen = schematic.volume.getEnclosingBox().getXLength();
        final int yLen = schematic.volume.getEnclosingBox().getYLength();
        final int zLen = schematic.volume.getEnclosingBox().getZLength();
        if (xLen > Short.MAX_VALUE || yLen > Short.MAX_VALUE || zLen > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Schematic is too large to write.");
        }
        BitArray includedSet = schematic.volume.getIncludedSet();
        boolean allIncluded = true;
        for (int i = 0; i < includedSet.size(); i++) {
            if (!includedSet.get(i)) {
                allIncluded = false;
                break;
            }
        }
        final short[] blocks = schematic.getBlocks();
        Set<Short> uniqueBlockIds = new HashSet<>();
        for (int i = 0; i < blocks.length; i++) {
            if (!uniqueBlockIds.contains(blocks[i])) {
                uniqueBlockIds.add(blocks[i]);
            }
        }
        short[] internalIdToSchematicId = new short[GlobalBlockStateMap.getNumBlockStates()];
        List<BlockState> palette = new ArrayList<>(uniqueBlockIds.size() + 1);
        palette.add(BlockState.AIR); //make AIR the 0 id in every schematic
        internalIdToSchematicId[0] = (short)0;
        for (Short blockId : uniqueBlockIds) {
            if (blockId == 0) {
                continue; //already included air
            }
            //the index is the internal id, the value at that index is the palette id
            internalIdToSchematicId[blockId] = (short)palette.size();
            palette.add(GlobalBlockStateMap.getBlockType(blockId));
        }
        
        final short[] schemBlocks = new short[blocks.length];
        for (int i = 0; i < blocks.length; i++) {
            schemBlocks[i] = internalIdToSchematicId[blocks[i]];
        }
        
        Collection<Entity> entities = schematic.getEntities();
        
        Collection<BlockEntity> blockEntities = schematic.getBlockEntities();
        
        //write nbt
        NbtCompoundTag schematicTag = new NbtCompoundTag("");
        //meta data
        schematicTag.putTag(new NbtStringTag("Version", "v2.0"));
        schematicTag.putTag(new NbtStringTag("Source", "AtomicEdit"));
        //volume
        schematicTag.putTag(new NbtShortTag("Width",  (short)xLen));
        schematicTag.putTag(new NbtShortTag("Height", (short)yLen));
        schematicTag.putTag(new NbtShortTag("Length", (short)zLen));
        if (!allIncluded) {
            schematicTag.putTag(new NbtLongArrayTag("Included_Set", includedSet.getBackingValues()));
        }
        //palette
        List<NbtTag> blockPalletTags = new ArrayList<>();
        for(BlockState blockState : palette){
            ArrayList<NbtTag> propertyTags = new ArrayList<>();
            if(blockState.blockStateProperties != null){
                for(BlockStateProperty property : blockState.blockStateProperties){
                    //currently blockstates only have string propreties, but if that changes we'll be ready
                    switch(property.valueType){
                        case INTEGER:
                            propertyTags.add(new NbtIntTag(property.NAME, (Integer)property.VALUE));
                            break;
                        case BOOLEAN:
                            propertyTags.add(new NbtByteTag(property.NAME, (Boolean)property.VALUE ? (byte)1 : 0));
                            break;
                        case STRING:
                            propertyTags.add(new NbtStringTag(property.NAME, (String)property.VALUE));
                            break;
                        default:
                            Logger.error("Invalid block state property value type: " + blockState);
                            throw new RuntimeException("Bad block state property: " + property);
                    }
                }
            }
            NbtCompoundTag blockStatePropertiesTag = new NbtCompoundTag("Properties", propertyTags);
            NbtStringTag blockStateNameTag = new NbtStringTag("Name", blockState.name);
            NbtCompoundTag blockStateTag = new NbtCompoundTag("", blockStateNameTag, blockStatePropertiesTag);
            blockPalletTags.add(blockStateTag);
        }
        schematicTag.putTag(new NbtListTag("Palette", blockPalletTags, NbtTypes.TAG_COMPOUND));
        //blocks
        if (palette.size() < 128) {
            byte[] byteBlocks = new byte[schemBlocks.length];
            for (int i = 0; i < schemBlocks.length; i++) {
                byteBlocks[i] = (byte)schemBlocks[i];
            }
            schematicTag.putTag(new NbtByteArrayTag("Blocks", byteBlocks));
        } else {
            int[] intBlocks = new int[schemBlocks.length];
            for (int i = 0; i < schemBlocks.length; i++) {
                intBlocks[i] = schemBlocks[i];
            }
            schematicTag.putTag(new NbtIntArrayTag("Blocks", intBlocks));
        }
        //entities
        List<NbtTag> entityTags = new ArrayList<>();
        for (Entity entity : entities) {
            entityTags.add(entity.getNbtData());
        }
        schematicTag.putTag(new NbtListTag("Entities", entityTags, NbtTypes.TAG_COMPOUND));
        //block entities
        List<NbtTag> blockEntityTags = new ArrayList<>();
        for (BlockEntity blockEntity : blockEntities) {
            blockEntityTags.add(blockEntity.getNbtData());
        }
        schematicTag.putTag(new NbtListTag("BlockEntities", blockEntityTags, NbtTypes.TAG_COMPOUND));
        //done
        return schematicTag;
    }
    
    
    
}
