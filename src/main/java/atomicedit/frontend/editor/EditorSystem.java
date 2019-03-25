
package atomicedit.frontend.editor;

import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.operations.OperationResult;
import atomicedit.operations.OperationType;
import atomicedit.operations.utils.OperationParameters;

/**
 *
 * @author Justin Bonner
 */
public class EditorSystem {
    
    private static final Object EDITOR_LOCK = new Object();
    private static Editor editor;
    private static EditorType editorType;
    private static AtomicEditRenderer renderer;
    private static EditorPointer editorPointer;
    
    public static void initialize(AtomicEditRenderer aeRenderer){
        renderer = aeRenderer;
        editorPointer = new EditorPointer();
        editorPointer.updatePosition(renderer.getCamera().getPosition(), renderer.getCamera().getRotation(), 0);
    }
    
    public static void renderTick(){
        synchronized(EDITOR_LOCK){
            editor.renderTick();
        }
    }
    
    public static void handleInput(boolean isUiFocused, int key, int action, int mods){
        synchronized(EDITOR_LOCK){
            editor.handleInput(isUiFocused, key, action, mods);
        }
    }
    
    public static OperationResult doOperation(OperationType opType, OperationParameters params){
        synchronized(EDITOR_LOCK){
            return editor.doOperation(opType, params);
        }
    }
    
    public static void setEditorType(EditorType newEditorType){
        synchronized(EDITOR_LOCK){
            if(editorType == newEditorType){
                return;
            }
            if(editor != null){
                editor.destory();
            }
            editor = newEditorType.createEditor(renderer, editorPointer);
            editorType = newEditorType;
            editor.initialize();
        }
    }
    
}
