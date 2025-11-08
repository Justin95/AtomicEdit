package atomicedit.frontend.ui.components;

import atomicedit.backend.nbt.NbtByteArrayTag;
import atomicedit.backend.nbt.NbtByteTag;
import atomicedit.backend.nbt.NbtCompoundTag;
import atomicedit.backend.nbt.NbtDoubleTag;
import atomicedit.backend.nbt.NbtFloatTag;
import atomicedit.backend.nbt.NbtIntArrayTag;
import atomicedit.backend.nbt.NbtIntTag;
import atomicedit.backend.nbt.NbtListTag;
import atomicedit.backend.nbt.NbtLongArrayTag;
import atomicedit.backend.nbt.NbtLongTag;
import atomicedit.backend.nbt.NbtShortTag;
import atomicedit.backend.nbt.NbtStringTag;
import atomicedit.backend.nbt.NbtTag;
import atomicedit.backend.nbt.NbtTypes;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImLong;
import imgui.type.ImString;
import java.util.List;

/**
 *
 * @author justin
 */
public class NbtEditorGui {
    
    private List<NbtTag> nbtTags;
    private boolean initialized;
    
    public NbtEditorGui(List<NbtTag> nbtTags) {
        this.nbtTags = nbtTags;
        initialized = false;
    }
    
    public void updateUi(ImBoolean isOpen) {
        
        if (!initialized) {
            ImGui.setNextWindowPos(500f, 100, 0f, 0f);
            ImGui.setNextWindowSize(600f, 400f);
            initialized = true;
        }
        
        if (ImGui.begin("NBT EDITOR###NbtEditorWindow" + this, isOpen)) {
            for (int i = 0; i < nbtTags.size(); i++) {
                NbtTag tag = nbtTags.get(i);
                ImGui.pushID(i);
                nbtUi(tag);
                ImGui.popID();
                ImGui.separator();
            }
            ImGui.end();
        }
        
    }
    
