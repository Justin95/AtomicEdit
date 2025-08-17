package atomicedit.frontend.ui.components;

import atomicedit.backend.BlockState;
import atomicedit.backend.BlockStateProperty;
import atomicedit.backend.BlockStateProperty.BlockStateDataType;
import atomicedit.backend.parameters.ParameterDescriptor;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author justin
 */
public class BlockSelectorGui {
    
    private ImBoolean windowVisible;
    private String id;
    private String blockStateName;
    private Map<String, List<BlockStateProperty>> possibleBlockStateProperties;
    private Map<String, BlockStateProperty> blockStateProperties;
    private ImInt nameIdx;
    private String[] blockStateNames;
    
    public BlockSelectorGui(String id) {
        windowVisible = new ImBoolean(false);
        this.id = id;
        this.nameIdx = new ImInt(0);
        this.blockStateNames = sortedBlockStateNames();
        this.possibleBlockStateProperties = Collections.emptyMap();
        this.blockStateProperties = Collections.emptyMap();
    }
    
    
    public BlockState updateUi(BlockState block, ParameterDescriptor paramDesc) {
        BlockState result = block;
        
        if (ImGui.button(paramDesc.name + "###select_block_type_button_" + id)) {
            windowVisible.set(true);
            ImGui.setNextWindowPos(500f, 100, 0f, 0f);
            ImGui.setNextWindowSize(400f, 400f);
        }
        ImGui.sameLine();
        ImGui.text(block.name);
        if (windowVisible.get()) {
            ImGui.begin("Select Block Type: " + paramDesc.name +"###select_block_type_window_" + id, windowVisible);
            //select button
            if (ImGui.button("Select")) {
                result = BlockState.lookupBlockState(blockStateName, blockStateProperties.values());
                windowVisible.set(false);
            }
            
            int initIndex = nameIdx.get();
            ImGui.listBox("Block###block_type_box", nameIdx, blockStateNames, 10);
            boolean changedBlock = initIndex != nameIdx.get();
            blockStateName = blockStateNames[nameIdx.get()];
            
            if (changedBlock) {
                possibleBlockStateProperties = BlockState.getPossibleBlockStateProperties(blockStateName);
                blockStateProperties = new HashMap<>();
            }
            for (String propName : possibleBlockStateProperties.keySet().stream().sorted().toList()) {
                List<BlockStateProperty> properties = possibleBlockStateProperties.get(propName);
                if (properties.isEmpty()) {
                    continue; //Probably won't happen
                }
                switch (properties.get(0).valueType) { //Assume all properties are the same data type, they should be
                    case BlockStateDataType.BOOLEAN -> {
                        BlockStateProperty currVal = blockStateProperties.getOrDefault(propName, properties.get(0));
                        ImBoolean boolBuff = new ImBoolean((Boolean)currVal.VALUE);
                        ImGui.checkbox(propName, boolBuff);
                        boolean val = boolBuff.get();
                        blockStateProperties.put(propName, findMatch(properties, val));
                    }
                    case BlockStateDataType.INTEGER -> {
                        BlockStateProperty currProp = blockStateProperties.getOrDefault(propName, properties.get(0));
                        int currValue = (Integer)currProp.VALUE;
                        int newValue = currValue;
                        if (ImGui.beginCombo(propName, Integer.toString(currValue))) {
                            for (int i = 0; i < properties.size(); i++) {
                                BlockStateProperty prop = properties.get(i);
                                boolean isSelected = prop == currProp;
                                if (ImGui.selectable(prop.VALUE.toString(), isSelected)) {
                                    newValue = (Integer)prop.VALUE;
                                }
                            }
                            ImGui.endCombo();
                        }
                        blockStateProperties.put(propName, findMatch(properties, newValue));
                    }
                    case BlockStateDataType.STRING -> {
                        BlockStateProperty currProp = blockStateProperties.getOrDefault(propName, properties.get(0));
                        String currValue = (String)currProp.VALUE;
                        String newValue = currValue;
                        if (ImGui.beginCombo(propName, currValue)) {
                            for (int i = 0; i < properties.size(); i++) {
                                BlockStateProperty prop = properties.get(i);
                                boolean isSelected = prop == currProp;
                                if (ImGui.selectable(prop.VALUE.toString(), isSelected)) {
                                    newValue = (String)prop.VALUE;
                                }
                            }
                            ImGui.endCombo();
                        }
                        blockStateProperties.put(propName, findMatch(properties, newValue));
                    }
                }
            }
            
            ImGui.end();
        }
        
        return result;
    }
    
    private static BlockStateProperty findMatch(List<BlockStateProperty> props, Object val) {
        for (BlockStateProperty prop : props) {
            if (prop.VALUE.equals(val)) {
                return prop;
            }
        }
        throw new RuntimeException("Block state property not found for value: `" + val + "`.");
    }
    
    private static String[] sortedBlockStateNames() {
        List<String> base = BlockState.getBlockStateNames();
        base.sort(Comparator.naturalOrder());
        base.remove(BlockState.AIR.name);
        base.addFirst(BlockState.AIR.name);
        return base.toArray(String[]::new);
    }
    
    
    
}
