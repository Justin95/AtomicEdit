
package atomicedit.backend.chunk;

import atomicedit.backend.BlockState;
import atomicedit.backend.BlockStateProperty;
import atomicedit.backend.ChunkSectionCoord;
import atomicedit.backend.GlobalBlockStateMap;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.entity.Entity;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtByteArrayTag;
import atomicedit.backend.nbt.NbtByteTag;
import atomicedit.backend.nbt.NbtCompoundTag;
import atomicedit.backend.nbt.NbtIntTag;
import atomicedit.backend.nbt.NbtListTag;
import atomicedit.backend.nbt.NbtLongArrayTag;
import atomicedit.backend.nbt.NbtStringTag;
import atomicedit.backend.nbt.NbtTag;
import atomicedit.backend.nbt.NbtTypes;
import atomicedit.logging.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hold the common chunk NBT parsing code for most chunk interpreters. If there is a major change to
 * the chunk NBT format a new BaseChunkInterpreter can be created along side this one or the interface
 * can be implemented alone.
 * @author Justin Bonner
 */
public abstract class BaseChunkInterpreterV1 implements ChunkNbtInterpreter {
    
    protected NbtCompoundTag getLevel(NbtCompoundTag chunkTag) throws MalformedNbtTagException {
        return chunkTag.getCompoundTag("Level");
    }
    
    @Override
    public ChunkCoord getChunkCoord(NbtCompoundTag chunkTag) throws MalformedNbtTagException {
        return ChunkCoord.getInstance(
            getLevel(chunkTag).getIntTag("xPos").getPayload(),
            getLevel(chunkTag).getIntTag("zPos").getPayload()
        );
    }
    
    @Override
    public int[] getBiomes(NbtCompoundTag chunkTag) throws MalformedNbtTagException {
        return VersionBehaviors.parseBiomes_1_16(chunkTag);
    }
    
    @Override
    public ChunkSection[] getChunkSections(NbtCompoundTag chunkTag) throws MalformedNbtTagException {
        ChunkCoord coord = getChunkCoord(chunkTag);
        ChunkSection[] chunkSections = new ChunkSection[chunkHeightInSections()];
        for(NbtCompoundTag section : getLevel(chunkTag).getListTag("Sections").getCompoundTags()) {
            int subChunkIndex = section.getByteTag("Y").getPayload();
            if (subChunkIndex < 0) {
                continue; //Minecraft 1.16 has a lot of y = -1 chunk sections in the data. These are empty except for the 'Y' tag. Dont know why.
            }
            if (subChunkIndex >= chunkSections.length) {
                throw new MalformedNbtTagException("Chunk Section out of bounds at " + subChunkIndex);
            }
            ChunkSectionCoord sectionCoord = new ChunkSectionCoord(coord.x, subChunkIndex, coord.z);
            chunkSections[subChunkIndex] = readChunkSection(section, sectionCoord);
        }
        for (int i = 0; i < chunkSections.length; i++) {
            if (chunkSections[i] == null) {
                ChunkSectionCoord sectionCoord = new ChunkSectionCoord(coord.x, i, coord.z);
                chunkSections[i] = makeBlankChunkSection(sectionCoord);
            }
        }
        return chunkSections;
    }
    
