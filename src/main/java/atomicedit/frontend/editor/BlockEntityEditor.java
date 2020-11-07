
package atomicedit.frontend.editor;

import atomicedit.AtomicEdit;
import atomicedit.backend.BackendController;
import atomicedit.backend.BlockCoord;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.ChunkReader;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtTag;
import atomicedit.backend.nbt.NbtTypes;
import atomicedit.backend.utils.ChunkUtils;
import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.frontend.render.RenderObject;
import atomicedit.frontend.render.Renderable;
import atomicedit.frontend.ui.BlockEntityEditorGui;
import atomicedit.frontend.ui.NbtEditorWidget;
import atomicedit.frontend.ui.NbtEditorWidget.ChangedNbtCallback;
import atomicedit.logging.Logger;
import atomicedit.operations.Operation;
import atomicedit.operations.OperationResult;
import atomicedit.operations.nbt.EditBlockEntityOperation;
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
public class BlockEntityEditor implements Editor {
    
    private Vector3i pointA;
    private Vector3i pointB;
    private RenderObject pointerRenderObject;
    private Renderable selectionBoxRenderable;
    private boolean currentlyDrawingBox;
    private final AtomicEditRenderer renderer;
    private BlockEntityEditorGui gui;
    private final EditorPointer editorPointer;
    private volatile boolean editorWidgetOpen;
    
    public BlockEntityEditor(AtomicEditRenderer renderer, EditorPointer editorPointer){
        pointA = null;
        pointB = null;
        this.renderer = renderer;
        this.currentlyDrawingBox = false;
        this.editorPointer = editorPointer;
        this.editorWidgetOpen = false;
    }
    
    @Override
    public void initialize(){
        this.gui = new BlockEntityEditorGui(this);
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
            if(pointA != null && pointB != null){ //already have full box
                pointA = null; //clear existing box
                pointB = null;
            }
            if(!currentlyDrawingBox){
                pointA = editorPointer.getSelectorPoint();
            }else{
                pointB = editorPointer.getSelectorPoint();
            }
            currentlyDrawingBox = !currentlyDrawingBox;
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
            Collection<BlockEntity> blockEntities = ChunkUtils.readBlockEntitiesFromChunkReaders(chunks.values(), worldVolume);
            nbtTags = blockEntities.stream().map((blockEntity) -> blockEntity.getNbtData()).collect(Collectors.toList());
        } catch (MalformedNbtTagException e) {
            Logger.warning("Cannot edit Block Entity NBT due to exception.", e);
            return null;
        }
        
        this.editorWidgetOpen = true;
        ChangedNbtCallback callback = (vol, originalTags, newTags) -> {
            List<BlockEntity> originals = new ArrayList<>();
            for (NbtTag tag : originalTags) {
                if (tag == null) {
                    originals.add(null);
                } else {
                    try {
                        originals.add(new BlockEntity(NbtTypes.getAsCompoundTag(tag)));
                    } catch (MalformedNbtTagException e) {
                        Logger.warning("Block Entity NBT is not a compound tag.", e);
                        originals.add(null);
                    }
                }
            }
            List<BlockEntity> changes = new ArrayList<>();
            for (NbtTag tag : newTags) {
                if (tag == null) {
                    changes.add(null);
                } else {
                    try {
                        changes.add(new BlockEntity(NbtTypes.getAsCompoundTag(tag)));
                    } catch (MalformedNbtTagException e) {
                        Logger.warning("Block Entity NBT is not a compound tag.", e);
                        changes.add(null);
                    }
                }
            }
            doOperation(vol, originals, changes);
        };
        NbtEditorWidget editorWidget = new NbtEditorWidget("Edit Block Entities", worldVolume, callback, nbtTags);
        editorWidget.addWidgetCloseEventListener((event) -> {
            this.editorWidgetOpen = false;
        });
        //set callback
        renderer.getFrame().getContainer().add(editorWidget);
        return editorWidget;
    }
    
    public OperationResult doOperation(WorldVolume volume, List<BlockEntity> originals, List<BlockEntity> changes) {
        this.editorWidgetOpen = false;
        if (volume == null) {
            return new OperationResult(false, "Cannot do operation with no volume");
        }
        Operation op = new EditBlockEntityOperation(volume, originals, changes);
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
    
    
    @Override
    public void cleanUp(){
        renderer.getRenderableStage().removeRenderable(this.selectionBoxRenderable);
        renderer.getRenderableStage().removeRenderObject(this.pointerRenderObject);
        renderer.getFrame().getContainer().removeAll(gui.getAllActiveComponents());
        this.gui = null;
    }
    
}
