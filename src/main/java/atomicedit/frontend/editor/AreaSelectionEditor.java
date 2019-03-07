
package atomicedit.frontend.editor;

import atomicedit.AtomicEdit;
import atomicedit.backend.BlockCoord;
import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.frontend.render.RenderObject;
import atomicedit.frontend.ui.AreaSelectionOptionsGui;
import atomicedit.operations.Operation;
import atomicedit.operations.OperationResult;
import atomicedit.operations.OperationType;
import atomicedit.operations.utils.OperationParameters;
import atomicedit.volumes.Volume;
import org.joml.Vector3i;

/**
 *
 * @author Justin Bonner
 */
public class AreaSelectionEditor implements Editor{
    
    private Vector3i pointA;
    private Vector3i pointB;
    private boolean isPointASelected;
    private RenderObject selectionBoxRenderObject;
    private final AtomicEditRenderer renderer;
    private AreaSelectionOptionsGui gui;
    
    public AreaSelectionEditor(AtomicEditRenderer renderer){
        pointA = null;
        pointB = null;
        this.renderer = renderer;
    }
    
    @Override
    public void initialize(){
        this.gui = new AreaSelectionOptionsGui();
        renderer.getFrame().getContainer().add(gui);
    }
    
    @Override
    public void renderTick(){
        
    }
    
    @Override
    public void handleInput(boolean isUiFocused, int key, int action, int mods){
        if(isUiFocused){
            return;
        }
        
    }
    
    @Override
    public OperationResult doOperation(OperationType opType, OperationParameters params){
        Volume volume = Volume.getInstance(pointA, pointB);
        BlockCoord smallestCoord = new BlockCoord(Math.min(pointA.x, pointB.x), Math.min(pointA.y, pointB.y), Math.min(pointA.z, pointB.z));
        Operation op = opType.getOperationInstance(volume, smallestCoord, params);
        OperationResult result = AtomicEdit.getBackendController().applyOperation(op);
        return result;
    }
    
    private static RenderObject createSelectionBoxRenderObject(Vector3i pointA, Vector3i pointB){
        return null;
    }
    
    @Override
    public void destory(){
        renderer.getFrame().getContainer().remove(gui);
        this.gui = null;
    }
    
}
