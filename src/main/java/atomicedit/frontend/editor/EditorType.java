
package atomicedit.frontend.editor;

import atomicedit.frontend.AtomicEditRenderer;

/**
 *
 * @author Justin Bonner
 */
public enum EditorType {
    AREA_SELECTION(
        (renderer, editorPointer) -> new AreaSelectionEditor(renderer, editorPointer)
    ),
    BRUSH_ACTION(
        (renderer, editorPointer) -> new AreaSelectionEditor(renderer, editorPointer) //TODO write brush action editor
    ),
    //SCHEMATIC_TOOL,
    ;
    private final EditorCreator editorCreator;
    
    EditorType(EditorCreator creator){
        this.editorCreator = creator;
    }
    
    public Editor createEditor(AtomicEditRenderer renderer, EditorPointer editorPointer){
        return this.editorCreator.createEditor(renderer, editorPointer);
    }
    
    private interface EditorCreator{
        public Editor createEditor(AtomicEditRenderer renderer, EditorPointer editorPointer);
    }
    
}
