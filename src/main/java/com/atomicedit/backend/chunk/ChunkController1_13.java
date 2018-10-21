
package com.atomicedit.backend.chunk;

import com.atomicedit.backend.BlockCoord;
import com.atomicedit.backend.BlockEntity.BlockEntity;
import com.atomicedit.backend.BlockStateProperty;
import com.atomicedit.backend.BlockType;
import com.atomicedit.backend.ChunkSectionCoord;
import com.atomicedit.backend.GlobalBlockTypeMap;
import com.atomicedit.backend.entity.Entity;
import com.atomicedit.backend.nbt.MalformedNbtTagException;
import com.atomicedit.backend.nbt.NbtByteArrayTag;
import com.atomicedit.backend.nbt.NbtCompoundTag;
import com.atomicedit.backend.nbt.NbtTag;
import com.atomicedit.backend.nbt.NbtTypes;
import com.atomicedit.backend.utils.GeneralUtils;
import com.atomicedit.logging.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author Justin Bonner
 */
public class ChunkController1_13 extends ChunkController{
    
    
    private ChunkCoord coord;
    private NbtCompoundTag chunkNbt;
    private ChunkSection[] chunkSectionCache;
    private boolean chunkSectionCacheIsDirty;
    
    
    public ChunkController1_13(Chunk chunk) throws MalformedNbtTagException{
        super(chunk);
        this.chunkNbt = chunk.getChunkTag();
        this.coord = new ChunkCoord(getLevel().getIntTag("xPos").getPayload(), getLevel().getIntTag("zPos").getPayload());
        this.chunkSectionCache = new ChunkSection[Chunk.NUM_CHUNK_SECTIONS_IN_CHUNK];
        this.chunkSectionCacheIsDirty = false;
    }
    
    private NbtCompoundTag getLevel() throws MalformedNbtTagException{
        return this.chunkNbt.getCompoundTag("Level");
    }
    
    @Override
    public ChunkCoord getChunkCoord() {
        return this.coord;
    }
    
    @Override
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
    public BlockType getBlockAt(BlockCoord coord) throws MalformedNbtTagException{
        if(this.chunkSectionCacheIsDirty || chunkSectionCache[coord.getSubChunkIndex()] == null){
            readChunkSectionIntoCache(coord.getSubChunkIndex());
        }
        short[] blockIds = chunkSectionCache[coord.getSubChunkIndex()].getBlockIds();
        int index = GeneralUtils.getIndexYZX(coord.getChunkLocalX(), coord.getSubChunkLocalY(), coord.getChunkLocalZ(), ChunkSection.SIDE_LENGTH);
        short globalId = blockIds[index];
        return GlobalBlockTypeMap.getBlockType(globalId);
    }
    
    @Override
    public void setBlockAt(BlockCoord coord, BlockType block) throws MalformedNbtTagException{
        if(this.chunkSectionCacheIsDirty || chunkSectionCache[coord.getSubChunkIndex()] == null){
            readChunkSectionIntoCache(coord.getSubChunkIndex());
        }
        short[] blockIds = chunkSectionCache[coord.getSubChunkIndex()].getBlockIds();
        int index = GeneralUtils.getIndexYZX(coord.getChunkLocalX(), coord.getSubChunkLocalY(), coord.getChunkLocalZ(), ChunkSection.SIDE_LENGTH);
        blockIds[index] = GlobalBlockTypeMap.getBlockId(block);
        writeChunkSectionCacheIntoNbt(coord.getSubChunkIndex());
        declareNbtChanged();
    }
    
    @Override
    public short[] getBlocks(int subChunkIndex) throws MalformedNbtTagException{
        if(this.chunkSectionCacheIsDirty || chunkSectionCache[subChunkIndex] == null){
            readChunkSectionIntoCache(subChunkIndex);
        }
        return this.chunkSectionCache[subChunkIndex].getBlockIds();
    }
    
    @Override
    public void setBlocks(int subChunkIndex, short[] blocks) throws MalformedNbtTagException{
        if(this.chunkSectionCacheIsDirty || chunkSectionCache[subChunkIndex] == null){
            readChunkSectionIntoCache(subChunkIndex);
        }
        if(blocks.length != ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION){
            throw new IllegalArgumentException("Wrong number of blocks tried to occupy chunk sub section");
        }
        if(blocks != chunkSectionCache[subChunkIndex].getBlockIds()){ //if the array we are trying to set not in the same location as the one we already have
            System.arraycopy(blocks, 0, chunkSectionCache[subChunkIndex].getBlockIds(), 0, ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION);
        }
        writeChunkSectionCacheIntoNbt(subChunkIndex);
        declareNbtChanged();
    }
    
