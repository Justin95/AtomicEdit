
package atomicedit.operations.nbt;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.World;
import atomicedit.backend.schematic.Schematic;
import atomicedit.logging.Logger;
import atomicedit.operations.Operation;
import atomicedit.operations.OperationResult;
import atomicedit.volumes.WorldVolume;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3i;

/**
 * An Operation to place a schematic into the world. This operation should be used
 * when the user places a schematic into the world.
 * @author Justin Bonner
 */
public class PlaceSchematicOperation extends Operation {
    
    private final WorldVolume operationVolume; //volume operated on
    private final List<Schematic> schematicBackups; //backup for undos
    private final Schematic toPlace;
    private final int repeatTimes;
    private final Vector3i repeatOffset;
    
    public PlaceSchematicOperation(WorldVolume volume, Schematic toPlace, int repeatTimes, Vector3i repeatOffset){
        this.operationVolume = volume;
        this.toPlace = toPlace;
        this.repeatTimes = repeatTimes;
        this.repeatOffset = repeatOffset;
        this.schematicBackups = new ArrayList<>(repeatTimes + 1);
    }
    
    public static PlaceSchematicOperation getInstance(Schematic toPlace, BlockCoord smallestPoint, int repeatTimes, Vector3i repeatOffset) {
        if (repeatOffset.equals(0, 0, 0)) {
            repeatTimes = 0;
        }
        if (repeatTimes == 0) {
            repeatOffset = new Vector3i(0, 0, 0);
        }
        return new PlaceSchematicOperation(WorldVolume.getInstance(toPlace.volume, smallestPoint), toPlace, repeatTimes, repeatOffset);
    }
    
    @Override
    protected OperationResult doOperation(World world) throws Exception {
        this.schematicBackups.clear();
        int xOff = 0;
        int yOff = 0;
        int zOff = 0;
        for (int i = 0; i <= repeatTimes; i++) { //iterate repeatTimes + 1
            try {
                this.schematicBackups.add(
                    Schematic.createSchematicFromWorld(
                        world,
                        operationDimension,
                        WorldVolume.getInstance(
                            operationVolume,
                            new BlockCoord(
                                operationVolume.getSmallestPoint().x + xOff,
                                operationVolume.getSmallestPoint().y + yOff,
                                operationVolume.getSmallestPoint().z + zOff
                            )
                        )
                    )
                );
                Schematic.putSchematicIntoWorld(
                    world,
                    operationDimension,
                    toPlace,
                    new BlockCoord(
                        operationVolume.getSmallestPoint().x + xOff,
                        operationVolume.getSmallestPoint().y + yOff,
                        operationVolume.getSmallestPoint().z + zOff
                    )
                );
            } catch (Exception e) {
                Logger.error("Could not place schematic into world.", e);
                return new OperationResult(false, "Failed to place schematic.", e);
            }
            xOff += repeatOffset.x;
            yOff += repeatOffset.y;
            zOff += repeatOffset.z;
        }
        return new OperationResult(true);
    }
    
    @Override
    protected OperationResult undoOperation(World world) throws Exception {
        for (int i = schematicBackups.size() - 1; i >= 0; i--) {
            Schematic backup = schematicBackups.get(i);
            BlockCoord initPoint = operationVolume.getSmallestPoint();
            BlockCoord smallestPoint = new BlockCoord(
                initPoint.x + repeatOffset.x * i,
                initPoint.y + repeatOffset.y * i,
                initPoint.z + repeatOffset.z * i
            );
            Schematic.putSchematicIntoWorld(world, operationDimension, backup, smallestPoint);
        }
        return new OperationResult(true);
    }
    
    @Override
    public WorldVolume getWorldVolume(){
        /*
        This operation is a special case in that this volume does not necessarily cover all
        the changes made by this operation. Repeated places are not included in this volume.
        However the unsaved chunk map will still be updated by those repeated schematic places
        because schematic placements go through the World class which updates the unsaved chunk
        map. Expanding this volume to cover all repeated places would be messy and would be very
        memory unfriendly in worst case.
        */
        return this.operationVolume;
    }
    
}
