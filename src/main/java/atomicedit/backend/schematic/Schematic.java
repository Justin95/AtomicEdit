package atomicedit.backend.schematic;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.World;
import atomicedit.backend.blockprovider.BlockProvider;
import atomicedit.backend.blockprovider.SchematicBlockProvider;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.dimension.Dimension;
import atomicedit.backend.entity.Entity;
import atomicedit.backend.entity.EntityCoord;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtCompoundTag;
import atomicedit.backend.nbt.NbtDoubleTag;
import atomicedit.backend.nbt.NbtIntTag;
import atomicedit.backend.nbt.NbtListTag;
import atomicedit.backend.nbt.NbtTag;
import atomicedit.backend.utils.ChunkUtils;
import atomicedit.operations.OperationResult;
import atomicedit.volumes.Volume;
import atomicedit.volumes.WorldVolume;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Store an arbitrarily shaped section of a world. Reference:
 * https://minecraft.gamepedia.com/Schematic_file_format
 *
 * @author Justin Bonner
 */
public class Schematic {

    public final Volume volume;
    private short[] blocks; //blocks stored in y,z,x order, any blocks not in the volume are undefined
    private Collection<Entity> entities;
    private Collection<BlockEntity> blockEntities;

    private Schematic(Volume volume, short[] blocks, Collection<Entity> entities, Collection<BlockEntity> blockEntities) {
        if (volume.getEnclosingBox().getNumBlocksContained() != blocks.length) {
            throw new IllegalArgumentException("Number of blocks and volume size differ");
        }
        this.volume = volume;
        this.blocks = blocks;
        this.entities = entities;
        this.blockEntities = blockEntities;
    }

    public static Schematic interpretSchematic(NbtCompoundTag schmaticNbt) {
        throw new UnsupportedOperationException(); //need different schematic interpreters + factory
    }

    public static NbtCompoundTag writeSchematicToNbt(Schematic schematic) {
        throw new UnsupportedOperationException(); //TODO
    }

    public static Schematic createSchematicFromWorld(World world, Dimension dim, WorldVolume volume) throws Exception {
        return createSchematicFromWorld(world, dim, volume, true, true);
    }

    public static Schematic createSchematicFromWorld(
        World world,
        Dimension dim,
        WorldVolume volume,
        boolean includeEntities,
        boolean includeBlockEntities
    ) throws Exception {
        Map<ChunkCoord, ChunkController> controllers = world.getLoadedChunkStage(dim).getMutableChunks(volume.getContainedChunkCoords());
        short[] blocks = ChunkUtils.readBlocksFromChunks(controllers, volume);
        Collection<Entity> entities = includeEntities ? ChunkUtils.readEntitiesFromChunks(controllers, volume) : null;
        entities = translateEntityCoordsToSchematic(entities, volume);
        Collection<BlockEntity> blockEntities = includeBlockEntities ? ChunkUtils.readBlockEntitiesFromChunks(controllers, volume) : null;
        blockEntities = translateBlockEntityCoordsToSchematic(blockEntities, volume);
        return new Schematic(volume, blocks, entities, blockEntities);
    }

    /**
     * Put a schematic into the world at a given location.
     *
     * @param world
     * @param dim
     * @param schematic
     * @param smallestCoord the smallest block coordinate in the volume, where we want it put into
     * the world.
     * @return
     * @throws Exception
     */
    public static OperationResult putSchematicIntoWorld(World world, Dimension dim, Schematic schematic, BlockCoord smallestCoord) throws Exception {
        BlockProvider provider = new SchematicBlockProvider(schematic);
        WorldVolume volume = new WorldVolume(schematic.volume, smallestCoord);
        Map<ChunkCoord, ChunkController> chunkControllers = world.getLoadedChunkStage(dim).getMutableChunks(volume.getContainedChunkCoords());
        ChunkUtils.writeBlocksIntoChunks(chunkControllers.values(), provider, smallestCoord);
        Collection<BlockEntity> blockEntitiesToRemove = ChunkUtils.readBlockEntitiesFromChunks(chunkControllers, volume);
        ChunkUtils.removeBlockEntitiesFromChunks(chunkControllers, blockEntitiesToRemove);
        //Do not have to remove all entities from schematic destination
        //update entity and block entity positions
        Collection<BlockEntity> blockEntities = translateBlockEntityCoordsToWorld(schematic.blockEntities, volume);
        Collection<Entity> entities = translateEntityCoordsToWorld(schematic.entities, volume);
        ChunkUtils.writeBlockEntitiesIntoChunks(chunkControllers, blockEntities);
        ChunkUtils.writeEntitiesIntoChunks(chunkControllers, entities);
        return new OperationResult(true);
    }

