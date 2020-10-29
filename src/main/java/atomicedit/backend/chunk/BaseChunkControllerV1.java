
package atomicedit.backend.chunk;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.BlockState;
import atomicedit.backend.BlockStateProperty;
import atomicedit.backend.ChunkSectionCoord;
import atomicedit.backend.GlobalBlockStateMap;
import atomicedit.backend.biomes.BiomeMap;
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
import atomicedit.backend.utils.GeneralUtils;
import atomicedit.logging.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author justin
 */
public abstract class BaseChunkControllerV1 extends ChunkController {
    
    protected ChunkCoord coord;
    protected NbtCompoundTag chunkNbt;
    protected ChunkSection[] chunkSectionCache;
    protected boolean chunkSectionCacheIsDirty;
    
    public BaseChunkControllerV1(Chunk chunk) throws MalformedNbtTagException {
        super(chunk);
        this.chunkNbt = chunk.getChunkTag();
        this.coord = ChunkCoord.getInstance(getLevel().getIntTag("xPos").getPayload(), getLevel().getIntTag("zPos").getPayload());
        this.chunkSectionCache = new ChunkSection[Chunk.NUM_CHUNK_SECTIONS_IN_CHUNK];
        this.chunkSectionCacheIsDirty = false;
    }
    
    private NbtCompoundTag getLevel() throws MalformedNbtTagException {
        return this.chunkNbt.getCompoundTag("Level");
    }
    
    @Override
    public ChunkCoord getChunkCoord() {
        return this.coord;
    }
    
    
    public void setBiomeAt(BlockCoord coord, int biome) throws MalformedNbtTagException{
        NbtByteArrayTag biomes = getLevel().getByteArrayTag("Biomes");
        biomes.getPayload()[GeneralUtils.getIndexZX(coord.getChunkLocalX(), coord.getChunkLocalZ(), Chunk.X_LENGTH)] = (byte) biome;
        declareNbtChanged();
    }
    
    @Override
    public int getBiomeAt(BlockCoord coord) throws MalformedNbtTagException{
        NbtByteArrayTag biomes = getLevel().getByteArrayTag("Biomes");
        return biomes.getPayload()[GeneralUtils.getIndexZX(coord.getChunkLocalX(), coord.getChunkLocalZ(), Chunk.X_LENGTH)];
    }
    
    @Override
    public BiomeMap getBiomeMap() throws MalformedNbtTagException {
        int[] biomes = getBiomes();
        BiomeMap biomeMap;
        
        throw new UnsupportedOperationException("TODO");
    }
    
    @Override
    public void setBiomeMap(BiomeMap biomeMap) {
        throw new UnsupportedOperationException("TODO");
    }
    
    @Override
    public BlockState getBlockAt(BlockCoord coord) throws MalformedNbtTagException{
        if(this.chunkSectionCacheIsDirty || chunkSectionCache[coord.getSubChunkIndex()] == null){
            readChunkSectionIntoCache(coord.getSubChunkIndex());
        }
        short[] blockIds = chunkSectionCache[coord.getSubChunkIndex()].getBlockIds();
        int index = GeneralUtils.getIndexYZX(coord.getChunkLocalX(), coord.getSubChunkLocalY(), coord.getChunkLocalZ(), ChunkSection.SIDE_LENGTH);
        short globalId = blockIds[index];
        return GlobalBlockStateMap.getBlockType(globalId);
    }
    
    @Override
    public void setBlockAt(BlockCoord coord, BlockState block) throws MalformedNbtTagException{
        if(this.chunkSectionCacheIsDirty || chunkSectionCache[coord.getSubChunkIndex()] == null){
            readChunkSectionIntoCache(coord.getSubChunkIndex());
        }
        short[] blockIds = chunkSectionCache[coord.getSubChunkIndex()].getBlockIds();
        int index = GeneralUtils.getIndexYZX(coord.getChunkLocalX(), coord.getSubChunkLocalY(), coord.getChunkLocalZ(), ChunkSection.SIDE_LENGTH);
        blockIds[index] = GlobalBlockStateMap.getBlockId(block);
        declareCacheIsDirty();
        chunkSectionCache[coord.getSubChunkIndex()].setDirty(true);
        declareNbtChanged();
        declareVisiblyChanged();
    }
    
    @Override
    public ChunkSection getChunkSection(int subChunkIndex) throws MalformedNbtTagException{
        if(this.chunkSectionCacheIsDirty || chunkSectionCache[subChunkIndex] == null){
            readChunkSectionIntoCache(subChunkIndex);
        }
        return this.chunkSectionCache[subChunkIndex];
    }
    