    protected ChunkSection readChunkSection(NbtCompoundTag sectionTag, ChunkSectionCoord chunkSectionCoord) throws MalformedNbtTagException{
        short[] blocks = new short[ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION];
        if(sectionTag.contains("Palette")) {
            List<NbtCompoundTag> blockStateNbts = sectionTag.getListTag("Palette").getCompoundTags();
            BlockState[] blockTypes = new BlockState[blockStateNbts.size()];
            for(int i = 0; i < blockStateNbts.size(); i++){
                NbtCompoundTag blockStateTag = blockStateNbts.get(i);
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
                blockTypes[i] = BlockState.getBlockState(blockName, properties);
            }
            if(blockTypes.length > ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION + 1){//if length is greater than max blocks in a sub section + 1 (air)
                throw new MalformedNbtTagException("Too many blocks in palette: " + blockTypes.length);
            }
            int indexSize = getIndexSize(blockTypes.length);
            long[] localBlockIds = sectionTag.getLongArrayTag("BlockStates").getPayload();
            for(int i = 0; i < ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION; i++){
                int blockTypeIndex = readBlockIdFromPackedIds(indexSize, i, localBlockIds);
                if(blockTypeIndex >= blockTypes.length){
                    Logger.warning("Invalid block type index (" + blockTypeIndex + ") in chunk "+chunkSectionCoord+" at index ("+i+"), replacing with air");
                    blockTypeIndex = 0;
                }
                BlockState blockType = blockTypes[blockTypeIndex];
                blocks[i] = GlobalBlockStateMap.getBlockId(blockType);
            }
        }
        byte[] blockLight = sectionTag.contains("BlockLight") ? sectionTag.getByteArrayTag("BlockLight").getPayload() : new byte[ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION / 2];
        byte[] skyLight = sectionTag.contains("SkyLight") ? sectionTag.getByteArrayTag("SkyLight").getPayload() : new byte[ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION / 2];
        return new ChunkSection(chunkSectionCoord, blocks, blockLight, skyLight);
    }
    
    @Override
    public List<Entity> getEntities(NbtCompoundTag chunkTag) throws MalformedNbtTagException{
        if(!getLevel(chunkTag).contains("Entities")) {
            return new ArrayList<>();
        }
        List<NbtCompoundTag> entityNbts = getLevel(chunkTag).getListTag("Entities").getCompoundTags();
        ArrayList<Entity> entities = new ArrayList<>();
        for(NbtCompoundTag tag : entityNbts){
            entities.add(new Entity(tag));
        }
        return entities;
    }
    
    @Override
    public List<BlockEntity> getBlockEntities(NbtCompoundTag chunkTag) throws MalformedNbtTagException{
        if(!getLevel(chunkTag).contains("BlockEntities")) {
            return new ArrayList<>();
        }
        List<NbtCompoundTag> blockEntityNbts = getLevel(chunkTag).getListTag("BlockEntities").getCompoundTags();
        ArrayList<BlockEntity> blockEntities = new ArrayList<>();
        for(NbtCompoundTag tag : blockEntityNbts){
            blockEntities.add(new BlockEntity(tag));
        }
        return blockEntities;
    }
    
    @Override
    public void writeBiomes(NbtCompoundTag chunkTag, int[] biomes) throws MalformedNbtTagException {
        VersionBehaviors.writeBiomes_1_16(chunkTag, biomes);
    }
    
    @Override
    public void writeChunkSections(NbtCompoundTag chunkTag, ChunkSection[] chunkSections) throws MalformedNbtTagException {
        for (int i = 0; i < chunkSections.length; i++) {
            writeChunkSection(chunkTag, chunkSections[i], i);
        }
    }
    
    protected void writeChunkSection(NbtCompoundTag chunkTag, ChunkSection chunkSection, int chunkSectionIndex) throws MalformedNbtTagException{
        if(chunkSection == null){
            Logger.warning("Tried to write null chunk section");
            throw new IllegalArgumentException("Cannot write null chunk section.");
        }
        NbtCompoundTag sectionTag = null;
        for(NbtCompoundTag sectionNbt : getLevel(chunkTag).getListTag("Sections").getCompoundTags()){
            if(sectionNbt.getByteTag("Y").getPayload() == chunkSectionIndex){
                sectionTag = sectionNbt;
                break;
            }
        }
        if(sectionTag == null){
            ArrayList<NbtTag> tags = new ArrayList<>();
            tags.add(new NbtByteTag("Y", (byte)chunkSectionIndex));
            tags.add(new NbtByteArrayTag("BlockLight", chunkSection.getBlockLightValues()));
            tags.add(new NbtByteArrayTag("SkyLight", chunkSection.getSkyLightValues()));
            sectionTag = new NbtCompoundTag("", tags);
            getLevel(chunkTag).getListTag("Sections").getCompoundTags().add(sectionTag);
        }
        writeChunkSection(sectionTag, chunkSection);
    }
    