    private static void nbtUi(NbtTag tag) {
        switch (tag.getType()) {
            case NbtTypes.TAG_BYTE -> {
                NbtByteTag byteTag = (NbtByteTag) tag;
                ImInt intBuff = new ImInt(byteTag.getPayload());
                ImGui.text("i8");
                ImGui.sameLine();
                if (ImGui.inputInt(tag.getName() + "###byte_tag", intBuff)) {
                    byteTag.setPayload(intBuff.byteValue());
                }
            }
            case NbtTypes.TAG_SHORT -> {
                NbtShortTag shortTag = (NbtShortTag) tag;
                ImInt intBuff = new ImInt(shortTag.getPayload());
                ImGui.text("i16");
                ImGui.sameLine();
                if (ImGui.inputInt(tag.getName() + "###short_tag", intBuff)) {
                    shortTag.setPayload(intBuff.shortValue());
                }
            }
            case NbtTypes.TAG_INT -> {
                NbtIntTag intTag = (NbtIntTag) tag;
                ImInt intBuff = new ImInt(intTag.getPayload());
                ImGui.text("i32");
                ImGui.sameLine();
                if (ImGui.inputInt(tag.getName() + "###int_tag", intBuff)) {
                    intTag.setPayload(intBuff.intValue());
                }
            }
            case NbtTypes.TAG_LONG -> {
                NbtLongTag longTag = (NbtLongTag) tag;
                ImLong longBuff = new ImLong(longTag.getPayload());
                ImGui.text("i64");
                ImGui.sameLine();
                if (ImGui.inputScalar(tag.getName() + "###long_tag", longBuff)) {
                    longTag.setPayload(longBuff.longValue());
                }
            }
            case NbtTypes.TAG_FLOAT -> {
                NbtFloatTag floatTag = (NbtFloatTag) tag;
                ImFloat floatBuff = new ImFloat(floatTag.getPayload());
                ImGui.text("f32");
                ImGui.sameLine();
                if (ImGui.inputScalar(tag.getName() + "###float_tag", floatBuff)) {
                    floatTag.setPayload(floatBuff.floatValue());
                }
            }
            case NbtTypes.TAG_DOUBLE -> {
                NbtDoubleTag doubleTag = (NbtDoubleTag) tag;
                ImDouble doubleBuff = new ImDouble(doubleTag.getPayload());
                ImGui.text("f64");
                ImGui.sameLine();
                if (ImGui.inputScalar(tag.getName() + "###double_tag", doubleBuff)) {
                    doubleTag.setPayload(doubleBuff.doubleValue());
                }
            }
            case NbtTypes.TAG_STRING -> {
                NbtStringTag stringTag = (NbtStringTag) tag;
                ImString stringBuff = new ImString(stringTag.getPayload());
                ImGui.text("str");
                ImGui.sameLine();
                if (ImGui.inputText(tag.getName() + "###string_tag", stringBuff)) {
                    stringTag.setPayload(stringBuff.get());
                }
            }
            case NbtTypes.TAG_BYTE_ARRAY -> {
                NbtByteArrayTag byteArrTag = (NbtByteArrayTag) tag;
                ImGui.text("[i8]");
                ImGui.sameLine();
                for (int i = 0; i < byteArrTag.getPayloadSize(); i++) {
                    ImInt intBuff = new ImInt(byteArrTag.getPayload()[i]);
                    if (ImGui.inputInt(tag.getName() + "###byte_arr_tag[" + i + "]", intBuff)) {
                        byteArrTag.getPayload()[i] = intBuff.byteValue();
                    }
                }
            }
            case NbtTypes.TAG_INT_ARRAY -> {
                NbtIntArrayTag intArrTag = (NbtIntArrayTag) tag;
                ImGui.text("[i32]");
                ImGui.sameLine();
                for (int i = 0; i < intArrTag.getPayloadSize(); i++) {
                    ImInt intBuff = new ImInt(intArrTag.getPayload()[i]);
                    if (ImGui.inputInt(tag.getName() + "###int_arr_tag[" + i + "]", intBuff)) {
                        intArrTag.getPayload()[i] = intBuff.intValue();
                    }
                }
            }
            case NbtTypes.TAG_LONG_ARRAY -> {
                NbtLongArrayTag longArrTag = (NbtLongArrayTag) tag;
                ImGui.text("[i64]");
                ImGui.sameLine();
                for (int i = 0; i < longArrTag.getPayloadSize(); i++) {
                    ImLong longBuff = new ImLong(longArrTag.getPayload()[i]);
                    if (ImGui.inputScalar(tag.getName() + "###long_arr_tag[" + i + "]", longBuff)) {
                        longArrTag.getPayload()[i] = longBuff.longValue();
                    }
                }
            }
            case NbtTypes.TAG_COMPOUND -> {
                NbtCompoundTag compTag = (NbtCompoundTag) tag;
                if (ImGui.treeNode(tag.getName() + "###comp_tag")) {
                    ImGui.indent();
                    for (int i = 0; i < compTag.getPayloadSize(); i++) {
                        NbtTag subTag = compTag.getPayload().get(i);
                        ImGui.pushID(i);
                        nbtUi(subTag);
                        ImGui.popID();
                    }
                    ImGui.unindent();
                    ImGui.treePop();
                }
                
            }
            case NbtTypes.TAG_LIST -> {
                NbtListTag listTag = (NbtListTag) tag;
                if (ImGui.treeNode(tag.getName() + "###comp_tag")) {
                    ImGui.indent();
                    for (int i = 0; i < listTag.getPayloadSize(); i++) {
                        NbtTag subTag = listTag.getPayload().get(i);
                        ImGui.pushID(i);
                        nbtUi(subTag);
                        ImGui.popID();
                    }
                    ImGui.unindent();
                    ImGui.treePop();
                }
            }
            case NbtTypes.TAG_END -> {
                //Pass
            }
        }
        
    }
    
}
