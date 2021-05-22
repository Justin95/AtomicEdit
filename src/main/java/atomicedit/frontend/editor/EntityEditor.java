
package atomicedit.frontend.editor;

import atomicedit.AtomicEdit;
import atomicedit.backend.BackendController;
import atomicedit.backend.BlockCoord;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.ChunkReader;
import atomicedit.backend.entity.Entity;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtTag;
import atomicedit.backend.nbt.NbtTypes;
import atomicedit.backend.utils.ChunkUtils;
import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.frontend.render.RenderObject;
import atomicedit.frontend.render.Renderable;
import atomicedit.frontend.ui.EntityEditorGui;
import atomicedit.frontend.ui.NbtEditorWidget;
import atomicedit.frontend.ui.NbtEditorWidget.ChangedNbtCallback;
import atomicedit.logging.Logger;
import atomicedit.operations.Operation;
import atomicedit.operations.OperationResult;
import atomicedit.operations.nbt.EditEntityOperation;
import atomicedit.volumes.Volume;
import atomicedit.volumes.WorldVolume;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;

/**
 *
 * @author Justin Bonner
 */
public class EntityEditor implements Editor {
    
    private RenderObject pointerRenderObject;
    private Renderable selectionBoxRenderable;
    private final AtomicEditRenderer renderer;
    private EntityEditorGui gui;
    private final EditorPointer editorPointer;
    private volatile boolean editorWidgetOpen;
    
    public EntityEditor(AtomicEditRenderer renderer, EditorPointer editorPointer){
        this.renderer = renderer;
        this.editorPointer = editorPointer;
        this.editorWidgetOpen = false;
    }
    
    @Override
    public void initialize(){
        this.gui = new EntityEditorGui(this);
        this.pointerRenderObject = EditorUtils.createEditorPointerRenderObject(editorPointer.getSelectorPoint());
        renderer.getFrame().getContainer().add(gui.getOpPanel());
        renderer.getRenderableStage().addRenderObject(pointerRenderObject);
    }
    
    @Override
    public void renderTick() {
        pointerRenderObject.updatePosition(new Vector3f(editorPointer.getSelectorPoint()));
        if(selectionBoxRenderable != null){
            renderer.getRenderableStage().removeRenderable(selectionBoxRenderable);
            this.selectionBoxRenderable = null;
        }
        Vector3i pointA = this.editorPointer.getPointA();
        Vector3i pointB = this.editorPointer.getPointB();
        if(pointA != null){
            Vector3i secondVec = pointB != null ? pointB : this.editorPointer.getSelectorPoint();
            this.selectionBoxRenderable = EditorUtils.createSelectionBoxRenderable(pointA, secondVec);
            renderer.getRenderableStage().addRenderable(this.selectionBoxRenderable);
        }
    }
    
    @Override
    public void handleInput(boolean isUiFocused, int key, int action, int mods){
        if(isUiFocused){
            return;
        }
        if(key == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_PRESS){
            mainClick();
        }
    }
    
    private void mainClick(){
        synchronized(editorPointer){
            editorPointer.clickUpdate();
        }
    }
    
    
    public NbtEditorWidget createEditorWidget() {
        BackendController backendController = AtomicEdit.getBackendController();
        if (!backendController.hasWorld()) {
            return null;
        }
        if (this.editorWidgetOpen) {
            return null;
        }
        Vector3i pointA = this.editorPointer.getPointA();
        Vector3i pointB = this.editorPointer.getPointB();
        if(pointA == null || pointB == null){
            return null; //Cannot do operation with no volume
        }
        Volume volume = Volume.getInstance(pointA, pointB);
        BlockCoord smallestCoord = new BlockCoord(Math.min(pointA.x, pointB.x), Math.min(pointA.y, pointB.y), Math.min(pointA.z, pointB.z));
        WorldVolume worldVolume = new WorldVolume(volume, smallestCoord);
        
        List<NbtTag> nbtTags;
        try {
            Map<ChunkCoord, ChunkReader> chunks = backendController.getReadOnlyChunks(
                worldVolume.getContainedChunkCoords(),
                backendController.getActiveDimension()
            );
            Collection<Entity> entities = ChunkUtils.readEntitiesFromChunkReaders(chunks.values(), worldVolume);
            nbtTags = entities.stream().map((entity) -> entity.getNbtData()).collect(Collectors.toList());
        } catch (MalformedNbtTagException e) {
            Logger.warning("Cannot edit Entity NBT due to exception.", e);
            return null;
        }
        
        this.editorWidgetOpen = true;
        ChangedNbtCallback callback = (vol, newTags) -> {
            List<Entity> changes = new ArrayList<>();
            for (NbtTag tag : newTags) {
                if (tag == null) {
                    changes.add(null);
                } else {
                    try {
                        changes.add(new Entity(NbtTypes.getAsCompoundTag(tag)));
                    } catch (MalformedNbtTagException e) {
                        Logger.warning("Entity NBT is not a compound tag.", e);
                        changes.add(null);
                    }
                }
            }
            doOperation(vol, changes, true);
        };
        NbtEditorWidget editorWidget = new NbtEditorWidget("Edit Entities", worldVolume, callback, nbtTags);
        editorWidget.addWidgetCloseEventListener((event) -> {
            this.editorWidgetOpen = false;
        });
        //set callback
        renderer.getFrame().getContainer().add(editorWidget);
        return editorWidget;
    }
    
    public OperationResult doOperation(WorldVolume volume, List<Entity> changes, boolean replaceExisting) {
        this.editorWidgetOpen = false;
        if (volume == null) {
            return new OperationResult(false, "Cannot do operation with no volume");
        }
        Operation op = new EditEntityOperation(volume, changes, replaceExisting);
        OperationResult result;
        try {
            result = AtomicEdit.getBackendController().applyOperation(op);
        } catch (Exception e) {
            result = new OperationResult(false, "Operation threw Exception.", e);
        }
        if(!result.getSuccess()){
            Logger.notice(result.getMessage(), result.getException());
        }
        Logger.info("Operation Result: " + result);
        return result;
    }
    
    public OperationResult doDeleteOperation() {
        BackendController backendController = AtomicEdit.getBackendController();
        if (!backendController.hasWorld()) {
            return new OperationResult(false, "Cannot do operation with no world.");
        }
        if (this.editorWidgetOpen) {
            return new OperationResult(false, "Should not do operation when widget is open.");
        }
        Vector3i pointA = this.editorPointer.getPointA();
        Vector3i pointB = this.editorPointer.getPointB();
        if(pointA == null || pointB == null){
            return new OperationResult(false, "Cannot do operation with no volume.");
        }
        Volume volume = Volume.getInstance(pointA, pointB);
        BlockCoord smallestCoord = new BlockCoord(Math.min(pointA.x, pointB.x), Math.min(pointA.y, pointB.y), Math.min(pointA.z, pointB.z));
        WorldVolume worldVolume = new WorldVolume(volume, smallestCoord);
        return doOperation(worldVolume, new ArrayList<>(0), true);
    }
    
    @Override
    public void cleanUp(){
        renderer.getRenderableStage().removeRenderable(this.selectionBoxRenderable);
        renderer.getRenderableStage().removeRenderObject(this.pointerRenderObject);
        renderer.getFrame().getContainer().removeAll(gui.getAllActiveComponents());
        this.gui = null;
    }
    
}