    private void writeChunkSection(NbtCompoundTag sectionTag, ChunkSection chunkSection){
        List<BlockState> blockStates = getContainedBlockStates(chunkSection);
        List<NbtTag> blockPalletTags = new ArrayList<>();
        for(BlockState blockState : blockStates){
            ArrayList<NbtTag> propertyTags = new ArrayList<>();
            if(blockState.blockStateProperties != null){
                for(BlockStateProperty property : blockState.blockStateProperties){
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
        sectionTag.putTag(new NbtListTag("Palette", blockPalletTags));
        
        int[] blockValues = new int[ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION];
        for(int i = 0; i < ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION; i++){
            short globalBlockId = chunkSection.getBlockIds()[i];
            BlockState blockState = GlobalBlockStateMap.getBlockType(globalBlockId);
            blockValues[i] = blockStates.indexOf(blockState);
        }
        int indexSize = getIndexSize(blockStates.size());
        long[] packedBlockValues = packBlockIds(blockValues, indexSize);
        sectionTag.putTag(new NbtLongArrayTag("BlockStates", packedBlockValues));
    }
    
    @Override
    public void writeBlockEntities(NbtCompoundTag chunkTag, List<BlockEntity> blockEntities) throws MalformedNbtTagException {
        List<NbtTag> blockEntityNbtTags = new ArrayList<>();
        for (BlockEntity blockEntity : blockEntities) {
            blockEntityNbtTags.add(blockEntity.getNbtData());
        }
        getLevel(chunkTag).putTag(new NbtListTag("BlockEntities", blockEntityNbtTags));
    }
    
    @Override
    public void writeEntities(NbtCompoundTag chunkTag, List<Entity> entities) throws MalformedNbtTagException {
        List<NbtTag> entityNbtTags = new ArrayList<>();
        for (Entity entity : entities) {
            entityNbtTags.add(entity.getNbtData());
        }
        getLevel(chunkTag).putTag(new NbtListTag("Entities", entityNbtTags));
    }
    
    protected ChunkSection makeBlankChunkSection(ChunkSectionCoord coord){
        short[] blocks = new short[ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION];
        if(GlobalBlockStateMap.getBlockId(BlockState.AIR) != 0){ //this condition SHOULD always be false but just incase
            Arrays.fill(blocks, GlobalBlockStateMap.getBlockId(BlockState.AIR));
        }
        byte[] blockLight = new byte[(ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION + 1) / 2]; // divid 2 rounds down on odd numbers, add one to make it round up
        byte[] skyLight = new byte[(ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION + 1) / 2]; //may cause lighting errors
        return new ChunkSection(coord, blocks, blockLight, skyLight);
    }
    
    protected List<BlockState> getContainedBlockStates(ChunkSection chunkSection){
        List<BlockState> blockStates = new ArrayList<>();
        blockStates.add(BlockState.AIR); //air is always in the block state list
        for(short blockId : chunkSection.getBlockIds()){
            BlockState blockState = GlobalBlockStateMap.getBlockType(blockId);
            if(blockStates.contains(blockState)){
                continue;
            }
            blockStates.add(blockState);
        }
        return blockStates;
    }
    
    /**
     * Determine how many bits are needed per number to store length numbers.
     * @param length
     * @return 
     */
    protected int getIndexSize(int length){ 
        int maxRepresentable = 16;
        int numBits = 4; //min 4 bits
        while(length > maxRepresentable){
            numBits++;
            maxRepresentable *= 2;
        }
        return numBits;
    }
    
    /**
     * Pack the local block ids into a long array according to the chunk format of the implementing class.
     * @param localBlockIds
     * @param indexSize
     * @return 
     */
    protected abstract long[] packBlockIds(int[] localBlockIds, int indexSize);
    
    /**
     * Unpack a block id from a long array according to the chunk format of the implementing class.
     * @param elementSize
     * @param offset
     * @param source
     * @return 
     */
    protected abstract int readBlockIdFromPackedIds(int elementSize, int offset, long[] source);
    
    protected abstract int chunkHeightInSections();
    
}
