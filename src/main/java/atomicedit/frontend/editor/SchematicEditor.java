
package atomicedit.frontend.editor;

import atomicedit.AtomicEdit;
import atomicedit.backend.BackendController;
import atomicedit.backend.BlockCoord;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtCompoundTag;
import atomicedit.backend.nbt.NbtTag;
import atomicedit.backend.nbt.NbtTypes;
import atomicedit.backend.schematic.AeSchematicFormatV1;
import atomicedit.backend.schematic.Schematic;
import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.frontend.render.RenderObject;
import atomicedit.frontend.render.Renderable;
import atomicedit.frontend.ui.SchematicEditorGui;
import atomicedit.logging.Logger;
import atomicedit.operations.OperationResult;
import atomicedit.operations.nbt.PlaceSchematicOperation;
import atomicedit.volumes.WorldVolume;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.liquidengine.legui.component.Component;
import org.lwjgl.glfw.GLFW;

/**
 *
 * @author Justin Bonner
 */
public class SchematicEditor implements Editor {
    
    public static final int MAX_REPEAT_TIMES = 512;
    public static final String SCHEMATIC_EXT = ".schematic";
    
    //status
    private EditorStatus status;
    private boolean useBrushPlacement;
    
    private final BackendController backendController;
    private RenderObject pointerRenderObject;
    private Renderable selectionBoxRenderable;
    private final AtomicEditRenderer renderer;
    private final EditorPointer editorPointer;
    private SchematicEditorGui gui;
    private Schematic heldSchematic;
    private int rightRotations;
    private boolean yFlip;
    private boolean processFlip;
    private Renderable schematicRenderable;
    private Vector3i schemPlacementMinPos;
    private int repeatTimes;
    private Vector3i repeatOffset;
    
    public SchematicEditor(AtomicEditRenderer renderer, EditorPointer editorPointer){
        this.backendController = AtomicEdit.getBackendController();
        this.renderer = renderer;
        this.editorPointer = editorPointer;
        this.useBrushPlacement = false;
        this.repeatTimes = 0;
        this.repeatOffset = new Vector3i(0, 0, 0);
        this.rightRotations = 0;
        this.yFlip = false;
        this.processFlip = false;
    }
    
    @Override
    public void initialize() {
        Component root = renderer.getFrame().getContainer();
        this.gui = new SchematicEditorGui(this, root);
        this.pointerRenderObject = EditorUtils.createEditorPointerRenderObject(editorPointer.getSelectorPoint());
        setStatus(EditorStatus.SELECT);
        root.add(gui.getSchematicPanel());
        renderer.getRenderableStage().addRenderObject(pointerRenderObject);
    }
    
    @Override
    public void renderTick() {
        //handle flip
        if (this.processFlip) {
            this.processFlip = false;
            this.heldSchematic = Schematic.createRotatedSchematic(heldSchematic, 0, true);
            if (this.schematicRenderable != null) {
                renderer.getRenderableStage().removeRenderable(schematicRenderable);
            }
            this.schematicRenderable = EditorUtils.createSchematicRenderable(this.heldSchematic);
            this.renderer.getRenderableStage().addRenderable(schematicRenderable);
        }
        
        int xLen = heldSchematic != null ? heldSchematic.volume.getEnclosingBox().getXLength() : 0;
        int yLen = heldSchematic != null ? heldSchematic.volume.getEnclosingBox().getYLength() : 0;
        int zLen = heldSchematic != null ? heldSchematic.volume.getEnclosingBox().getZLength() : 0;
        float xRotOff = rightRotations == 1 ? zLen : (rightRotations == 2 ? xLen : 0);
        float zRotOff = rightRotations == 2 ? zLen : (rightRotations == 3 ? xLen : 0);
        switch(status) {
            case SELECT:
                pointerRenderObject.updatePosition(new Vector3f(editorPointer.getSelectorPoint()));
                if(selectionBoxRenderable != null){
                    renderer.getRenderableStage().removeRenderable(selectionBoxRenderable);
                    this.selectionBoxRenderable = null;
                }
                final Vector3i pointA = this.editorPointer.getPointA();
                final Vector3i pointB = this.editorPointer.getPointB();
                if(pointA != null){
                    Vector3i secondVec = pointB != null ? pointB : this.editorPointer.getSelectorPoint();
                    this.selectionBoxRenderable = EditorUtils.createSelectionBoxRenderable(pointA, secondVec);
                    renderer.getRenderableStage().addRenderable(this.selectionBoxRenderable);
                }
                break;
            case INITIAL_PLACE:
                schemPlacementMinPos = new Vector3i(
                    editorPointer.getSelectorPoint().x - xLen / 2,
                    editorPointer.getSelectorPoint().y - yLen / 2,
                    editorPointer.getSelectorPoint().z - zLen / 2
                );
                if (schematicRenderable != null) {
                    schematicRenderable.updatePositionAndRotation(
                        new Vector3f(
                            schemPlacementMinPos.x + xRotOff,
                            schemPlacementMinPos.y,
                            schemPlacementMinPos.z + zRotOff
                        ),
                        new Vector3f(0, rightRotations * 90, 0)
                    );
                }
                break;
            case FINE_TUNING:
                if (schematicRenderable != null) {
                    schematicRenderable.updatePositionAndRotation(
                        new Vector3f(
                            schemPlacementMinPos.x + xRotOff,
                            schemPlacementMinPos.y,
                            schemPlacementMinPos.z + zRotOff
                        ),
                        new Vector3f(0, rightRotations * 90, 0)
                    );
                }
                break;
        }
        
    }
    
