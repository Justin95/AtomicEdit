
package atomicedit.frontend.editor;

import atomicedit.AtomicEdit;
import atomicedit.backend.BlockCoord;
import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.frontend.render.RenderObject;
import atomicedit.frontend.render.Renderable;
import atomicedit.frontend.ui.AreaSelectionOptionsGui;
import atomicedit.logging.Logger;
import atomicedit.operations.Operation;
import atomicedit.operations.OperationResult;
import atomicedit.operations.OperationType;
import atomicedit.backend.parameters.Parameters;
import atomicedit.volumes.Volume;
import atomicedit.volumes.WorldVolume;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;

/**
 *
 * @author Justin Bonner
 */
public class AreaSelectionEditor implements Editor {
    
    private Vector3i pointA;
    private Vector3i pointB;
    private RenderObject pointerRenderObject;
    private Renderable selectionBoxRenderable;
    private boolean currentlyDrawingBox;
    private final AtomicEditRenderer renderer;
    private AreaSelectionOptionsGui gui;
    private final EditorPointer editorPointer;
    
    public AreaSelectionEditor(AtomicEditRenderer renderer, EditorPointer editorPointer){
        pointA = null;
        pointB = null;
        this.renderer = renderer;
        this.currentlyDrawingBox = false;
        this.editorPointer = editorPointer;
    }
    
    @Override
    public void initialize(){
        this.gui = new AreaSelectionOptionsGui(this);
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
    
    public OperationResult doOperation(OperationType opType, Parameters params){
        if(pointA == null || pointB == null){
            return new OperationResult(false, "Cannot do operation with no volume.");
        }
        WorldVolume worldVolume = WorldVolume.getInstance(pointA, pointB);
        Operation op = opType.getOperationInstance(worldVolume, params);
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
        renderer.getFrame().getContainer().remove(gui.getOpPanel());
        this.gui = null;
    }
    
}
