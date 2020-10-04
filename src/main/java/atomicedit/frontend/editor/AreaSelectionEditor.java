
package atomicedit.frontend.editor;

import atomicedit.AtomicEdit;
import atomicedit.backend.BlockCoord;
import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.frontend.render.LinesRenderObject;
import atomicedit.frontend.render.NoTextureRenderObject;
import atomicedit.frontend.render.RenderObject;
import atomicedit.frontend.render.Renderable;
import atomicedit.frontend.ui.AreaSelectionOptionsGui;
import atomicedit.logging.Logger;
import atomicedit.operations.Operation;
import atomicedit.operations.OperationResult;
import atomicedit.operations.OperationType;
import atomicedit.operations.utils.OperationParameters;
import atomicedit.volumes.Volume;
import atomicedit.volumes.WorldVolume;
import java.util.Arrays;
import java.util.List;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;

/**
 *
 * @author Justin Bonner
 */
public class AreaSelectionEditor implements Editor{
    
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
        this.pointerRenderObject = createEditorPointerRenderObject(editorPointer.getSelectorPoint());
        renderer.getFrame().getContainer().add(gui);
        renderer.getRenderableStage().addRenderObject(pointerRenderObject);
    }
    
    @Override
    public void renderTick(){
        editorPointer.updatePosition(renderer.getCamera().getPosition(), renderer.getCamera().getRotation(), 0);
        pointerRenderObject.updatePosition(new Vector3f(editorPointer.getSelectorPoint()));
        if(selectionBoxRenderable != null){
            renderer.getRenderableStage().removeRenderable(selectionBoxRenderable);
            this.selectionBoxRenderable = null;
        }
        if(pointA != null){
            Vector3i secondVec = pointB != null ? pointB : this.editorPointer.getSelectorPoint();
            this.selectionBoxRenderable = createSelectionBoxRenderable(pointA, secondVec);
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
    
    @Override
    public OperationResult doOperation(OperationType opType, OperationParameters params){
        if(pointA == null || pointB == null){
            return new OperationResult(false, "Cannot do operation with no volume.");
        }
        Volume volume = Volume.getInstance(pointA, pointB);
        BlockCoord smallestCoord = new BlockCoord(Math.min(pointA.x, pointB.x), Math.min(pointA.y, pointB.y), Math.min(pointA.z, pointB.z));
        WorldVolume worldVolume = new WorldVolume(volume, smallestCoord);
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
        return result;
    }
    
    private static RenderObject createEditorPointerRenderObject(Vector3i position){
        float min = -0.01f;
        float max = 1.01f;
        return new NoTextureRenderObject(
            new Vector3f(position),
            new Vector3f(0,0,0),
            true,
            new float[]{
                min,min,min,    .33f, .33f, .66f,  .7f,
                min,min,max,    .66f, .66f, .66f,  .7f,
                min,max,min,    .66f, .66f, .66f,  .7f,
                min,max,max,    .66f, .66f, .66f,  .7f,
                max,min,min,    .66f, .66f, .66f,  .7f,
                max,min,max,    .66f, .66f, .66f,  .7f,
                max,max,min,    .66f, .66f, .66f,  .7f,
                max,max,max,    .99f, .99f, .66f,  .7f,
            },
            new int[]{
                0,1,3,  0,3,2, //x = 0 face
                0,4,5,  0,5,1, //y = 0 face
                0,2,6,  0,6,4, //z = 0 face
                4,6,7,  4,7,5, //x = 1 face
                2,3,7,  2,7,6, //y = 1 face
                1,5,7,  1,7,3  //z = 1 face
            }
        );
    }
    
    private static Renderable createSelectionBoxRenderable(Vector3i pointA, Vector3i pointB){
        Vector3f position = new Vector3f(Math.min(pointA.x, pointB.x), Math.min(pointA.y, pointB.y), Math.min(pointA.z, pointB.z));
        Vector3f rotation = new Vector3f(0,0,0);
        float min = -0.02f;
        float xLen = Math.abs(pointA.x - pointB.x) + 1 + .02f;
        float yLen = Math.abs(pointA.y - pointB.y) + 1 + .02f;
        float zLen = Math.abs(pointA.z - pointB.z) + 1 + .02f;
        float[] vertexData = new float[]{
             min, min, min,    .33f, .33f, .66f,  .6f,
             min, min,zLen,    .66f, .66f, .66f,  .6f,
             min,yLen, min,    .66f, .66f, .66f,  .6f,
             min,yLen,zLen,    .66f, .66f, .66f,  .6f,
            xLen, min, min,    .66f, .66f, .66f,  .6f,
            xLen, min,zLen,    .66f, .66f, .66f,  .6f,
            xLen,yLen, min,    .66f, .66f, .66f,  .6f,
            xLen,yLen,zLen,    .99f, .99f, .66f,  .6f,
        };
        int[] faceIndicies = new int[]{
            0,1,3,  0,3,2, //x = 0 face
            0,4,5,  0,5,1, //y = 0 face
            0,2,6,  0,6,4, //z = 0 face
            4,6,7,  4,7,5, //x = 1 face
            2,3,7,  2,7,6, //y = 1 face
            1,5,7,  1,7,3  //z = 1 face
        };
        int[] lineIndicies = new int[]{
            0,1,  0,2,  0,4,
            1,3,  1,5,
            2,3,  2,6,
            3,7,
            4,5,  4,6,
            5,7,
            6,7
        };
        return new SelectionRenderable(
            new NoTextureRenderObject(position, rotation, true, vertexData, faceIndicies),
            new LinesRenderObject(position, rotation, false, vertexData, lineIndicies)
        );
    }
    
    @Override
    public void destory(){
        renderer.getFrame().getContainer().remove(gui);
        this.gui = null;
    }
    
    private static class SelectionRenderable implements Renderable{
        
        List<RenderObject> renderObjects;
        
        SelectionRenderable(RenderObject... renObjects){
            this.renderObjects = Arrays.asList(renObjects);
        }
        
        @Override
        public List<RenderObject> getRenderObjects(){
            return renderObjects;
        }        
    }
    
}
