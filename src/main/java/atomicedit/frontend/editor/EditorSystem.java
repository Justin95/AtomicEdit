
package atomicedit.frontend.editor;

import atomicedit.frontend.AtomicEditRenderer;

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
    
    public static void cleanUp() {
        if (editor != null) {
            editor.cleanUp();
        }
    }
    
    public static void renderTick() {
        synchronized(EDITOR_LOCK){
            editorPointer.updatePosition(renderer.getCamera().getPosition(), renderer.getCamera().getRotation(), 0);
            editor.renderTick();
        }
    }
    
    public static void handleInput(boolean isUiFocused, int key, int action, int mods){
        synchronized(EDITOR_LOCK){
            editor.handleInput(isUiFocused, key, action, mods);
        }
    }
    
    public static void handleScrollInput(boolean isUiFocused, double yScroll) {
        synchronized(EDITOR_LOCK){
            if (isUiFocused) {
                return;
            }
            editorPointer.updatePosition(null, null, (float)yScroll);
        }
    }
    
    public static void setEditorType(EditorType newEditorType){
        synchronized(EDITOR_LOCK){
            if(editorType == newEditorType){
                return;
            }
            if(editor != null){
                editor.cleanUp();
            }
            editor = newEditorType.createEditor(renderer, editorPointer);
            editorType = newEditorType;
            editor.initialize();
        }
    }
    
}
