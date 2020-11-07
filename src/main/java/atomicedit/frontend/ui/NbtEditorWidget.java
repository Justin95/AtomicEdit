
package atomicedit.frontend.ui;

import atomicedit.backend.nbt.*;
import atomicedit.frontend.ui.atomicedit_legui.DoubleSelectorComponent;
import atomicedit.frontend.ui.atomicedit_legui.IntegerSelectorComponent;
import atomicedit.volumes.WorldVolume;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.Component;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.component.Panel;
import org.liquidengine.legui.component.ScrollablePanel;
import org.liquidengine.legui.component.TextInput;
import org.liquidengine.legui.component.Widget;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.style.Style;
import org.liquidengine.legui.style.flex.FlexStyle;

/**
 *
 * @author Justin Bonner
 */
public class NbtEditorWidget extends Widget {
    
    private static final int GUI_WIDTH = 700;
    private static final int GUI_HEIGHT = 800;
    private static final int SCROLL_PANEL_HEIGHT = GUI_HEIGHT - 100;
    private static final int HEIGHT_PER_TAG = 30;
    private static final int INDENT_PER_TAG = 30;
    private static final int CHAR_WIDTH = 8;
    
    private final List<NbtTagHolder> tagHolders;
    private final List<NbtTag> originalTags;
    private final List<NbtTag> nbtTags;
    private final WorldVolume worldVolume;
    private final ChangedNbtCallback callback;
    private ScrollablePanel scrollPanel;
    
    public NbtEditorWidget(String title, WorldVolume worldVolume, ChangedNbtCallback callback, List<NbtTag> nbtTags) {
        super(title);
        this.worldVolume = worldVolume;
        this.callback = callback;
        this.originalTags = Collections.unmodifiableList(nbtTags);
        this.nbtTags = new ArrayList<>();
        for (NbtTag tag : originalTags) {
            this.nbtTags.add(tag.copy());
        }
        this.tagHolders = new ArrayList<>();
        initialize();
    }
    
