
package atomicedit.frontend.ui;

import atomicedit.backend.nbt.NbtTag;
import atomicedit.frontend.editor.BlockEntityEditor;
import atomicedit.frontend.ui.components.NbtEditorGui;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public class BlockEntityEditorGui {
    
    private final BlockEntityEditor editor;
    private NbtEditorGui editorWindow;
    
    public BlockEntityEditorGui(BlockEntityEditor editor) {
        this.editor = editor;
        this.editorWindow = null;
    }
    
    public void updateUi() {
        int winFlags = ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoDecoration;
        ImGui.setNextWindowPos(0f, 100, 0f, 0f);
        ImGui.setNextWindowSize(400f, 800f);
        if(ImGui.begin("###block_entity_editor_gui", winFlags)) {
            if (ImGui.button("Edit Block Entities")) {
                if (editorWindow == null) {
                    List<NbtTag> entities = editor.getBlockEntitesInSelection();
                    editorWindow = new NbtEditorGui(entities);
                }
            }
            ImGui.end();
        }
        if (editorWindow != null) {
            ImBoolean isOpen = new ImBoolean(true);
            editorWindow.updateUi(isOpen);
            if (!isOpen.get()) {
                editorWindow = null;
            }
        }
    }
    
}
