
package atomicedit.frontend.editor;

import atomicedit.frontend.AtomicEditRenderer;

/**
 *
 * @author Justin Bonner
 */
public enum EditorType {
    AREA_SELECTION(
        (renderer) -> new AreaSelectionEditor(renderer)
    ),
    BRUSH_ACTION(
        (renderer) -> new AreaSelectionEditor(renderer) //TODO write brush action editor
    ),
    //SCHEMATIC_TOOL,
    ;
    private final EditorCreator editorCreator;
    
    EditorType(EditorCreator creator){
        this.editorCreator = creator;
    }
    
    public Editor createEditor(AtomicEditRenderer renderer){
        return this.editorCreator.createEditor(renderer);
    }
    
    private interface EditorCreator{
        public Editor createEditor(AtomicEditRenderer renderer);
    }
    
}
