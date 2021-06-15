package atomicedit.backend.schematic;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.GlobalBlockStateMap;
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
import java.util.Arrays;
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
    private int[] blocks; //blocks stored in y,z,x order, any blocks not in the volume are undefined
    private Collection<Entity> entities;
    private Collection<BlockEntity> blockEntities;

    Schematic(Volume volume, int[] blocks, Collection<Entity> entities, Collection<BlockEntity> blockEntities) {
        if (volume.getEnclosingBox().getNumBlocksContained() != blocks.length) {
            throw new IllegalArgumentException("Number of blocks and volume size differ");
        }
        this.volume = volume;
        this.blocks = blocks;
        this.entities = entities;
        this.blockEntities = blockEntities;
    }

    public static Schematic interpretSchematic(NbtCompoundTag schmaticNbt) throws MalformedNbtTagException {
        return SchematicFileFormats.determineFileFormat(schmaticNbt).readSchematic(schmaticNbt);
    }

    public static NbtCompoundTag writeSchematicToNbt(SchematicFileFormat schematicFileFormat, Schematic schematic) {
        return schematicFileFormat.writeSchematic(schematic);
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
        int[] blocks = ChunkUtils.readBlocksFromChunks(controllers, volume);
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
     * @param yFlip
     * @return 
     */
    public static Schematic createRotatedSchematic(Schematic original, int rightRotations, boolean yFlip) {
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
        int[] newBlocks = new int[original.blocks.length];
        BitArray includedSet = new BitArray(newBox.getNumBlocksContained());
        final int origXLen = origBox.getXLength();
        final int origYLen = origBox.getYLength();
        final int origZLen = origBox.getZLength();
        origBox.doForXyz((x, y, z, blockIndex) -> {
            int newX = calcNewX(x, z, origXLen, origZLen, rightRots);
            int newY = yFlip ? origYLen - 1 - y : y;
            int newZ = calcNewZ(x, z, origXLen, origZLen, rightRots);
            final int newIndex = GeneralUtils.getIndexYZX(newX, newY, newZ, newBox.getXLength(), newBox.getZLength());
            final int oldIndex = blockIndex;
            includedSet.set(newIndex, original.volume.getIncludedSet().get(oldIndex));
            newBlocks[newIndex] = rotateBlock(original.blocks[oldIndex], rightRots, yFlip);
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
            double newY = yFlip ? origYLen - 1 - coord.y : coord.y;
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
            int newY = yFlip ? origYLen - 1 - coord.y : coord.y;
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
    
    private static int rotateBlock(int block, int rightRotations, boolean yFlip) {
        int rotBlock = GlobalBlockStateMap.getRotatedBlockId(block, rightRotations);
        return yFlip ? GlobalBlockStateMap.getFlippedBlockId(block) : rotBlock;
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

    public int[] getBlocks() {
        return this.blocks;
    }
    
    /**
     * Get the block at the relative coordinate in this schematic. Return -1 if the block is not
     * in this schematic volume.
     * @param x the x coord
     * @param y the y coord
     * @param z the z coord
     * @return the block id or -1 if out of bounds
     */
    public int getBlockAt(int x, int y, int z) {
        if (!this.volume.containsXYZ(x, y, z)) {
            return -1;
        }
        int xLen = this.volume.getEnclosingBox().getXLength();
        int zLen = this.volume.getEnclosingBox().getZLength();
        int index = GeneralUtils.getIndexYZX(x, y, z, xLen, zLen);
        return blocks[index];
    }

    public Collection<Entity> getEntities() {
        return this.entities;
    }

    public Collection<BlockEntity> getBlockEntities() {
        return this.blockEntities;
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Schematic:\n");
        str.append("X Length: ").append(volume.getEnclosingBox().getXLength()).append("\n");
        str.append("Y Length: ").append(volume.getEnclosingBox().getYLength()).append("\n");
        str.append("Z Length: ").append(volume.getEnclosingBox().getZLength()).append("\n");
        str.append("Bit Mask: ").append(volume.getIncludedSet()).append("\n");
        str.append("Blocks: ").append(Arrays.toString(blocks)).append("\n");
        str.append("Num Entities: ").append(entities.size()).append("\n");
        str.append("Num Block Entities: ").append(blockEntities.size()).append("\n");
        return str.toString();
    }
    
}
