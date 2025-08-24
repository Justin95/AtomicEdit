
package atomicedit.frontend.ui;

import atomicedit.backend.parameters.Parameters;
import atomicedit.frontend.editor.AreaSelectionEditor;
import atomicedit.frontend.ui.components.ParameterDescriptorGui;
import atomicedit.operations.OperationType;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;

/**
 *
 * @author Justin Bonner
 */
public class AreaSelectionOptionsGui {
    
    private static final int OPERATION_SELECT_HEIGHT = 6;
    
    private final AreaSelectionEditor editor;
    private final ImInt currOperationItem;
    private OperationType operationType;
    private Parameters opParameters;
    private ParameterDescriptorGui paramDescGui;
    
    public AreaSelectionOptionsGui(AreaSelectionEditor editor) {
        this.editor = editor;
        currOperationItem = new ImInt();
        operationType = OperationType.SET_BLOCKS_OPERATION;
        opParameters = Parameters.withDefaults(operationType.getOperationParameterDescription());
        paramDescGui = new ParameterDescriptorGui();
    }
    
    public void updateUi() {
        int winFlags = ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoDecoration;
        ImGui.setNextWindowPos(0f, 100, 0f, 0f);
        ImGui.setNextWindowSize(400f, 800f);
        if(ImGui.begin("###area_select_options_gui", winFlags)){

            // operation select box
            {
                String[] operationStrs = OperationType.getDisplayNames();
                ImGui.setNextItemWidth(200); //temp solution
                if (ImGui.combo("###operation_combo", currOperationItem, operationStrs, OPERATION_SELECT_HEIGHT)) {
                    OperationType newOpType = this.operationType;
                    final String newOpName = operationStrs[currOperationItem.intValue()];
                    for (OperationType op : OperationType.values()) {
                        if (op.getOperationName().equals(newOpName)) {
                            newOpType = op;
                            break;
                        }
                    }
                    this.operationType = newOpType;
                    this.opParameters = Parameters.withDefaults(operationType.getOperationParameterDescription());
                }
            }

            // Do operation button
            if (ImGui.button("Do Operation")) {
                editor.doOperation(operationType, opParameters);
            }

            ImGui.separator();

            //Parameters
            paramDescGui.updateUi(operationType.getOperationParameterDescription(), opParameters);


            ImGui.end();
        }
    }
    
}