    @Override
    public short[] getBlocks(int subChunkIndex) throws MalformedNbtTagException{
        return getChunkSection(subChunkIndex).getBlockIds();
    }
    
    @Override
    public void setBlocks(int subChunkIndex, short[] blocks) throws MalformedNbtTagException{
        if(subChunkIndex >= Chunk.NUM_CHUNK_SECTIONS_IN_CHUNK){
            throw new IllegalArgumentException("Cannot write to sub chunk at index: " + subChunkIndex);
        }
        if(this.chunkSectionCacheIsDirty || chunkSectionCache[subChunkIndex] == null){
            readChunkSectionIntoCache(subChunkIndex);
        }
        if(blocks.length != ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION){
            throw new IllegalArgumentException("Wrong number of blocks tried to occupy chunk sub section");
        }
        if(blocks != chunkSectionCache[subChunkIndex].getBlockIds()){ //if the array we are trying to set not in the same location as the one we already have
            System.arraycopy(blocks, 0, chunkSectionCache[subChunkIndex].getBlockIds(), 0, ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION);
        }
        declareCacheIsDirty();
        chunkSectionCache[subChunkIndex].setDirty(true);
        declareNbtChanged();
        declareVisiblyChanged();
    }
    
    private void readChunkSectionIntoCache(int subChunkIndex) throws MalformedNbtTagException{
        ChunkSectionCoord sectionCoord = new ChunkSectionCoord(coord.x, subChunkIndex, coord.z);
        for(NbtCompoundTag section : getLevel().getListTag("Sections").getCompoundTags()){
            if(section.getByteTag("Y").getPayload() == subChunkIndex){
                this.chunkSectionCache[subChunkIndex] = readChunkSection(section, sectionCoord);
                return;
            }
        }
        this.chunkSectionCache[subChunkIndex] = makeBlankChunkSection(sectionCoord);
    }
    
