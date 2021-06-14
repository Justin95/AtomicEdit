
package atomicedit.frontend.ui;

import atomicedit.backend.nbt.*;
import atomicedit.frontend.ui.atomicedit_legui.DoubleSelectorComponent;
import atomicedit.frontend.ui.atomicedit_legui.IntegerSelectorComponent;
import atomicedit.utils.FileUtils;
import atomicedit.volumes.WorldVolume;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.Component;
import org.liquidengine.legui.component.ImageView;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.component.Panel;
import org.liquidengine.legui.component.ScrollablePanel;
import org.liquidengine.legui.component.SelectBox;
import org.liquidengine.legui.component.TextInput;
import org.liquidengine.legui.component.Tooltip;
import org.liquidengine.legui.component.Widget;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.icon.ImageIcon;
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
    private static final int INDENT_PER_TAG = 40;
    private static final int CHAR_WIDTH = 8;
    private static final int ICON_SIZE = 25;
    
    private static final Vector4f LIGHT_BACKGROUND_COLOR = new Vector4f(.6f, .6f, .6f, 1f);
    private static final Vector4f DARK_BACKGROUND_COLOR = new Vector4f(.6f, .6f, .6f, 1f);
    
    private static final ImageIcon ADD_ICON = FileUtils.loadIcon("icons/green_plus_icon.png");
    private static final ImageIcon REMOVE_ICON = FileUtils.loadIcon("icons/red_x_icon.png");
    //nbt icons
    private static final ImageIcon BYTE_ARRAY_ICON = FileUtils.loadIcon("icons/nbt/byte_array_tag_icon.png");
    private static final ImageIcon BYTE_ICON = FileUtils.loadIcon("icons/nbt/byte_tag_icon.png");
    private static final ImageIcon COMPOUND_ICON = FileUtils.loadIcon("icons/nbt/compound_tag_icon.png");
    private static final ImageIcon DOUBLE_ICON = FileUtils.loadIcon("icons/nbt/double_tag_icon.png");
    private static final ImageIcon FLOAT_ICON = FileUtils.loadIcon("icons/nbt/float_tag_icon.png");
    private static final ImageIcon INT_ARRAY_ICON = FileUtils.loadIcon("icons/nbt/integer_array_tag_icon.png");
    private static final ImageIcon INT_ICON = FileUtils.loadIcon("icons/nbt/integer_tag_icon.png");
    private static final ImageIcon LIST_ICON = FileUtils.loadIcon("icons/nbt/list_tag_icon.png");
    private static final ImageIcon LONG_ARRAY_ICON = FileUtils.loadIcon("icons/nbt/long_array_tag_icon.png");
    private static final ImageIcon LONG_ICON = FileUtils.loadIcon("icons/nbt/long_tag_icon.png");
    private static final ImageIcon SHORT_ICON = FileUtils.loadIcon("icons/nbt/short_tag_icon.png");
    private static final ImageIcon STRING_ICON = FileUtils.loadIcon("icons/nbt/string_tag_icon.png");
    
    static {
        ADD_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
        REMOVE_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
        //nbt tags
        BYTE_ARRAY_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
        BYTE_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
        COMPOUND_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
        DOUBLE_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
        FLOAT_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
        INT_ARRAY_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
        INT_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
        LIST_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
        LONG_ARRAY_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
        LONG_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
        SHORT_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
        STRING_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
    }
    
    private final List<NbtTag> nbtTags;
    private final List<NbtComponent> nbtComponents;
    private final WorldVolume worldVolume;
    private final ChangedNbtCallback callback;
    private ScrollablePanel scrollPanel;
    
    public NbtEditorWidget(String title, WorldVolume worldVolume, ChangedNbtCallback callback, List<NbtTag> nbtTags) {
        super(title);
        this.worldVolume = worldVolume;
        this.callback = callback;
        this.nbtComponents = new ArrayList<>();
        this.nbtTags = nbtTags;
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
                        for (NbtTagHolder tagHolder : this.nbtComponents) {
                            tagHolder.updateHeldNbt();
                        }
                        this.callback.changedNbt(worldVolume, nbtTags);
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
    
    private void removeNbtTag(NbtTag tag) {
        this.nbtTags.remove(tag);
        this.updateGui();
    }
    
    private void updateGui() {
        Component contentPanel = this.scrollPanel.getContainer();
        contentPanel.getStyle().getBackground().setColor(.7f, .7f, .7f, 1f);
        //Flex layout
        contentPanel.getStyle().setDisplay(Style.DisplayType.FLEX);
        contentPanel.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
        contentPanel.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.FLEX_START);
        contentPanel.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.FLEX_START);
        //padding
        contentPanel.getStyle().setPadding(10, 10, 10, 10); //top, right, bottom, left
        
        //clear any existing components
        for (NbtComponent comp : this.nbtComponents) {
            contentPanel.remove(comp);
        }
        this.nbtComponents.clear();
        
        boolean darken = true;
        for (NbtTag nbtTag : this.nbtTags) {
            NbtComponent subComp = calcNbtComp(nbtTag, this, this::removeNbtTag);
            Vector4f color = darken ? DARK_BACKGROUND_COLOR : LIGHT_BACKGROUND_COLOR;
            darken = !darken;
            subComp.getStyle().getBackground().setColor(color);
            subComp.getStyle().setPosition(Style.PositionType.RELATIVE);
            subComp.getStyle().setMinWidth(subComp.getSize().x);
            subComp.getStyle().setMinHeight(subComp.getSize().y);
            this.nbtComponents.add(subComp);
            contentPanel.add(subComp);
        }
        
        recalcSizes();
    }
    
    private void recalcSizes() {
        int rawHeight = 0;
        int rawWidth = 0;
        for (NbtComponent nbtComp : this.nbtComponents) {
            nbtComp.updateSize();
            rawHeight += nbtComp.getSize().y;
            rawWidth = nbtComp.getSize().x > rawWidth ? (int)nbtComp.getSize().x : rawWidth;
        }
        float height = Math.max(SCROLL_PANEL_HEIGHT - this.scrollPanel.getHorizontalScrollBar().getSize().y, rawHeight + 20);
        float width = Math.max(GUI_WIDTH - this.scrollPanel.getVerticalScrollBar().getSize().x, rawWidth + 20);
        this.scrollPanel.getContainer().setSize(width, height);
    }
    
    private static NbtComponent calcNbtComp(NbtTag nbtTag, NbtEditorWidget nbtWidget, NbtTagParent parent) {
        switch (nbtTag.getType()) {
            case TAG_BYTE:
                return new ByteComponent((NbtByteTag)nbtTag, nbtWidget, parent);
            case TAG_SHORT:
                return new ShortComponent((NbtShortTag)nbtTag, nbtWidget, parent);
            case TAG_INT:
                return new IntComponent((NbtIntTag)nbtTag, nbtWidget, parent);
            case TAG_LONG:
                return new LongComponent((NbtLongTag)nbtTag, nbtWidget, parent);
            case TAG_FLOAT:
                return new FloatComponent((NbtFloatTag)nbtTag, nbtWidget, parent);
            case TAG_DOUBLE:
                return new DoubleComponent((NbtDoubleTag)nbtTag, nbtWidget, parent);
            case TAG_STRING:
                return new StringComponent((NbtStringTag)nbtTag, nbtWidget, parent);
            case TAG_LIST:
                return new ListComponent((NbtListTag)nbtTag, nbtWidget, parent);
            case TAG_COMPOUND:
                return new CompoundComponent((NbtCompoundTag)nbtTag, nbtWidget, parent);
            case TAG_BYTE_ARRAY:
                return new ByteArrayComponent((NbtByteArrayTag)nbtTag, nbtWidget, parent);
            case TAG_INT_ARRAY:
                return new IntArrayComponent((NbtIntArrayTag)nbtTag, nbtWidget, parent);
            case TAG_LONG_ARRAY:
                return new LongArrayComponent((NbtLongArrayTag)nbtTag, nbtWidget, parent);
            default:
                throw new IllegalArgumentException("Bad NBT Type: " + nbtTag.getType());
        }
    }
    
    public static interface ChangedNbtCallback {
        void changedNbt(WorldVolume worldVolume, List<NbtTag> newTags);
    }
    
    private static interface NbtTagHolder {
        void updateHeldNbt();
    }
    
    private static interface NbtTagParent {
        void removeTag(NbtTag tag);
    }
    
    private static final int MARGIN = 3;
    
    private static void styleNbtPanel(Panel nbtPanel) {
        //Flex layout
        nbtPanel.getStyle().setPosition(Style.PositionType.RELATIVE);
        nbtPanel.getStyle().setDisplay(Style.DisplayType.FLEX);
        nbtPanel.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.ROW);
        nbtPanel.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.FLEX_START);
        nbtPanel.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.CENTER);
        nbtPanel.getStyle().getBackground().setColor(.7f, .7f, .7f, 0f);
        nbtPanel.getStyle().setBorder(null);
        nbtPanel.getStyle().setShadow(null);
        
        
        //nbtLabel.getStyle().setMinWidth(150);
    }
    
    private static Label createLabel(String tagName) {
        Label nbtLabel = new Label(tagName);
        nbtLabel.getStyle().setPosition(Style.PositionType.RELATIVE);
        nbtLabel.setSize(nbtLabel.getTextState().length() * CHAR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
        nbtLabel.getStyle().setMinHeight(HEIGHT_PER_TAG - 2 * MARGIN);
        nbtLabel.getStyle().setMinWidth(nbtLabel.getSize().x);
        nbtLabel.getStyle().setMarginRight((float)MARGIN);
        return nbtLabel;
    }
    
    private static Button createRemoveButton(NbtTagParent parent, NbtTag tag) {
        Button button = new Button("");
        button.getStyle().getBackground().setIcon(REMOVE_ICON);
        button.getStyle().setPosition(Style.PositionType.RELATIVE);
        button.getStyle().setMinimumSize(ICON_SIZE, ICON_SIZE);
        button.setSize(ICON_SIZE, ICON_SIZE);
        button.getStyle().setMargin(MARGIN);
        button.getStyle().setBorder(null);
        button.getStyle().setShadow(null);
        button.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK) {
                parent.removeTag(tag);
            }
        });
        return button;
    }
    
    private static Panel createLabelPanel(NbtTagParent parent, NbtTag tag) {
        NbtTypes nbtType = tag.getType();
        String tagName = tag.getName();
        ImageView typeIcon = createNbtImageComp(nbtType);
        Label label = createLabel(tagName);
        Button removeButton = createRemoveButton(parent, tag);
        Panel labelPanel = new Panel();
        labelPanel.setSize(
            MARGIN * 4 + typeIcon.getSize().x + label.getSize().x + removeButton.getSize().x,
            MARGIN * 2 + Math.max(typeIcon.getSize().y, label.getSize().y) //icons are all the same size so dont need to check both
        );
        labelPanel.getStyle().getFlexStyle().setAlignContent(FlexStyle.AlignContent.FLEX_START);
        labelPanel.getStyle().setDisplay(Style.DisplayType.FLEX);
        labelPanel.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.ROW);
        labelPanel.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.FLEX_START);
        labelPanel.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.CENTER);
        labelPanel.getStyle().setBorder(null);
        labelPanel.getStyle().setShadow(null);
        labelPanel.getStyle().setPosition(Style.PositionType.RELATIVE);
        labelPanel.getStyle().setMinimumSize(labelPanel.getSize().x, labelPanel.getSize().y);
        labelPanel.getStyle().getBackground().setColor(1f, 1f, 1f, 0f);
        labelPanel.getStyle().setMargin(MARGIN);
        
        labelPanel.add(typeIcon);
        labelPanel.add(label);
        labelPanel.add(removeButton);
        return labelPanel;
    }
    
    private static ImageView createNbtImageComp(NbtTypes nbtType) {
        ImageView imageView = new ImageView();
        ImageIcon icon;
        switch (nbtType) {
            case TAG_BYTE:
                icon = BYTE_ICON;
                break;
            case TAG_SHORT:
                icon = SHORT_ICON;
                break;
            case TAG_INT:
                icon = INT_ICON;
                break;
            case TAG_LONG:
                icon = LONG_ICON;
                break;
            case TAG_FLOAT:
                icon = FLOAT_ICON;
                break;
            case TAG_DOUBLE:
                icon = DOUBLE_ICON;
                break;
            case TAG_STRING:
                icon = STRING_ICON;
                break;
            case TAG_LIST:
                icon = LIST_ICON;
                break;
            case TAG_COMPOUND:
                icon = COMPOUND_ICON;
                break;
            case TAG_BYTE_ARRAY:
                icon = BYTE_ARRAY_ICON;
                break;
            case TAG_INT_ARRAY:
                icon = INT_ARRAY_ICON;
                break;
            case TAG_LONG_ARRAY:
                icon = LONG_ARRAY_ICON;
                break;
            default:
                throw new IllegalArgumentException("Bad NBT Type: " + nbtType);
        }
        imageView.setImage(icon.getImage());
        Tooltip tooltip = new Tooltip();
        tooltip.getTextState().setText(nbtType.name);
        tooltip.setSize(nbtType.name.length() * CHAR_WIDTH, 20);
        tooltip.getStyle().getBackground().setColor(.8f, .8f, .25f, .9f);
        tooltip.getStyle().setTextColor(0f, 0f, 0f, 1f);
        imageView.setTooltip(tooltip);
        imageView.setSize(ICON_SIZE, ICON_SIZE);
        imageView.getStyle().setMinimumSize(ICON_SIZE, ICON_SIZE);
        imageView.getStyle().setPosition(Style.PositionType.RELATIVE);
        imageView.getStyle().setMarginRight((float)MARGIN);
        imageView.getStyle().setBorder(null);
        imageView.getStyle().setShadow(null);
        return imageView;
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
    
    private static abstract class NbtComponent extends Panel implements NbtTagHolder {
        
        protected NbtEditorWidget nbtWidget;
        protected NbtTagParent parent;
        protected Panel labelPanel;
        protected NbtTag nbtTag;
        
        NbtComponent(NbtEditorWidget nbtWidget, NbtTag nbtTag, NbtTagParent parent) {
            this.parent = parent;
            this.nbtWidget = nbtWidget;
            this.nbtTag = nbtTag;
            initialize();
        }
        
        private void initialize() {
            styleNbtPanel(this);
            this.labelPanel = createLabelPanel(this.parent, this.nbtTag);
            this.add(labelPanel);
        }
        
        protected void updateSize() {
            //Do nothing by default
        }
        
    }
    
    private static abstract class SingleComponent<T extends Component> extends NbtComponent {
        
        protected T selectorComp;
        
        SingleComponent(NbtEditorWidget nbtWidget, NbtTag nbtTag, NbtTagParent parent) {
            super(nbtWidget, nbtTag, parent);
            initialize();
        }
        
        private void initialize() {
            this.selectorComp = createSelectorComp();
            styleSelectorComp(this.selectorComp); //after setting size
            
            this.add(selectorComp);
            
            //set size so scroll bars work
            float height = HEIGHT_PER_TAG;
            float width = this.labelPanel.getSize().x + this.selectorComp.getSize().x + 3 * MARGIN;
            this.setSize(width, height);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setMinWidth(this.getSize().x);
            this.getStyle().setMinHeight(this.getSize().y);
        }
        
        abstract T createSelectorComp();
        
    }
    
    private static class ByteComponent extends SingleComponent<IntegerSelectorComponent> {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 5;
        
        ByteComponent(NbtByteTag nbtTag, NbtEditorWidget nbtWidget, NbtTagParent parent) {
            super(nbtWidget, nbtTag, parent);
        }
        
        @Override
        IntegerSelectorComponent createSelectorComp() {
            IntegerSelectorComponent comp = new IntegerSelectorComponent(Byte.MIN_VALUE, Byte.MAX_VALUE, ((NbtByteTag)nbtTag).getPayload());
            comp.setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
            return comp;
        }
        
        @Override
        public void updateHeldNbt() {
            ((NbtByteTag)nbtTag).setPayload((byte)selectorComp.getValue());
        }
        
    }
    
    private static class ShortComponent extends SingleComponent<IntegerSelectorComponent> {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 8;
        
        ShortComponent(NbtShortTag nbtTag, NbtEditorWidget nbtWidget, NbtTagParent parent) {
            super(nbtWidget, nbtTag, parent);
        }
        
        @Override
        IntegerSelectorComponent createSelectorComp() {
            IntegerSelectorComponent comp = new IntegerSelectorComponent(Short.MIN_VALUE, Short.MAX_VALUE, ((NbtShortTag)nbtTag).getPayload());
            comp.setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
            return comp;
        }
        
        @Override
        public void updateHeldNbt() {
            ((NbtShortTag)nbtTag).setPayload((short)selectorComp.getValue());
        }
        
    }
    
    private static class IntComponent extends SingleComponent<IntegerSelectorComponent> {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 15;
        
        IntComponent(NbtIntTag nbtTag, NbtEditorWidget nbtWidget, NbtTagParent parent) {
            super(nbtWidget, nbtTag, parent);
        }
        
        @Override
        IntegerSelectorComponent createSelectorComp() {
            IntegerSelectorComponent comp = new IntegerSelectorComponent(Integer.MIN_VALUE, Integer.MAX_VALUE, ((NbtIntTag)nbtTag).getPayload());
            comp.setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
            return comp;
        }
        
        @Override
        public void updateHeldNbt() {
            ((NbtIntTag)nbtTag).setPayload((int)selectorComp.getValue());
        }
        
    }
    
    private static class LongComponent extends SingleComponent<IntegerSelectorComponent> {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 30;
        
        LongComponent(NbtLongTag nbtTag, NbtEditorWidget nbtWidget, NbtTagParent parent) {
            super(nbtWidget, nbtTag, parent);
        }
        
        @Override
        IntegerSelectorComponent createSelectorComp() {
            IntegerSelectorComponent comp = new IntegerSelectorComponent(Long.MIN_VALUE, Long.MAX_VALUE, ((NbtLongTag)nbtTag).getPayload());
            comp.setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
            return comp;
        }
        
        @Override
        public void updateHeldNbt() {
            ((NbtLongTag)nbtTag).setPayload(selectorComp.getValue());
        }
        
    }
    
    private static class FloatComponent extends SingleComponent<DoubleSelectorComponent> {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 20;
        
        FloatComponent(NbtFloatTag nbtTag, NbtEditorWidget nbtWidget, NbtTagParent parent) {
            super(nbtWidget, nbtTag, parent);
        }
        
        @Override
        DoubleSelectorComponent createSelectorComp() {
            DoubleSelectorComponent comp = new DoubleSelectorComponent(-Float.MAX_VALUE, Float.MAX_VALUE, ((NbtFloatTag)nbtTag).getPayload());
            comp.setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
            return comp;
        }
        
        @Override
        public void updateHeldNbt() {
            ((NbtFloatTag)nbtTag).setPayload((float)selectorComp.getValue());
        }
        
    }
    
    private static class DoubleComponent extends SingleComponent<DoubleSelectorComponent> {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 35;
        
        DoubleComponent(NbtDoubleTag nbtTag, NbtEditorWidget nbtWidget, NbtTagParent parent) {
            super(nbtWidget, nbtTag, parent);
        }
        
        @Override
        DoubleSelectorComponent createSelectorComp() {
            DoubleSelectorComponent comp = new DoubleSelectorComponent(-Double.MAX_VALUE, Double.MAX_VALUE, ((NbtDoubleTag)nbtTag).getPayload());
            comp.setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
            return comp;
        }
        
        @Override
        public void updateHeldNbt() {
            ((NbtDoubleTag)nbtTag).setPayload(selectorComp.getValue());
        }
        
    }
    
    private static class StringComponent extends SingleComponent<TextInput> {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 40;
        
        StringComponent(NbtStringTag nbtTag, NbtEditorWidget nbtWidget, NbtTagParent parent) {
            super(nbtWidget, nbtTag, parent);
        }
        
        @Override
        TextInput createSelectorComp() {
            TextInput comp = new TextInput(((NbtStringTag)nbtTag).getPayload());
            comp.setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
            return comp;
        }
        
        @Override
        public void updateHeldNbt() {
            ((NbtStringTag)nbtTag).setPayload(selectorComp.getTextState().getText());
        }
        
    }
    
    private static class ByteArrayComponent extends NbtComponent {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 10;
        private IntegerSelectorComponent[] selectorComps;
        
        ByteArrayComponent(NbtByteArrayTag nbtTag, NbtEditorWidget nbtWidget, NbtTagParent parent) {
            super(nbtWidget, nbtTag, parent);
            initialize();
        }
        
        private void initialize() {
            this.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
            this.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.FLEX_START);
            
            this.selectorComps = new IntegerSelectorComponent[((NbtByteArrayTag)nbtTag).getPayloadSize()];
            for (int i = 0; i < ((NbtByteArrayTag)nbtTag).getPayloadSize(); i++) {
                this.selectorComps[i] = new IntegerSelectorComponent(Byte.MIN_VALUE, Byte.MAX_VALUE, ((NbtByteArrayTag)nbtTag).getPayload()[i]);
                this.selectorComps[i].setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
                styleSelectorComp(this.selectorComps[i]); //after setting size
                this.add(selectorComps[i]);
            }
            
            //set size so scroll bars work
            float height = HEIGHT_PER_TAG * selectorComps.length + HEIGHT_PER_TAG;
            float width = this.labelPanel.getSize().x + 2 * MARGIN + (this.selectorComps.length * (SELECTOR_WIDTH + 2 * MARGIN));
            this.setSize(width, height);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setMinWidth(this.getSize().x);
            this.getStyle().setMinHeight(this.getSize().y);
        }
        
        @Override
        public void updateHeldNbt() {
            for (int i = 0; i < ((NbtByteArrayTag)nbtTag).getPayloadSize(); i++) {
                ((NbtByteArrayTag)nbtTag).getPayload()[i] = (byte)selectorComps[i].getValue();
            }
        }
        
    }
    
    private static class IntArrayComponent extends NbtComponent {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 25;
        private IntegerSelectorComponent[] selectorComps;
        
        IntArrayComponent(NbtIntArrayTag nbtTag, NbtEditorWidget nbtWidget, NbtTagParent parent) {
            super(nbtWidget, nbtTag, parent);
            initialize();
        }
        
        private void initialize() {
            this.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
            this.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.FLEX_START);
            
            this.selectorComps = new IntegerSelectorComponent[((NbtIntArrayTag)nbtTag).getPayloadSize()];
            for (int i = 0; i < ((NbtIntArrayTag)nbtTag).getPayloadSize(); i++) {
                this.selectorComps[i] = new IntegerSelectorComponent(Integer.MIN_VALUE, Integer.MAX_VALUE, ((NbtIntArrayTag)nbtTag).getPayload()[i]);
                this.selectorComps[i].setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
                styleSelectorComp(this.selectorComps[i]); //after setting size
                this.add(selectorComps[i]);
            }
            
            //set size so scroll bars work
            float height = HEIGHT_PER_TAG * selectorComps.length + HEIGHT_PER_TAG;
            float width = this.labelPanel.getSize().x + 2 * MARGIN + (this.selectorComps.length * (SELECTOR_WIDTH + 2 * MARGIN));
            this.setSize(width, height);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setMinWidth(this.getSize().x);
            this.getStyle().setMinHeight(this.getSize().y);
        }
        
        @Override
        public void updateHeldNbt() {
            for (int i = 0; i < ((NbtIntArrayTag)nbtTag).getPayloadSize(); i++) {
                ((NbtIntArrayTag)nbtTag).getPayload()[i] = (int)selectorComps[i].getValue();
            }
        }
        
    }
    
    private static class LongArrayComponent extends NbtComponent {
        
        private static final int SELECTOR_WIDTH = CHAR_WIDTH * 50;
        private IntegerSelectorComponent[] selectorComps;
        
        LongArrayComponent(NbtLongArrayTag nbtTag, NbtEditorWidget nbtWidget, NbtTagParent parent) {
            super(nbtWidget, nbtTag, parent);
            initialize();
        }
        
        private void initialize() {
            this.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
            this.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.FLEX_START);
            
            this.selectorComps = new IntegerSelectorComponent[((NbtLongArrayTag)nbtTag).getPayloadSize()];
            for (int i = 0; i < ((NbtLongArrayTag)nbtTag).getPayloadSize(); i++) {
                this.selectorComps[i] = new IntegerSelectorComponent(Long.MIN_VALUE, Long.MAX_VALUE, ((NbtLongArrayTag)nbtTag).getPayload()[i]);
                this.selectorComps[i].setSize(SELECTOR_WIDTH, HEIGHT_PER_TAG - 2 * MARGIN);
                styleSelectorComp(this.selectorComps[i]); //after setting size
                this.add(selectorComps[i]);
            }
            
            //set size so scroll bars work
            float height = HEIGHT_PER_TAG * selectorComps.length + HEIGHT_PER_TAG;
            float width = this.labelPanel.getSize().x + 2 * MARGIN + (this.selectorComps.length * (SELECTOR_WIDTH + 2 * MARGIN));
            this.setSize(width, height);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setMinWidth(this.getSize().x);
            this.getStyle().setMinHeight(this.getSize().y);
        }
        
        @Override
        public void updateHeldNbt() {
            for (int i = 0; i < ((NbtLongArrayTag)nbtTag).getPayloadSize(); i++) {
                ((NbtLongArrayTag)nbtTag).getPayload()[i] = selectorComps[i].getValue();
            }
        }
        
    }
    
    private static class CompoundComponent extends NbtComponent implements NbtTagParent {
        
        private final List<NbtComponent> selectorComps;
        private Button addNbtButton;
        
        CompoundComponent(NbtCompoundTag nbtTag, NbtEditorWidget nbtWidget, NbtTagParent parent) {
            super(nbtWidget, nbtTag, parent);
            this.selectorComps = new ArrayList<>();
            initialize();
        }
        
        private void initialize() {
            this.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
            this.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.FLEX_START);
            
            for (NbtTag subNbtTag : ((NbtCompoundTag)nbtTag).getPayload()) {
                NbtComponent subComp = calcNbtComp(subNbtTag, this.nbtWidget, this);
                subComp.getStyle().setMarginLeft((float)INDENT_PER_TAG);
                this.selectorComps.add(subComp);
                this.add(subComp);
            }
            
            this.addNbtButton = new Button("");
            this.addNbtButton.getStyle().getBackground().setIcon(ADD_ICON);
            this.addNbtButton.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.addNbtButton.getStyle().setMinimumSize(ICON_SIZE, ICON_SIZE);
            this.addNbtButton.getStyle().setMargin(MARGIN);
            this.addNbtButton.getStyle().setMarginLeft((float)INDENT_PER_TAG);
            this.addNbtButton.getStyle().setBorder(null);
            this.addNbtButton.getStyle().setShadow(null);
            this.addNbtButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
                if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK) {
                    AddTagWidget addNbtWidget = AddTagWidget.getInstance(null, (NbtTypes nbtType, String tagName) -> {
                        this.remove(this.addNbtButton);
                        NbtTag newTag = nbtType.instantiateEmpty(tagName);
                        ((NbtCompoundTag)nbtTag).putTag(newTag);
                        NbtComponent subComp = calcNbtComp(newTag, this.nbtWidget, this);
                        subComp.getStyle().setMarginLeft((float)INDENT_PER_TAG);
                        this.selectorComps.add(subComp);
                        this.add(subComp);
                        this.add(this.addNbtButton); //ensure button is always last
                        this.nbtWidget.recalcSizes();
                    });
                    if (addNbtWidget != null) {
                        this.nbtWidget.add(addNbtWidget);
                    }
                }
            });
            this.add(this.addNbtButton);
            
            updateSize();
        }
        
        @Override
        public void removeTag(NbtTag tag) {
            ((NbtCompoundTag)this.nbtTag).getPayload().remove(tag);
            this.nbtWidget.updateGui();
        }
        
        @Override
        public void updateHeldNbt() {
            for (NbtTagHolder child : this.selectorComps) {
                child.updateHeldNbt();
            }
        }
        
        @Override
        protected void updateSize() {
            float totalHeight = labelPanel.getSize().y + MARGIN + ICON_SIZE + MARGIN;
            float maxWidth = this.labelPanel.getSize().x + 2 * MARGIN;
            for (NbtComponent subComp : this.selectorComps) {
                subComp.updateSize();
                totalHeight += subComp.getSize().y + MARGIN;
                float newWidth = subComp.getSize().x + MARGIN + INDENT_PER_TAG;
                maxWidth = maxWidth > newWidth ? maxWidth : newWidth;
            }
            //set size so scroll bars work
            float height = totalHeight;
            float width = maxWidth;
            this.setSize(width, height);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setMinWidth(this.getSize().x);
            this.getStyle().setMinHeight(this.getSize().y);
        }
        
    }
    
    private static class ListComponent extends NbtComponent implements NbtTagParent {
        
        private final List<NbtComponent> selectorComps;
        private Button addNbtButton;
        
        ListComponent(NbtListTag nbtTag, NbtEditorWidget nbtWidget, NbtTagParent parent) {
            super(nbtWidget, nbtTag, parent);
            this.selectorComps = new ArrayList<>();
            initialize();
        }
        
        private void initialize() {
            this.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
            this.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.FLEX_START);
            
            for (NbtTag subNbtTag : ((NbtListTag)nbtTag).getPayload()) {
                NbtComponent subComp = calcNbtComp(subNbtTag, this.nbtWidget, this);
                subComp.getStyle().setMarginLeft((float)INDENT_PER_TAG);
                this.selectorComps.add(subComp);
                this.add(subComp);
            }
            
            this.addNbtButton = new Button("");
            this.addNbtButton.getStyle().getBackground().setIcon(ADD_ICON);
            this.addNbtButton.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.addNbtButton.getStyle().setMinimumSize(ICON_SIZE, ICON_SIZE);
            this.addNbtButton.getStyle().setMargin(MARGIN);
            this.addNbtButton.getStyle().setMarginLeft((float)INDENT_PER_TAG);
            this.addNbtButton.getStyle().setBorder(null);
            this.addNbtButton.getStyle().setShadow(null);
            this.addNbtButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
                if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK) {
                    AddTagWidget addNbtWidget = AddTagWidget.getInstance(null, (NbtTypes nbtType, String tagName) -> {
                        this.remove(this.addNbtButton);
                        NbtTag newTag = nbtType.instantiateEmpty(tagName);
                        ((NbtListTag)nbtTag).getPayload().add(newTag);
                        NbtComponent subComp = calcNbtComp(newTag, this.nbtWidget, this);
                        subComp.getStyle().setMarginLeft((float)INDENT_PER_TAG);
                        this.selectorComps.add(subComp);
                        this.add(subComp);
                        this.add(this.addNbtButton); //ensure button is always last
                        this.nbtWidget.recalcSizes();
                    });
                    if (addNbtWidget != null) {
                        this.nbtWidget.add(addNbtWidget);
                    }
                }
            });
            this.add(this.addNbtButton);
            
            updateSize();
        }
        
        @Override
        public void removeTag(NbtTag tag) {
            ((NbtListTag)this.nbtTag).getPayload().remove(tag);
            this.nbtWidget.updateGui();
        }
        
        @Override
        protected void updateSize() {
            float totalHeight = labelPanel.getSize().y + MARGIN + ICON_SIZE + MARGIN;
            float maxWidth = this.labelPanel.getSize().x;
            for (NbtComponent subComp : this.selectorComps) {
                subComp.updateSize();
                totalHeight += subComp.getSize().y + MARGIN;
                float newWidth = subComp.getSize().x + MARGIN + INDENT_PER_TAG;
                maxWidth = maxWidth > newWidth ? maxWidth : newWidth;
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
            for (NbtTagHolder child : this.selectorComps) {
                child.updateHeldNbt();
            }
        }
        
    }
    
    private static class AddTagWidget extends Widget {
        
        private static boolean isOpen = false;
        
        private final AddTagCallback callback;
        private final NbtTypes predeterminedType;
        private SelectBox<NbtTypes> nbtTypeSelector;
        private TextInput nbtNameBox;
        
        private AddTagWidget(NbtTypes predeterminedType, AddTagCallback callback) {
            this.callback = callback;
            this.predeterminedType = predeterminedType;
            initialize();
        }
        
        public static AddTagWidget getInstance(NbtTypes predeterminedType, AddTagCallback callback) {
            if (isOpen) {
                return null;
            }
            isOpen = true;
            return new AddTagWidget(predeterminedType, callback);
        }
        
        private void initialize() {
            this.addWidgetCloseEventListener((event) -> {
                isOpen = false;
            });
            this.getTitle().getTextState().setText("Add NBT Tag");
            this.setCloseable(true);
            this.setMinimizable(false);
            this.setMinimizable(false);
            this.setDraggable(true);
            //absolute pos in root
            this.getStyle().setPosition(Style.PositionType.ABSOLUTE);
            this.getStyle().setLeft(200);
            this.getStyle().setTop(200);
            //self size
            this.getStyle().setMinWidth(300);
            this.getStyle().setMinHeight(250);
            Component container = this.getContainer();
            //Flex layout
            container.getStyle().setDisplay(Style.DisplayType.FLEX);
            container.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
            container.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.FLEX_START);
            container.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.CENTER);
            //padding
            container.getStyle().setPadding(0, 10, 10, 10); //top, right, bottom, left
            //add components
            if (predeterminedType == null) {
                this.nbtTypeSelector = new SelectBox<>();
                for (NbtTypes nbtType : NbtTypes.values()) {
                    if (nbtType == NbtTypes.TAG_END) {
                        continue;
                    }
                    this.nbtTypeSelector.addElement(nbtType);
                }
                this.nbtTypeSelector.setVisibleCount(12);
                this.nbtTypeSelector.setSelected(0, true);
                this.nbtTypeSelector.setTabFocusable(false);
                this.nbtTypeSelector.getStyle().setPosition(Style.PositionType.RELATIVE);
                this.nbtTypeSelector.getStyle().setMaxWidth(200);
                this.nbtTypeSelector.getStyle().setMaxHeight(30);
                this.nbtTypeSelector.getStyle().setHeight(30);
                this.nbtTypeSelector.getStyle().setMinWidth(100);
                this.nbtTypeSelector.getStyle().setMinHeight(30);
                this.nbtTypeSelector.getStyle().setWidth(200);
                this.nbtTypeSelector.getStyle().setMargin(20);
                this.nbtTypeSelector.getStyle().getFlexStyle().setFlexGrow(1);
                this.nbtTypeSelector.getStyle().getFlexStyle().setFlexShrink(1);
                //this.nbtTypeSelector.getSelectionListPanel().s
                container.add(this.nbtTypeSelector);
            }
            this.nbtNameBox = new TextInput();
            this.nbtNameBox.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.nbtNameBox.getStyle().setMaxWidth(200);
            this.nbtNameBox.getStyle().setMaxHeight(30);
            this.nbtNameBox.getStyle().setHeight(30);
            this.nbtNameBox.getStyle().setMinWidth(100);
            this.nbtNameBox.getStyle().setMinHeight(30);
            this.nbtNameBox.getStyle().setWidth(200);
            this.nbtNameBox.getStyle().setMargin(20);
            container.add(this.nbtNameBox);
            
            Button button = new Button();
            button.getStyle().setPosition(Style.PositionType.RELATIVE);
            button.getStyle().setMaxWidth(200);
            button.getStyle().setMaxHeight(30);
            button.getStyle().setHeight(30);
            button.getStyle().setMinWidth(100);
            button.getStyle().setMinHeight(30);
            button.getStyle().setWidth(200);
            button.getStyle().setMargin(20);
            button.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
                if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK) {
                    NbtTypes nbtType = predeterminedType != null ? predeterminedType : this.nbtTypeSelector.getSelection();
                    isOpen = false;
                    this.getParent().remove(this);
                    callback.callback(nbtType, this.nbtNameBox.getTextState().getText());
                }
            });
            button.getTextState().setText("Create NBT Tag");
            container.add(button);
        }
        
    }
    
    private static interface AddTagCallback {
        void callback(NbtTypes type, String name);
    }
}
