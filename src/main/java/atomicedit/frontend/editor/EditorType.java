
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
        (renderer, editorPointer) -> new BrushEditor(renderer, editorPointer)
    ),
    SCHEMATIC_EDITOR(
        (renderer, editorPointer) -> new SchematicEditor(renderer, editorPointer)
    ),
    BLOCK_ENTITY_EDITOR(
        (renderer, editorPointer) -> new BlockEntityEditor(renderer, editorPointer)
    ),
    ENTITY_EDITOR(
        (renderer, editorPointer) -> new EntityEditor(renderer, editorPointer)
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
