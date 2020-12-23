package atomicedit.backend.schematic;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.World;
import atomicedit.backend.blockentity.BlockEntityUtils;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.dimension.Dimension;
import atomicedit.backend.entity.Entity;
import atomicedit.backend.entity.EntityCoord;
import atomicedit.backend.entity.EntityUtils;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtCompoundTag;
import atomicedit.backend.utils.BitArray;
import atomicedit.backend.utils.ChunkUtils;
import atomicedit.backend.utils.GeneralUtils;
import atomicedit.logging.Logger;
import atomicedit.operations.OperationResult;
import atomicedit.volumes.Box;
import atomicedit.volumes.Volume;
import atomicedit.volumes.WorldVolume;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.joml.Vector3i;

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
        Collection<Entity> entities = includeEntities ? ChunkUtils.readEntitiesFromChunkControllers(controllers.values(), volume) : null;
        entities = EntityUtils.translateEntityCoordsToVolume(entities, volume);
        Collection<BlockEntity> blockEntities = includeBlockEntities ? ChunkUtils.readBlockEntitiesFromChunkControllers(controllers.values(), volume) : null;
        blockEntities = BlockEntityUtils.translateBlockEntityCoordsToVolume(blockEntities, volume);
        return new Schematic(volume, blocks, entities, blockEntities);
    }
    
    /**
     * Create a new schematic that is rotated right a certain number of times.
     * @param original the original schematic
     * @param rightRotations
     * @return 
     */
    public static Schematic createRotatedSchematic(Schematic original, int rightRotations) {
        final int rightRots = (rightRotations % 4 + 4) % 4;
        //create new volume
        Box origBox = original.volume.getEnclosingBox();
        Volume newVolume = Volume.getInstance(
            new Vector3i(0,0,0),
            new Vector3i(
                (rightRots % 2 == 1 ? origBox.getZLength() : origBox.getXLength()) - 1,
                origBox.getYLength() - 1,
                (rightRots % 2 == 1 ? origBox.getXLength() : origBox.getZLength()) - 1
            )
        );
        //create new blocks
        Box newBox = newVolume.getEnclosingBox();
        short[] newBlocks = new short[original.blocks.length];
        BitArray includedSet = new BitArray(newBox.getNumBlocksContained());
        final int origXLen = origBox.getXLength();
        final int origZLen = origBox.getZLength();
        origBox.doForXyz((x, y, z) -> {
            int newX = calcNewX(x, z, origXLen, origZLen, rightRots);
            int newY = y;
            int newZ = calcNewZ(x, z, origXLen, origZLen, rightRots);
            final int newIndex = GeneralUtils.getIndexYZX(newX, newY, newZ, newBox.getXLength(), newBox.getZLength());
            final int oldIndex = GeneralUtils.getIndexYZX(x, y, z, origBox.getXLength(), origBox.getZLength());
            includedSet.set(newIndex, original.volume.getIncludedSet().get(oldIndex));
            newBlocks[newIndex] = original.blocks[oldIndex];
        });
        //create new entity list
        List<Entity> newEntities = new ArrayList<>(original.entities.size());
        for (Entity entity : original.entities) {
            Entity copy = entity.copy();
            EntityCoord coord;
            try {
                coord = copy.getCoord();
            } catch (MalformedNbtTagException e) {
                Logger.error("Error rotating entity in schematic.", e);
                coord = new EntityCoord(0, 0, 0);
            }
            double newX = calcNewXDouble(coord.x, coord.z, origXLen, origZLen, rightRots);
            double newY = coord.y;
            double newZ = calcNewZDouble(coord.x, coord.z, origXLen, origZLen, rightRots);
            copy.setCoord(newX, newY, newZ);
            newEntities.add(copy);
        }
        
        //create new block entity list
        List<BlockEntity> newBlockEntities = new ArrayList<>(original.blockEntities.size());
        for (BlockEntity entity : original.blockEntities) {
            BlockEntity copy = entity.copy();
            BlockCoord coord;
            try {
                coord = copy.getBlockCoord();
            } catch (MalformedNbtTagException e) {
                Logger.error("Error rotating block entity in schematic.", e);
                coord = new BlockCoord(0, 0, 0);
            }
            int newX = calcNewX(coord.x, coord.z, origXLen, origZLen, rightRots);
            int newY = coord.y;
            int newZ = calcNewZ(coord.x, coord.z, origXLen, origZLen, rightRots);
            copy.setBlockCoord(newX, newY, newZ);
            newBlockEntities.add(copy);
        }
        
        return new Schematic(newVolume, newBlocks, newEntities, newBlockEntities);
    }
    
    private static int calcNewX(int x, int z, int origXLen, int origZLen, int rightRots) {
        switch (rightRots) {
            case 0:
                return x;
            case 1:
                return (origZLen - 1) - z;
            case 2:
                return (origXLen - 1) - x;
            case 3:
                return z;
            default:
                throw new IllegalArgumentException("Bad number of rotations.");
        }
    }
    
    private static int calcNewZ(int x, int z, int origXLen, int origZLen, int rightRots) {
        switch (rightRots) {
            case 0:
                return z;
            case 1:
                return x;
            case 2:
                return (origZLen - 1) - z;
            case 3:
                return (origXLen - 1) - x;
            default:
                throw new IllegalArgumentException("Bad number of rotations.");
        }
    }
    
    private static double calcNewXDouble(double x, double z, int origXLen, int origZLen, int rightRots) {
        switch (rightRots) {
            case 0:
                return x;
            case 1:
                return (origZLen - 1) - z;
            case 2:
                return (origXLen - 1) - x;
            case 3:
                return z;
            default:
                throw new IllegalArgumentException("Bad number of rotations.");
        }
    }
    
    private static double calcNewZDouble(double x, double z, int origXLen, int origZLen, int rightRots) {
        switch (rightRots) {
            case 0:
                return z;
            case 1:
                return (origXLen - 1) - x;
            case 2:
                return (origZLen - 1) - z;
            case 3:
                return x;
            default:
                throw new IllegalArgumentException("Bad number of rotations.");
        }
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
        //function writing to the world is in the world class to allow updating the unsaved chunks map
        return world.putSchematicIntoWorld(schematic, dim, smallestCoord);
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
    
}
