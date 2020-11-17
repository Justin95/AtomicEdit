
package atomicedit.frontend.editor;

import atomicedit.AtomicEdit;
import atomicedit.backend.BlockCoord;
import atomicedit.backend.brushes.Brush;
import atomicedit.backend.brushes.BrushType;
import atomicedit.backend.parameters.Parameters;
import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.frontend.render.RenderObjectCollection;
import atomicedit.frontend.render.utils.VolumeRenderingUtil;
import atomicedit.frontend.ui.BrushGui;
import atomicedit.logging.Logger;
import atomicedit.operations.Operation;
import atomicedit.operations.OperationResult;
import atomicedit.operations.OperationType;
import atomicedit.volumes.Box;
import atomicedit.volumes.Volume;
import atomicedit.volumes.WorldVolume;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;

/**
 *
 * @author justin
 */
public class BrushEditor implements Editor {
    
    private Volume brushVolume;
    private RenderObjectCollection brushRenderable;
    private BrushGui gui;
    private final AtomicEditRenderer renderer;
    private final EditorPointer editorPointer;
    private static Brush usedBrush = BrushType.ELIPSE.createInstance();
    private static final long BRUSH_USE_DELAY = 100; //ms
    private long brushLastUseTime;
    private RepeatOperationThread repeatThread;
    
    public BrushEditor(AtomicEditRenderer renderer, EditorPointer editorPointer) {
        this.renderer = renderer;
        this.editorPointer = editorPointer;
    }
    
    @Override
    public void initialize() {
        this.repeatThread = new RepeatOperationThread(this);
        this.brushLastUseTime = System.currentTimeMillis();
        this.gui = new BrushGui(this);
        renderer.getFrame().getContainer().add(gui.getBrushPanel());
        renderer.getFrame().getContainer().add(gui.getOpPanel());
        setBrush(usedBrush, gui.getBrushParameters());
        this.repeatThread.start();
    }
    
    @Override
    public void renderTick() {
        updateBrushRenderablePosition();
    }
    
    @Override
    public void handleInput(boolean isUiFocused, int key, int action, int mods) {
        //handle key release code even if ui is focused
        if (key == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_RELEASE) {
            this.repeatThread.setActive(false);
        }
        if(isUiFocused){
            return;
        }
        if(key == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_PRESS) {
            doOperationTimed();
            this.repeatThread.setActive(true);
        }
    }
    
    private synchronized void doOperationTimed() {
        long currTime = System.currentTimeMillis();
        if (currTime - brushLastUseTime >= BRUSH_USE_DELAY) {
            doOperation(gui.getOperationType(), gui.getOperationParameters());
            brushLastUseTime = System.currentTimeMillis(); //update last use time as the finish time of the last op to avoid lag issues
        }
    }
    
    public OperationResult doOperation(OperationType opType, Parameters params) {
        Vector3i center = editorPointer.getSelectorPoint();
        Box box = brushVolume.getEnclosingBox();
        //this might be off by one, not a big deal as long as its consistant with the rendering position
        BlockCoord smallestCoord = new BlockCoord(center.x - box.getXLength() / 2, center.y - box.getYLength() / 2, center.z - box.getZLength() / 2);
        WorldVolume worldVolume = new WorldVolume(this.brushVolume, smallestCoord);
        OperationResult result;
        try {
            Operation op = opType.getOperationInstance(worldVolume, params);
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
    public void cleanUp() {
        this.repeatThread.shutdown();
        renderer.getFrame().getContainer().remove(gui.getBrushPanel());
        renderer.getFrame().getContainer().remove(gui.getOpPanel());
        renderer.getRenderableStage().removeRenderable(this.brushRenderable);
        this.gui = null;
    }
    
    public void setBrush(Brush brush, Parameters params) {
        usedBrush = brush;
        this.brushVolume = brush.getVolume(params);
        this.renderer.getRenderableStage().removeRenderable(brushRenderable);
        this.brushRenderable = VolumeRenderingUtil.createVolumeRenderable(brushVolume);
        updateBrushRenderablePosition();
        this.renderer.getRenderableStage().addRenderable(brushRenderable);
    }
    
    private void updateBrushRenderablePosition() {
        Vector3i center = editorPointer.getSelectorPoint();
        Box box = brushVolume.getEnclosingBox();
        this.brushRenderable.updatePosition(new Vector3f(
                center.x - box.getXLength() / 2,
                center.y - box.getYLength() / 2,
                center.z - box.getZLength() / 2
        ));
    }
    
    private static class RepeatOperationThread extends Thread {
        
        private static long SLEEP_TIME_MS = 10;
        private boolean active;
        private boolean keepAlive;
        private final BrushEditor brushEditor;
        
        RepeatOperationThread(BrushEditor brushEditor) {
            this.setName("Brush Repeat Operation Thread");
            this.active = false;
            this.keepAlive = true;
            this.brushEditor = brushEditor;
        }
        
        @Override
        public void run() {
            while (keepAlive) {
                if (isActive()) {
                    brushEditor.doOperationTimed();
                }
                sleep();
            }
        }
        
        private void sleep() {
            try {
                Thread.sleep(SLEEP_TIME_MS);
            } catch (InterruptedException e) {
                //pass
            }
        }
        
        public void shutdown() {
            this.keepAlive = false;
            this.interrupt();
            try {
                this.join();
            } catch (InterruptedException e) {
                //pass
            }
        }
        
        public synchronized void setActive(boolean active) {
            this.active = active;
        }
        
        public synchronized boolean isActive() {
            return this.active;
        }
        
    }
    
}