    @Override
    public void handleInput(boolean isUiFocused, int key, int action, int mods) {
        if (isUiFocused) {
            return;
        }
        if (key == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_PRESS) {
            mainClick();
        }
        if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
            stepBack();
        }
    }
    
    private void mainClick(){
        if (this.status == EditorStatus.SELECT) {
            synchronized (editorPointer) {
                this.editorPointer.clickUpdate();
            }
        } else if (this.status == EditorStatus.INITIAL_PLACE) {
            if (useBrushPlacement) {
                placeSchematic();
            } else {
                placePreview();
            }
        } else if (this.status == EditorStatus.FINE_TUNING) {
            //no raw inputs for FINE_TUNING
        }
        
    }
    
    private void placePreview() {
        
        setStatus(EditorStatus.FINE_TUNING);
    }
    
    @Override
    public void cleanUp() {
        //if a render object or renderable is null thats ok
        renderer.getRenderableStage().removeRenderable(this.schematicRenderable);
        renderer.getRenderableStage().removeRenderable(this.selectionBoxRenderable);
        renderer.getRenderableStage().removeRenderObject(this.pointerRenderObject);
        renderer.getFrame().getContainer().remove(gui.getSchematicPanel());
    }
    
    public void stepBack() {
        if (status == EditorStatus.INITIAL_PLACE) {
            setStatus(EditorStatus.SELECT);
        } else if (status == EditorStatus.FINE_TUNING) {
            setStatus(EditorStatus.INITIAL_PLACE);
        }
    }
    
    public void pickupSchematic(boolean includeAir) {
        if (this.status != EditorStatus.SELECT) {
            return;
        }
        if (!AtomicEdit.getBackendController().hasWorld()) {
            return; //cannot pickup schematic if no world is loaded
        }
        WorldVolume volume;
        synchronized (editorPointer) {
            final Vector3i pointA = this.editorPointer.getPointA();
            final Vector3i pointB = this.editorPointer.getPointB();
            if (pointA == null || pointB == null) {
                return;
            }
            volume = WorldVolume.getInstance(pointA, pointB);
        }
        Schematic schematic;
        try {
            schematic = backendController.createSchematic(volume);
        } catch (Exception e) {
            Logger.error("Cannot create schematic.", e);
            return;
        }
        
        schemPlacementMinPos = new Vector3i(
            editorPointer.getSelectorPoint().x - volume.getEnclosingBox().getXLength() / 2,
            editorPointer.getSelectorPoint().y - volume.getEnclosingBox().getYLength() / 2,
            editorPointer.getSelectorPoint().z - volume.getEnclosingBox().getZLength() / 2
        );
        setHeldSchematic(schematic);
        setStatus(EditorStatus.INITIAL_PLACE);
    }
    
    public void clearSchematic() {
        setStatus(EditorStatus.SELECT);
    }
    
    public boolean hasSchematic() {
        return this.heldSchematic != null;
    }
    
    public void saveSchematic(File schematicFile) {
        if (schematicFile == null || schematicFile.isDirectory()) {
            return;
        }
        if (this.heldSchematic == null) {
            return;
        }
        NbtTag schematicTag = Schematic.writeSchematicToNbt(AeSchematicFormatV1.getInstance(), heldSchematic);
        try (DataOutputStream fileOut = new DataOutputStream(new FileOutputStream(schematicFile))) {
            NbtTag.writeTag(fileOut, schematicTag);
            Logger.info("Successfully wrote schematic file: " + schematicFile.getAbsolutePath());
        } catch (IOException e) {
            Logger.error("Error writing schematic file: " + schematicFile.getAbsolutePath(), e);
        }
    }
    
    public void loadSchematic(File schematicFile) {
        if (schematicFile == null || schematicFile.isDirectory()) {
            return;
        }
        NbtCompoundTag schematicTag;
        try (DataInputStream input = new DataInputStream(new FileInputStream(schematicFile))) {
            schematicTag = NbtTypes.getAsCompoundTag(NbtTag.readNbt(input));
        } catch (IOException e) {
            Logger.error("Could not read schematic: " + schematicFile.getAbsolutePath(), e);
            return;
        } catch (MalformedNbtTagException e) {
            Logger.error("Could not parse NBT in file: " + schematicFile.getAbsolutePath(), e);
            return;
        }
        Schematic schematic;
        try {
            schematic = Schematic.interpretSchematic(schematicTag);
        } catch (MalformedNbtTagException e) {
            Logger.error("Could not parse NBT as schematic.", e);
            return;
        }
        setHeldSchematic(schematic);
        setStatus(EditorStatus.INITIAL_PLACE);
    }
    
    private void setHeldSchematic(Schematic schematic) {
        this.heldSchematic = schematic;
        this.schematicRenderable = EditorUtils.createSchematicRenderable(schematic);
        this.rightRotations = 0;
        this.renderer.getRenderableStage().addRenderable(schematicRenderable);
    }
    
    public void rotateRight() {
        this.rightRotations = ((this.rightRotations + 1) % 4 + 4) % 4;
    }
    
    public void rotateLeft() {
        this.rightRotations = ((this.rightRotations - 1) % 4 + 4) % 4;
    }
    
    public void doYFlip() {
        if (this.processFlip) {
            return; //if last flip has not been processed yet don't do another one
        }
        this.yFlip = !this.yFlip;
        this.processFlip = true;
    }
    
    public void setRepeatTimes(int repeatTimes) {
        this.repeatTimes = repeatTimes;
    }
    
    public void setRepeatOffset(Vector3i repeatOffset) {
        this.repeatOffset = repeatOffset;
    }
    
    /**
     * Tell this editor to use brush placement.
     * Brush placement disables the fine tuning placement step and allows placing the 
     * schematic like a brush. Repeated placement is disabled with brush placement.
     * @param useBrushPlacement
     */
    public void setBrushPlacement(boolean useBrushPlacement) {
        this.useBrushPlacement = useBrushPlacement;
    }
    
    public void adjustOffset(Vector3i adjustment) {
        if (schemPlacementMinPos != null) {
            schemPlacementMinPos = schemPlacementMinPos.add(adjustment, new Vector3i());
        }
    }
    
    public void placeSchematic() {
        if (!AtomicEdit.getBackendController().hasWorld()) {
            return; //cannot place schematic if no world is loaded
        }
        Schematic schematic = updateSchematicWithRotation(heldSchematic, rightRotations, false);
        BlockCoord placementCoord = new BlockCoord(schemPlacementMinPos.x, schemPlacementMinPos.y, schemPlacementMinPos.z);
        PlaceSchematicOperation op = PlaceSchematicOperation.getInstance(schematic, placementCoord, repeatTimes, repeatOffset);
        try {
            OperationResult result = backendController.applyOperation(op);
            Logger.info(result.toString());
        } catch (Exception e) {
            Logger.error("Could not place schematic into world.", e);
            return; //dont change status if an error happens
        }
        setStatus(EditorStatus.INITIAL_PLACE);
    }
    
    private void setStatus(EditorStatus newStatus) {
        //clean up
        if (status != null) {
            switch(status) {
                case SELECT:
                    if(selectionBoxRenderable != null){
                        renderer.getRenderableStage().removeRenderable(selectionBoxRenderable);
                        this.selectionBoxRenderable = null;
                    }
                    renderer.getRenderableStage().removeRenderObject(pointerRenderObject);
                    this.pointerRenderObject = null;
                    break;
                case INITIAL_PLACE:

                    break;
                case FINE_TUNING:

                    break;
            }
        }
        //initialize
        switch(newStatus) {
            case SELECT:
                this.pointerRenderObject = EditorUtils.createEditorPointerRenderObject(editorPointer.getSelectorPoint());
                renderer.getRenderableStage().addRenderObject(pointerRenderObject);
                if (this.schematicRenderable != null) {
                    renderer.getRenderableStage().removeRenderable(schematicRenderable);
                    this.schematicRenderable = null;
                }
                this.heldSchematic = null;
                break;
            case INITIAL_PLACE:
                
                break;
            case FINE_TUNING:
                
                break;
        }
        this.status = newStatus;
        gui.updateStatus(status);
    }
    
    /*
    SELECT -> INITIAL_PLACE -> FINE_TUNING
    FINE_TUNING -> INITIAL_PLACE
    (CANCEL) -> SELECT
    SELECT -> INITIAL_PLACE(brush placement)
    */
    public enum EditorStatus {
        SELECT,
        INITIAL_PLACE,
        FINE_TUNING
    }
    
    /**
     * Loop through each block in the schematic and if it has a property describing rotation around
     * the y axis, "facing" or "rotation", then replace that block with a block with the rotated
     * property. Create a rotated schematic equivilant to schematic.
     * @param schematic
     * @param rightRotations 
     * @param yFlip should the schematic be flipped on the y axis
     * @return the rotated schematic
     */
    private static Schematic updateSchematicWithRotation(Schematic schematic, int rightRotations, boolean yFlip) {
        if (rightRotations == 0 && yFlip == false) {
            return schematic;
        }
        return Schematic.createRotatedSchematic(schematic, rightRotations, yFlip);
    }
    
}
