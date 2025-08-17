package atomicedit.frontend.ui.components;

import atomicedit.backend.BlockState;
import atomicedit.backend.parameters.ParameterDescriptor;
import atomicedit.backend.parameters.ParameterType;
import atomicedit.backend.parameters.Parameters;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author justin
 */
public class ParameterDescriptorGui {
    
    private final Map<ParameterDescriptor, BlockSelectorGui> blockSelectorWindows;
    
    public ParameterDescriptorGui() {
        blockSelectorWindows = new HashMap<>();
    }
    
    public boolean updateUi(List<ParameterDescriptor> paramDescs, Parameters parameters) {
        ImFloat floatBuff = new ImFloat();
        ImBoolean booleanBuff = new ImBoolean();
        ImInt intBuff = new ImInt();
        ImString stringBuff = new ImString();
        BlockState blockStateBuff;
        boolean changed = false;
        for (ParameterDescriptor paramDesc : paramDescs) {
            switch (paramDesc.parameterType) {
                case ParameterType.INT -> {
                    intBuff.set(parameters.getParamAsInteger(paramDesc));
                    if (ImGui.inputInt(paramDesc.name, intBuff)) {
                        parameters.setParam(paramDesc, intBuff.get());
                        changed = true;
                    }
                }
                case ParameterType.FLOAT -> {
                    floatBuff.set(parameters.getParamAsFloat(paramDesc));
                    if (ImGui.inputFloat(paramDesc.name, floatBuff)) {
                        parameters.setParam(paramDesc, floatBuff.get());
                        changed = true;
                    }
                }
                case ParameterType.BOOLEAN -> {
                    booleanBuff.set(parameters.getParamAsBoolean(paramDesc));
                    if (ImGui.checkbox(paramDesc.name, booleanBuff)) {
                        parameters.setParam(paramDesc, booleanBuff.get());
                        changed = true;
                    }
                }
                case ParameterType.STRING -> {
                    stringBuff.set(parameters.getParamAsString(paramDesc));
                    if (ImGui.inputText(paramDesc.name, stringBuff)) {
                        parameters.setParam(paramDesc, stringBuff.get());
                        changed = true;
                    }
                }
                case ParameterType.BLOCK_SELECTOR -> {
                    blockStateBuff = parameters.getParamAsBlockState(paramDesc);
                    if (!this.blockSelectorWindows.containsKey(paramDesc)) {
                        this.blockSelectorWindows.put(paramDesc, new BlockSelectorGui(paramDesc.name));
                    }
                    BlockSelectorGui gui = blockSelectorWindows.get(paramDesc);
                    BlockState temp = blockStateBuff;
                    blockStateBuff = gui.updateUi(blockStateBuff, paramDesc);
                    if (!temp.equals(blockStateBuff)) {
                        parameters.setParam(paramDesc, blockStateBuff);
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }
    
}