    private void initialize() {
        this.setCloseable(true);
        this.setMinimizable(false);
        this.setMinimizable(false);
        this.setDraggable(true);
        //absolute pos in root
        this.getStyle().setPosition(Style.PositionType.ABSOLUTE);
        this.getStyle().setLeft(500);
        this.getStyle().setTop(50);
        //self size
        this.getStyle().setMinWidth(GUI_WIDTH);
        this.getStyle().setMinHeight(GUI_HEIGHT);
        Component container = this.getContainer();
        //Flex layout
        container.getStyle().setDisplay(Style.DisplayType.FLEX);
        container.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
        container.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.FLEX_START);
        container.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.CENTER);
        //padding
        container.getStyle().setPadding(0, 10, 10, 10); //top, right, bottom, left
        
        scrollPanel = new ScrollablePanel();
        this.scrollPanel.getStyle().setPosition(Style.PositionType.RELATIVE);
        //self size
        this.scrollPanel.getStyle().setMinWidth(GUI_WIDTH);
        this.scrollPanel.getStyle().setMinHeight(SCROLL_PANEL_HEIGHT);
        container.add(scrollPanel);
        
        Button button = new Button();
        button.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK) {
                synchronized (this) {
                    if (this.getParent() != null) { //this widget is still on screen
                        this.getParent().remove(this); //close this widget
                        for (NbtTagHolder tagHolder : this.tagHolders) {
                            tagHolder.updateHeldNbt();
                        }
                        this.callback.changedNbt(worldVolume, originalTags, nbtTags);
                    }
                }
            }
        });
        button.getTextState().setText("Confirm");
        button.getStyle().setPosition(Style.PositionType.RELATIVE);
        button.getStyle().setMinHeight(30f);
        button.getStyle().setMinWidth(100f);
        button.getStyle().setMargin(20);
        container.add(button);
        
        updateGui();
    }
    
    private void updateGui() {
        //System.out.println("Creating nbt gui for " + this.nbtTags.size() + " tags.");
        Component contentPanel = this.scrollPanel.getContainer();
        contentPanel.getStyle().getBackground().setColor(.7f, .7f, .7f, 1f);
        //Flex layout
        contentPanel.getStyle().setDisplay(Style.DisplayType.FLEX);
        contentPanel.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
        contentPanel.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.FLEX_START);
        contentPanel.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.FLEX_START);
        //padding
        contentPanel.getStyle().setPadding(10, 10, 10, 10); //top, right, bottom, left
        
        int rawHeight = 0;
        int rawWidth = 0;
        boolean darken = true;
        for (NbtTag nbtTag : this.nbtTags) {
            Component subComp = calcNbtComp(nbtTag);
            float color = darken ? .6f : .7f;
            darken = !darken;
            subComp.getStyle().getBackground().setColor(color, color, color, 1f);
            this.tagHolders.add((NbtTagHolder)subComp);
            subComp.getStyle().setPosition(Style.PositionType.RELATIVE);
            subComp.getStyle().setMinWidth(subComp.getSize().x);
            subComp.getStyle().setMinHeight(subComp.getSize().y);
            rawHeight += subComp.getSize().y;
            rawWidth = subComp.getSize().x > rawWidth ? (int)subComp.getSize().x : rawWidth;
            contentPanel.add(subComp);
        }
        
        //set size so scroll bars work
        float height = Math.max(SCROLL_PANEL_HEIGHT - this.scrollPanel.getHorizontalScrollBar().getSize().y, rawHeight + 20);
        float width = Math.max(GUI_WIDTH - this.scrollPanel.getVerticalScrollBar().getSize().x, rawWidth + 20);
        contentPanel.setSize(width, height);
    }
    
    private static Component calcNbtComp(NbtTag nbtTag) {
        switch (nbtTag.getType()) {
            case TAG_BYTE:
                return new ByteComponent((NbtByteTag)nbtTag);
            case TAG_SHORT:
                return new ShortComponent((NbtShortTag)nbtTag);
            case TAG_INT:
                return new IntComponent((NbtIntTag)nbtTag);
            case TAG_LONG:
                return new LongComponent((NbtLongTag)nbtTag);
            case TAG_FLOAT:
                return new FloatComponent((NbtFloatTag)nbtTag);
            case TAG_DOUBLE:
                return new DoubleComponent((NbtDoubleTag)nbtTag);
            case TAG_STRING:
                return new StringComponent((NbtStringTag)nbtTag);
            case TAG_LIST:
                return new ListComponent((NbtListTag)nbtTag);
            case TAG_COMPOUND:
                return new CompoundComponent((NbtCompoundTag)nbtTag);
            case TAG_BYTE_ARRAY:
                return new ByteArrayComponent((NbtByteArrayTag)nbtTag);
            case TAG_INT_ARRAY:
                return new IntArrayComponent((NbtIntArrayTag)nbtTag);
            case TAG_LONG_ARRAY:
                return new LongArrayComponent((NbtLongArrayTag)nbtTag);
            default:
                throw new IllegalArgumentException("Bad NBT Type: " + nbtTag.getType());
        }
    }
    
    public static interface ChangedNbtCallback {
        void changedNbt(WorldVolume worldVolume, List<NbtTag> originalTags, List<NbtTag> newTags);
    }
    
    private static interface NbtTagHolder {
        void updateHeldNbt();
    }
    
    private static final int MARGIN = 3;
    
    private static void styleNbtPanel(Panel nbtPanel) {
        //Flex layout
        nbtPanel.getStyle().setPosition(Style.PositionType.RELATIVE);
        nbtPanel.getStyle().setDisplay(Style.DisplayType.FLEX);
        nbtPanel.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.ROW);
        nbtPanel.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.FLEX_START);
        nbtPanel.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.FLEX_START);
        nbtPanel.getStyle().getFlexStyle().setFlexGrow(1);
        nbtPanel.getStyle().getFlexStyle().setFlexShrink(1);
        nbtPanel.getStyle().getBackground().setColor(.7f, .7f, .7f, 0f);
        nbtPanel.getStyle().setBorder(null);
        nbtPanel.getStyle().setShadow(null);
        
        
        //nbtLabel.getStyle().setMinWidth(150);
    }
    
    private static Label createLabel(NbtTypes nbtType, String tagName) {
        Label nbtLabel = new Label(nbtType + ":" + tagName);
        nbtLabel.getStyle().setPosition(Style.PositionType.RELATIVE);
        nbtLabel.setSize(nbtLabel.getTextState().length() * CHAR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
        nbtLabel.getStyle().setMinHeight(HEIGHT_PER_TAG - 2 * MARGIN);
        nbtLabel.getStyle().setMinWidth(nbtLabel.getSize().x);
        nbtLabel.getStyle().setMargin(MARGIN);
        return nbtLabel;
    }
    
    private static void styleSelectorComp(Component selectorComp) {
        selectorComp.getStyle().setPosition(Style.PositionType.RELATIVE);
        selectorComp.getStyle().setMinWidth(selectorComp.getSize().x);
        selectorComp.getStyle().setMinHeight(selectorComp.getSize().y);
        selectorComp.getStyle().setMargin(MARGIN);
        selectorComp.getStyle().getBackground().setColor(.9f, .9f, .9f, 1f);
        selectorComp.getFocusedStyle().getBackground().setColor(.9f, .9f, .9f, 1f);
        selectorComp.getStyle().setBorder(null);
        selectorComp.getStyle().setShadow(null);
    }
    
    private static class ByteComponent extends Panel implements NbtTagHolder {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 5;
        private final NbtByteTag nbtTag;
        private Label label;
        private IntegerSelectorComponent selectorComp;
        
        ByteComponent(NbtByteTag nbtTag) {
            this.nbtTag = nbtTag;
            initialize();
        }
        
        private void initialize() {
            styleNbtPanel(this);
            this.label = createLabel(nbtTag.getType(), nbtTag.getName());
            
            this.selectorComp = new IntegerSelectorComponent(Byte.MIN_VALUE, Byte.MAX_VALUE, nbtTag.getPayload());
            this.selectorComp.setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
            styleSelectorComp(this.selectorComp); //after setting size
            
            this.add(label);
            this.add(selectorComp);
            
            //set size so scroll bars work
            float height = HEIGHT_PER_TAG;
            float width = this.label.getSize().x + this.selectorComp.getSize().x + 4 * MARGIN;
            this.setSize(width, height);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setMinWidth(this.getSize().x);
            this.getStyle().setMinHeight(this.getSize().y);
        }
        
        @Override
        public void updateHeldNbt() {
            nbtTag.setPayload((byte)selectorComp.getValue());
        }
        
    }
    
    private static class ShortComponent extends Panel implements NbtTagHolder {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 8;
        private final NbtShortTag nbtTag;
        private Label label;
        private IntegerSelectorComponent selectorComp;
        
        ShortComponent(NbtShortTag nbtTag) {
            this.nbtTag = nbtTag;
            initialize();
        }
        
        private void initialize() {
            styleNbtPanel(this);
            this.label = createLabel(nbtTag.getType(), nbtTag.getName());
            
            this.selectorComp = new IntegerSelectorComponent(Short.MIN_VALUE, Short.MAX_VALUE, nbtTag.getPayload());
            this.selectorComp.setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
            styleSelectorComp(this.selectorComp); //after setting size
            
            this.add(label);
            this.add(selectorComp);
            
            //set size so scroll bars work
            float height = HEIGHT_PER_TAG;
            float width = this.label.getSize().x + this.selectorComp.getSize().x + 4 * MARGIN;
            this.setSize(width, height);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setMinWidth(this.getSize().x);
            this.getStyle().setMinHeight(this.getSize().y);
        }
        
        @Override
        public void updateHeldNbt() {
            nbtTag.setPayload((short)selectorComp.getValue());
        }
        
    }
    
    private static class IntComponent extends Panel implements NbtTagHolder {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 15;
        private final NbtIntTag nbtTag;
        private Label label;
        private IntegerSelectorComponent selectorComp;
        
        IntComponent(NbtIntTag nbtTag) {
            this.nbtTag = nbtTag;
            initialize();
        }
        
        private void initialize() {
            styleNbtPanel(this);
            this.label = createLabel(nbtTag.getType(), nbtTag.getName());
            
            this.selectorComp = new IntegerSelectorComponent(Integer.MIN_VALUE, Integer.MAX_VALUE, nbtTag.getPayload());
            this.selectorComp.setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
            styleSelectorComp(this.selectorComp); //after setting size
            
            this.add(label);
            this.add(selectorComp);
            
            //set size so scroll bars work
            float height = HEIGHT_PER_TAG;
            float width = this.label.getSize().x + this.selectorComp.getSize().x + 4 * MARGIN;
            this.setSize(width, height);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setMinWidth(this.getSize().x);
            this.getStyle().setMinHeight(this.getSize().y);
        }
        
        @Override
        public void updateHeldNbt() {
            nbtTag.setPayload((int)selectorComp.getValue());
        }
        
    }
    
    private static class LongComponent extends Panel implements NbtTagHolder {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 30;
        private final NbtLongTag nbtTag;
        private Label label;
        private IntegerSelectorComponent selectorComp;
        
        LongComponent(NbtLongTag nbtTag) {
            this.nbtTag = nbtTag;
            initialize();
        }
        
        private void initialize() {
            styleNbtPanel(this);
            this.label = createLabel(nbtTag.getType(), nbtTag.getName());
            
            this.selectorComp = new IntegerSelectorComponent(Long.MIN_VALUE, Long.MAX_VALUE, nbtTag.getPayload());
            this.selectorComp.setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
            styleSelectorComp(this.selectorComp); //after setting size
            
            this.add(label);
            this.add(selectorComp);
            
            //set size so scroll bars work
            float height = HEIGHT_PER_TAG;
            float width = this.label.getSize().x + this.selectorComp.getSize().x + 4 * MARGIN;
            this.setSize(width, height);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setMinWidth(this.getSize().x);
            this.getStyle().setMinHeight(this.getSize().y);
        }
        
        @Override
        public void updateHeldNbt() {
            nbtTag.setPayload(selectorComp.getValue());
        }
        
    }
    
    private static class FloatComponent extends Panel implements NbtTagHolder {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 20;
        private final NbtFloatTag nbtTag;
        private Label label;
        private DoubleSelectorComponent selectorComp;
        
        FloatComponent(NbtFloatTag nbtTag) {
            this.nbtTag = nbtTag;
            initialize();
        }
        
        private void initialize() {
            styleNbtPanel(this);
            this.label = createLabel(nbtTag.getType(), nbtTag.getName());
            
            this.selectorComp = new DoubleSelectorComponent(Float.MIN_VALUE, Float.MAX_VALUE, nbtTag.getPayload());
            this.selectorComp.setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
            styleSelectorComp(this.selectorComp); //after setting size
            
            this.add(label);
            this.add(selectorComp);
            
            //set size so scroll bars work
            float height = HEIGHT_PER_TAG;
            float width = this.label.getSize().x + this.selectorComp.getSize().x + 4 * MARGIN;
            this.setSize(width, height);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setMinWidth(this.getSize().x);
            this.getStyle().setMinHeight(this.getSize().y);
        }
        
        @Override
        public void updateHeldNbt() {
            nbtTag.setPayload((float)selectorComp.getValue());
        }
        
    }
    
    private static class DoubleComponent extends Panel implements NbtTagHolder {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 35;
        private final NbtDoubleTag nbtTag;
        private Label label;
        private DoubleSelectorComponent selectorComp;
        
        DoubleComponent(NbtDoubleTag nbtTag) {
            this.nbtTag = nbtTag;
            initialize();
        }
        
        private void initialize() {
            styleNbtPanel(this);
            this.label = createLabel(nbtTag.getType(), nbtTag.getName());
            
            this.selectorComp = new DoubleSelectorComponent(Double.MIN_VALUE, Double.MAX_VALUE, nbtTag.getPayload());
            this.selectorComp.setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
            styleSelectorComp(this.selectorComp); //after setting size
            
            this.add(label);
            this.add(selectorComp);
            
            //set size so scroll bars work
            float height = HEIGHT_PER_TAG;
            float width = this.label.getSize().x + this.selectorComp.getSize().x + 4 * MARGIN;
            this.setSize(width, height);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setMinWidth(this.getSize().x);
            this.getStyle().setMinHeight(this.getSize().y);
        }
        
        @Override
        public void updateHeldNbt() {
            nbtTag.setPayload(selectorComp.getValue());
        }
        
    }
    
    private static class StringComponent extends Panel implements NbtTagHolder {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 40;
        private final NbtStringTag nbtTag;
        private Label label;
        private TextInput selectorComp;
        
        StringComponent(NbtStringTag nbtTag) {
            this.nbtTag = nbtTag;
            initialize();
        }
        
        private void initialize() {
            styleNbtPanel(this);
            this.label = createLabel(nbtTag.getType(), nbtTag.getName());
            
            this.selectorComp = new TextInput(nbtTag.getPayload());
            this.selectorComp.setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
            styleSelectorComp(this.selectorComp); //after setting size
            
            this.add(label);
            this.add(selectorComp);
            
            //set size so scroll bars work
            float height = HEIGHT_PER_TAG;
            float width = this.label.getSize().x + this.selectorComp.getSize().x + 4 * MARGIN;
            this.setSize(width, height);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setMinWidth(this.getSize().x);
            this.getStyle().setMinHeight(this.getSize().y);
        }
        
        @Override
        public void updateHeldNbt() {
            nbtTag.setPayload(selectorComp.getTextState().getText());
        }
        
    }
    
    private static class ByteArrayComponent extends Panel implements NbtTagHolder {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 10;
        private final NbtByteArrayTag nbtTag;
        private Label label;
        private IntegerSelectorComponent[] selectorComps;
        
        ByteArrayComponent(NbtByteArrayTag nbtTag) {
            this.nbtTag = nbtTag;
            initialize();
        }
        
        private void initialize() {
            styleNbtPanel(this);
            this.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
            this.label = createLabel(nbtTag.getType(), nbtTag.getName());
            this.add(label);
            
            this.selectorComps = new IntegerSelectorComponent[nbtTag.getPayloadSize()];
            for (int i = 0; i < nbtTag.getPayloadSize(); i++) {
                this.selectorComps[i] = new IntegerSelectorComponent(Byte.MIN_VALUE, Byte.MAX_VALUE, nbtTag.getPayload()[i]);
                this.selectorComps[i].setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
                styleSelectorComp(this.selectorComps[i]); //after setting size
                this.add(selectorComps[i]);
            }
            
            //set size so scroll bars work
            float height = HEIGHT_PER_TAG * selectorComps.length + HEIGHT_PER_TAG;
            float width = this.label.getSize().x + 2 * MARGIN + (this.selectorComps.length * (SELECTOR_WIDTH + 2 * MARGIN));
            this.setSize(width, height);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setMinWidth(this.getSize().x);
            this.getStyle().setMinHeight(this.getSize().y);
        }
        
        @Override
        public void updateHeldNbt() {
            for (int i = 0; i < nbtTag.getPayloadSize(); i++) {
                nbtTag.getPayload()[i] = (byte)selectorComps[i].getValue();
            }
        }
        
    }
    
    private static class IntArrayComponent extends Panel implements NbtTagHolder {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 25;
        private final NbtIntArrayTag nbtTag;
        private Label label;
        private IntegerSelectorComponent[] selectorComps;
        
        IntArrayComponent(NbtIntArrayTag nbtTag) {
            this.nbtTag = nbtTag;
            initialize();
        }
        
        private void initialize() {
            styleNbtPanel(this);
            this.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
            this.label = createLabel(nbtTag.getType(), nbtTag.getName());
            this.add(label);
            
            this.selectorComps = new IntegerSelectorComponent[nbtTag.getPayloadSize()];
            for (int i = 0; i < nbtTag.getPayloadSize(); i++) {
                this.selectorComps[i] = new IntegerSelectorComponent(Integer.MIN_VALUE, Integer.MAX_VALUE, nbtTag.getPayload()[i]);
                this.selectorComps[i].setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
                styleSelectorComp(this.selectorComps[i]); //after setting size
                this.add(selectorComps[i]);
            }
            
            //set size so scroll bars work
            float height = HEIGHT_PER_TAG * selectorComps.length + HEIGHT_PER_TAG;
            float width = this.label.getSize().x + 2 * MARGIN + (this.selectorComps.length * (SELECTOR_WIDTH + 2 * MARGIN));
            this.setSize(width, height);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setMinWidth(this.getSize().x);
            this.getStyle().setMinHeight(this.getSize().y);
        }
        
        @Override
        public void updateHeldNbt() {
            for (int i = 0; i < nbtTag.getPayloadSize(); i++) {
                nbtTag.getPayload()[i] = (int)selectorComps[i].getValue();
            }
        }
        
    }
    
    private static class LongArrayComponent extends Panel implements NbtTagHolder {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 50;
        private final NbtLongArrayTag nbtTag;
        private Label label;
        private IntegerSelectorComponent[] selectorComps;
        
        LongArrayComponent(NbtLongArrayTag nbtTag) {
            this.nbtTag = nbtTag;
            initialize();
        }
        
        private void initialize() {
            styleNbtPanel(this);
            this.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
            this.label = createLabel(nbtTag.getType(), nbtTag.getName());
            this.add(label);
            
            this.selectorComps = new IntegerSelectorComponent[nbtTag.getPayloadSize()];
            for (int i = 0; i < nbtTag.getPayloadSize(); i++) {
                this.selectorComps[i] = new IntegerSelectorComponent(Long.MIN_VALUE, Long.MAX_VALUE, nbtTag.getPayload()[i]);
                this.selectorComps[i].setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
                styleSelectorComp(this.selectorComps[i]); //after setting size
                this.add(selectorComps[i]);
            }
            
            //set size so scroll bars work
            float height = HEIGHT_PER_TAG * selectorComps.length + HEIGHT_PER_TAG;
            float width = this.label.getSize().x + 2 * MARGIN + (this.selectorComps.length * (SELECTOR_WIDTH + 2 * MARGIN));
            this.setSize(width, height);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setMinWidth(this.getSize().x);
            this.getStyle().setMinHeight(this.getSize().y);
        }
        
        @Override
        public void updateHeldNbt() {
            for (int i = 0; i < nbtTag.getPayloadSize(); i++) {
                nbtTag.getPayload()[i] = selectorComps[i].getValue();
            }
        }
        
    }
    
    private static class CompoundComponent extends Panel implements NbtTagHolder {
        
        private final NbtCompoundTag nbtTag;
        private Label label;
        private final List<NbtTagHolder> children;
        private final List<Component> selectorComps;
        
        CompoundComponent(NbtCompoundTag nbtTag) {
            this.nbtTag = nbtTag;
            this.children = new ArrayList<>();
            this.selectorComps = new ArrayList<>();
            initialize();
        }
        
        private void initialize() {
            styleNbtPanel(this);
            this.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
            this.label = createLabel(nbtTag.getType(), nbtTag.getName());
            this.add(label);
            
            float totalHeight = label.getSize().y + 2 * MARGIN;
            float maxWidth = this.label.getSize().x;
            for (NbtTag subNbtTag : nbtTag.getPayload()) {
                Component subComp = calcNbtComp(subNbtTag);
                subComp.getStyle().setMarginLeft((float)INDENT_PER_TAG);
                this.children.add((NbtTagHolder)subComp);
                this.selectorComps.add(subComp);
                totalHeight += subComp.getSize().y + 2 * MARGIN;
                float newWidth = subComp.getSize().x + 2 * MARGIN + INDENT_PER_TAG;
                maxWidth = maxWidth > newWidth ? maxWidth : newWidth;
                this.add(subComp);
            }
            
            //set size so scroll bars work
            float height = totalHeight;
            float width = maxWidth;
            this.setSize(width, height);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setMinWidth(this.getSize().x);
            this.getStyle().setMinHeight(this.getSize().y);
        }
        
        @Override
        public void updateHeldNbt() {
            for (NbtTagHolder child : children) {
                child.updateHeldNbt();
            }
        }
        
    }
    
    private static class ListComponent extends Panel implements NbtTagHolder {
        
        private final NbtListTag nbtTag;
        private Label label;
        private final List<NbtTagHolder> children;
        private final List<Component> selectorComps;
        
        ListComponent(NbtListTag nbtTag) {
            this.nbtTag = nbtTag;
            this.children = new ArrayList<>();
            this.selectorComps = new ArrayList<>();
            initialize();
        }
        
        private void initialize() {
            styleNbtPanel(this);
            this.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
            this.label = createLabel(nbtTag.getType(), nbtTag.getName());
            this.add(label);
            
            float totalHeight = label.getSize().y + 2 * MARGIN;
            float maxWidth = this.label.getSize().x;
            for (NbtTag subNbtTag : nbtTag.getPayload()) {
                Component subComp = calcNbtComp(subNbtTag);
                subComp.getStyle().setMarginLeft((float)INDENT_PER_TAG);
                this.children.add((NbtTagHolder)subComp);
                this.selectorComps.add(subComp);
                totalHeight += subComp.getSize().y + 2 * MARGIN;
                float newWidth = subComp.getSize().x + 2 * MARGIN + INDENT_PER_TAG;
                maxWidth = maxWidth > newWidth ? maxWidth : newWidth;
                this.add(subComp);
            }
            
            //set size so scroll bars work
            float height = totalHeight;
            float width = maxWidth;
            this.setSize(width, height);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setMinWidth(this.getSize().x);
            this.getStyle().setMinHeight(this.getSize().y);
        }
        
        @Override
        public void updateHeldNbt() {
            for (NbtTagHolder child : children) {
                child.updateHeldNbt();
            }
        }
        
    }
    
}