    private void writeChunkSectionCacheIntoNbt(int subChunkIndex) throws MalformedNbtTagException{
        ChunkSection chunkSection = this.chunkSectionCache[subChunkIndex];
        if(chunkSection == null){
            Logger.warning("Tried to write null chunk section");
            throw new IllegalArgumentException("Cannot write null chunk section.");
        }
        NbtCompoundTag sectionTag = null;
        for(NbtCompoundTag sectionNbt : getLevel().getListTag("Sections").getCompoundTags()){
            if(sectionNbt.getByteTag("Y").getPayload() == subChunkIndex){
                sectionTag = sectionNbt;
                break;
            }
        }
        if(sectionTag == null){
            ArrayList<NbtTag> tags = new ArrayList<>();
            tags.add(new NbtByteTag("Y", (byte)subChunkIndex));
            tags.add(new NbtByteArrayTag("BlockLight", chunkSection.getBlockLightValues()));
            tags.add(new NbtByteArrayTag("SkyLight", chunkSection.getSkyLightValues()));
            sectionTag = new NbtCompoundTag("", tags);
            getLevel().getListTag("Sections").getCompoundTags().add(sectionTag);
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
        sectionTag.putTag(new NbtByteArrayTag("BlockLight", chunkSection.getBlockLightValues()));
        sectionTag.putTag(new NbtByteArrayTag("SkyLight", chunkSection.getSkyLightValues()));
    }
    
    private List<BlockState> getContainedBlockStates(ChunkSection chunkSection){
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
    
    private ChunkSection readChunkSection(NbtCompoundTag sectionTag, ChunkSectionCoord chunkSectionCoord) throws MalformedNbtTagException{
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
    
    
    private int getIndexSize(int length){ 
        int maxRepresentable = 16;
        int numBits = 4; //min 4 bits
        while(length > maxRepresentable){
            numBits++;
            maxRepresentable *= 2;
        }
        return numBits;
    }
    
    private ChunkSection makeBlankChunkSection(ChunkSectionCoord coord){
        short[] blocks = new short[ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION];
        if(GlobalBlockStateMap.getBlockId(BlockState.AIR) != 0){ //this condition SHOULD always be false but just incase
            Arrays.fill(blocks, GlobalBlockStateMap.getBlockId(BlockState.AIR));
        }
        byte[] blockLight = new byte[(ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION + 1) / 2]; // divid 2 rounds down on odd numbers, add one to make it round up
        byte[] skyLight = new byte[(ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION + 1) / 2]; //may cause lighting errors
        return new ChunkSection(coord, blocks, blockLight, skyLight);
    }
    
    @Override
    public List<Entity> getEntities() throws MalformedNbtTagException{
        if(!getLevel().contains("Entities")){
            return new ArrayList<>();
        }
        List<NbtCompoundTag> entityNbts = getLevel().getListTag("Entities").getCompoundTags();
        ArrayList<Entity> entities = new ArrayList<>();
        for(NbtCompoundTag tag : entityNbts){
            entities.add(new Entity(tag));
        }
        return entities;
    }
    
    @Override
    public void addEntity(Entity entity) throws MalformedNbtTagException{
        List<NbtCompoundTag> entityNbts = getLevel().getListTag("Entities").getCompoundTags();
        entityNbts.add(entity.getNbtData());
        declareNbtChanged();
    }
    
    @Override
    public void removeEntity(Entity entity) throws MalformedNbtTagException{
        List<NbtCompoundTag> entityNbts = getLevel().getListTag("Entities").getCompoundTags();
        entityNbts.remove(entity.getNbtData());
        declareNbtChanged();
    }
    
    @Override
    public List<BlockEntity> getBlockEntities() throws MalformedNbtTagException{
        if(!getLevel().contains("BlockEntities")){
            return new ArrayList<>();
        }
        List<NbtCompoundTag> blockEntityNbts = getLevel().getListTag("BlockEntities").getCompoundTags();
        ArrayList<BlockEntity> blockEntities = new ArrayList<>();
        for(NbtCompoundTag tag : blockEntityNbts){
            blockEntities.add(new BlockEntity(tag));
        }
        return blockEntities;
    }
    
    @Override
    public void addBlockEntity(BlockEntity blockEntity) throws MalformedNbtTagException{
        List<NbtCompoundTag> blockEntityNbts = getLevel().getListTag("BlockEntities").getCompoundTags();
        blockEntityNbts.add(blockEntity.getNbtData());
        declareNbtChanged();
    }
    
    @Override
    public void removeBlockEntity(BlockEntity blockEntity) throws MalformedNbtTagException{
        List<NbtCompoundTag> blockEntityNbts = getLevel().getListTag("BlockEntities").getCompoundTags();
        blockEntityNbts.remove(blockEntity.getNbtData());
        declareNbtChanged();
    }
    
    @Override
    public NbtTag getChunkAsNbtTag() {
        flushCacheToChunkNbt();
        return this.chunkNbt;
    }
    
    @Override
    public void setChunkNbtTag(NbtTag tag) throws MalformedNbtTagException{
        NbtCompoundTag chunkTag = NbtTypes.getAsCompoundTag(tag);
        this.chunkSectionCacheIsDirty = true; //it might have been changed, could go through and check if it actually was
        this.chunk.setChunkTag(chunkTag);
        this.chunkNbt = chunkTag;
        declareNbtChanged();
    }
    
    private void declareCacheIsDirty(){
        this.chunkSectionCacheIsDirty = true;
    }
    
    @Override
    public void flushCacheToChunkNbt(){
        if(!chunkSectionCacheIsDirty){
            return;
        }
        for(int i = 0; i < this.chunkSectionCache.length; i++){
            if(chunkSectionCache[i] == null){
                continue;
            }
            if(!chunkSectionCache[i].isDirty()){
                continue;
            }
            try{
                writeChunkSectionCacheIntoNbt(i);
            }catch(MalformedNbtTagException e){
                Logger.error("Unable to write chunk section into NBT.", e);
            }
            chunkSectionCache[i].setDirty(false);
        }
        chunkSectionCacheIsDirty = false;
    }
    
    @Override
    public void declareChunkSectionCacheChanged() {
        this.chunkSectionCacheIsDirty = true;
        for (ChunkSection section : this.chunkSectionCache) {
            section.setDirty(true);
        }
    }
    
    protected int[] getBiomes() throws MalformedNbtTagException {
        return VersionBehaviors.parseBiomes_1_16(chunkNbt);
    }
    
    protected void writeBiomes(int[] biomes) throws MalformedNbtTagException {
        VersionBehaviors.writeBiomes_1_16(chunkNbt, biomes);
    }
    
    /**
     * Pack the local block ids into a long array according to the chunk format of the implementing class.
     * @param localBlockIds
     * @param indexSize
     * @return 
     */
    protected long[] packBlockIds(int[] localBlockIds, int indexSize) {
        //default to most recent version
        return VersionBehaviors.packBlockIds_1_16(localBlockIds, indexSize);
    }
    
    /**
     * Unpack a block id from a long array according to the chunk format of the implementing class.
     * @param elementSize
     * @param offset
     * @param source
     * @return 
     */
    protected int readBlockIdFromPackedIds(int elementSize, int offset, long[] source) {
        //default to most recent version
        return VersionBehaviors.readBlockIdFromPackedIds_1_16(elementSize, offset, source);
    }
    
}