    private void readChunkSectionIntoCache(int subChunkIndex) throws MalformedNbtTagException{
        ChunkSectionCoord sectionCoord = new ChunkSectionCoord(coord.x, subChunkIndex, coord.z);
        for(NbtCompoundTag section : chunkNbt.getListTag("Sections").getCompoundTags()){
            if(section.getByteTag("Y").getPayload() == subChunkIndex){
                this.chunkSectionCache[subChunkIndex] = readChunkSection(section, sectionCoord);
                break;
            }
        }
        this.chunkSectionCache[subChunkIndex] = makeBlankChunkSection(sectionCoord);
    }
    
    private void writeChunkSectionCacheIntoNbt(int subChunkIndex){
        throw new UnsupportedOperationException(); //TODO
    }
    
    private ChunkSection readChunkSection(NbtCompoundTag sectionTag, ChunkSectionCoord chunkSectionCoord) throws MalformedNbtTagException{
        List<NbtCompoundTag> blockStateNbts = sectionTag.getListTag("Palette").getCompoundTags();
        BlockType[] blockTypes = new BlockType[blockStateNbts.size()];
        for(int i = 0; i < blockStateNbts.size(); i++){
            NbtCompoundTag blockStateTag = blockStateNbts.get(i);
            String blockName = blockStateTag.getStringTag("Name").getPayload();
            BlockStateProperty[] properties = null;
            if(blockStateTag.contains("Properties")){
                NbtCompoundTag propertiesTag = blockStateTag.getCompoundTag("Properties");
                properties = new BlockStateProperty[propertiesTag.getPayloadSize()];
                for(int j = 0; j < properties.length; j++){
                    NbtTag property = propertiesTag.getPayload().get(i);
                    switch(property.getType()){
                        case TAG_INT:
                            properties[i] = new BlockStateProperty(property.getName(), NbtTypes.getInt(property));
                            break;
                        case TAG_BYTE: //assume byte used for boolean
                            properties[i] = new BlockStateProperty(property.getName(), NbtTypes.getByte(property) != 0); //assume non zero value means true boolean
                            break;
                        case TAG_STRING:
                            properties[i] = new BlockStateProperty(property.getName(), NbtTypes.getString(property));
                            break;
                        default:
                            Logger.warning("Unexpected NBT type in block properties: " + property.getName() + " " + property.getType());
                    }
                }
            }
            blockTypes[i] = BlockType.getBlockType(blockName, properties);
        }
        int indexSize = getIndexSize(blockTypes.length);
        long[] localBlockIds = sectionTag.getLongArrayTag("BlockStates").getPayload();
        short[] blocks = new short[ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION];
        for(int i = 0; i < ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION; i++){
            BlockType blockType = blockTypes[GeneralUtils.readIntFromPackedArray(indexSize, i, localBlockIds)];
            blocks[i] = GlobalBlockTypeMap.getBlockId(blockType);
        }
        byte[] blockLight = sectionTag.getByteArrayTag("BlockLight").getPayload();
        byte[] skyLight = sectionTag.getByteArrayTag("SkyLight").getPayload();
        return new ChunkSection(chunkSectionCoord, blocks, blockLight, skyLight);
    }
    
    
    private int getIndexSize(int length) throws MalformedNbtTagException{ 
        if(length > ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION + 1){//if length is greater than max blocks in a sub section + 1 (air)
            throw new MalformedNbtTagException("Too many blocks in palette: " + length);
        }
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
        if(GlobalBlockTypeMap.getBlockId(BlockType.AIR) != 0){
            Arrays.fill(blocks, GlobalBlockTypeMap.getBlockId(BlockType.AIR));
        }
        byte[] blockLight = new byte[(ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION + 1) / 2]; // divid 2 rounds down on odd numbers, add one to make it round up
        byte[] skyLight = new byte[(ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION + 1) / 2]; //may cause lighting errors
        return new ChunkSection(coord, blocks, blockLight, skyLight);
    }
    
    @Override
    public List<Entity> getEntities() throws MalformedNbtTagException{
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
    
    private void declareNbtChanged(){
        this.chunk.setNeedsSaving(true);
    }
    
}