    private static Collection<BlockEntity> translateBlockEntityCoordsToSchematic(Collection<BlockEntity> toUpdate, WorldVolume volume) throws MalformedNbtTagException {
        if (toUpdate == null) {
            return null;
        }
        List<BlockEntity> updated = new ArrayList<>();
        for (BlockEntity blockEntity : toUpdate) {
            BlockCoord coord = blockEntity.getBlockCoord();
            int x = coord.x - volume.getSmallestPoint().x;
            int y = coord.y - volume.getSmallestPoint().y;
            int z = coord.z - volume.getSmallestPoint().z;
            blockEntity.getNbtData().putTag(new NbtIntTag("x", x));
            blockEntity.getNbtData().putTag(new NbtIntTag("y", y));
            blockEntity.getNbtData().putTag(new NbtIntTag("z", z));
        }
        return updated;
    }

    private static Collection<BlockEntity> translateBlockEntityCoordsToWorld(Collection<BlockEntity> toUpdate, WorldVolume volume) throws MalformedNbtTagException {
        if (toUpdate == null) {
            return null;
        }
        List<BlockEntity> updated = new ArrayList<>();
        for (BlockEntity blockEntity : toUpdate) {
            BlockCoord coord = blockEntity.getBlockCoord();
            int x = coord.x + volume.getSmallestPoint().x;
            int y = coord.y + volume.getSmallestPoint().y;
            int z = coord.z + volume.getSmallestPoint().z;
            blockEntity.getNbtData().putTag(new NbtIntTag("x", x));
            blockEntity.getNbtData().putTag(new NbtIntTag("y", y));
            blockEntity.getNbtData().putTag(new NbtIntTag("z", z));
        }
        return updated;
    }
    
    private static Collection<Entity> translateEntityCoordsToSchematic(Collection<Entity> toUpdate, WorldVolume volume) throws MalformedNbtTagException {
        if (toUpdate == null) {
            return null;
        }
        List<Entity> updated = new ArrayList<>();
        for (Entity entity : toUpdate) {
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
        }
        return updated;
    }

    private static Collection<Entity> translateEntityCoordsToWorld(Collection<Entity> toUpdate, WorldVolume volume) throws MalformedNbtTagException {
        if (toUpdate == null) {
            return null;
        }
        List<Entity> updated = new ArrayList<>();
        for (Entity entity : toUpdate) {
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
        }
        return updated;
    }
    
    protected Volume getVolume() {
        return this.volume;
    }

    public short[] getBlocks() {
        return this.blocks;
    }

    public Collection<Entity> getEntities() {
        return this.entities;
    }

    public Collection<BlockEntity> getBlockEntities() {
        return this.blockEntities;
    }

    /*
    private void uncompressSchematic(){ //schematic nbts will be compressed but not in schematic objects
        if(uncompressedBlocks != null) return;
        uncompressedBlocks = new short[volume.getEnclosingBox().getNumBlocksContained()];
        int xLen = volume.getEnclosingBox().getXLength();
        int zLen = volume.getEnclosingBox().getZLength();
        doForBlock((x, y, z, block) -> {
            //if a block isn't set it defaults to 0 which is always counted as minecraft:air, but we shouldnt try to access those anyway as they aren't in the schematic
            uncompressedBlocks[GeneralUtils.getIndexYZX(x, y, z, xLen, zLen)] = block;
        });
    }
     */
}
