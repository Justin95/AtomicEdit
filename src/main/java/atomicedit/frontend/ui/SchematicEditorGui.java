
package atomicedit.frontend.ui;

import atomicedit.frontend.editor.SchematicEditor;
import atomicedit.frontend.editor.SchematicEditor.EditorStatus;
import atomicedit.utils.FileUtils;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector2f;
import org.joml.Vector3i;
import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.CheckBox;
import org.liquidengine.legui.component.Component;
import org.liquidengine.legui.component.Panel;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.icon.ImageIcon;
import org.liquidengine.legui.style.Style;
import org.liquidengine.legui.style.color.ColorConstants;
import org.liquidengine.legui.style.flex.FlexStyle;
import org.liquidengine.legui.style.length.Length;
import org.liquidengine.legui.style.length.LengthType;

/**
 *
 * @author Justin Bonner
 */
public class SchematicEditorGui {
    
    private static final int GUI_WIDTH = 350;
    private static final int GUI_HEIGHT = 800;
    private static final ImageIcon ROTATE_LEFT_ICON = FileUtils.loadIcon("icons/rotate_left.png");
    private static final ImageIcon ROTATE_RIGHT_ICON = FileUtils.loadIcon("icons/rotate_right.png");
    private static final ImageIcon Y_FLIP_ICON = FileUtils.loadIcon("icons/flip.png");
    
    static {
        ROTATE_LEFT_ICON.setSize(new Vector2f(30, 30));
        ROTATE_RIGHT_ICON.setSize(new Vector2f(30, 30));
        Y_FLIP_ICON.setSize((new Vector2f(30, 30)));
    }
    
    private final SchematicEditor editor;
    private EditorStatus status;
    private Panel schematicPanel;
    private final Map<EditorStatus, List<Component>> statusToComponents;
    private CheckBox includeAirCheckBox;
    
    public SchematicEditorGui(SchematicEditor editor) {
        this.editor = editor;
        this.status = EditorStatus.SELECT;
        this.statusToComponents = new EnumMap<>(EditorStatus.class);
        for (EditorStatus eStatus : EditorStatus.values()) {
            this.statusToComponents.put(eStatus, new ArrayList<>());
        }
        initialize();
        updateStatus(this.status);
    }
    
    private void initialize() {
        this.schematicPanel = new Panel();
        this.schematicPanel.setFocusable(false);
        this.schematicPanel.getStyle().getBackground().setColor(AtomicEditGui.PANEL_COLOR);
        //absolute pos in root
        this.schematicPanel.getStyle().setPosition(Style.PositionType.ABSOLUTE);
        this.schematicPanel.getStyle().setLeft(0);
        this.schematicPanel.getStyle().setTop(50);
        //self size
        this.schematicPanel.getStyle().setMinWidth(GUI_WIDTH);
        this.schematicPanel.getStyle().setMinHeight(GUI_HEIGHT);
        //Flex layout
        this.schematicPanel.getStyle().setDisplay(Style.DisplayType.FLEX);
        this.schematicPanel.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
        this.schematicPanel.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.FLEX_START);
        this.schematicPanel.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.CENTER);
        //padding
        this.schematicPanel.getStyle().setPadding(30, 10, 30, 10); //top, right, bottom, left
        
        /*
        Add each component to the appropriate status list(s)
        */
        //pickup button
        Button pickupButton = new Button();
        pickupButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                editor.pickupSchematic(this.includeAirCheckBox.isChecked());
            }
        });
        pickupButton.getTextState().setText("Pickup Schematic");
        pickupButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        pickupButton.getStyle().setMinHeight(30f);
        pickupButton.getStyle().setMinWidth(130f);
        pickupButton.getStyle().setMargin(10);
        this.statusToComponents.get(EditorStatus.SELECT).add(pickupButton);
        //load button
        Button loadButton = new Button();
        loadButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                //TODO add schematic loading
            }
        });
        loadButton.getTextState().setText("Load Schematic");
        loadButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        loadButton.getStyle().setMinHeight(30f);
        loadButton.getStyle().setMinWidth(130f);
        loadButton.getStyle().setMargin(10);
        this.statusToComponents.get(EditorStatus.SELECT).add(loadButton);
        //pickup / load button panel
        Panel plPanel = new Panel();
        plPanel.setFocusable(false);
        plPanel.getStyle().setBorder(null);
        plPanel.getStyle().setShadow(null);
        plPanel.getStyle().getBackground().setColor(ColorConstants.transparent());
        plPanel.getStyle().setPosition(Style.PositionType.RELATIVE);
        plPanel.getStyle().setDisplay(Style.DisplayType.FLEX);
        plPanel.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.ROW);
        plPanel.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.CENTER);
        plPanel.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.CENTER);
        plPanel.getStyle().setWidth(new Length(98f, LengthType.PERCENT));
        plPanel.getStyle().setMinHeight(32);
        plPanel.getStyle().setMargin(20);
        plPanel.add(pickupButton);
        plPanel.add(loadButton);
        this.schematicPanel.add(plPanel);
        
        //include air checkbox (panel with checkbox and label)
        this.includeAirCheckBox = new CheckBox("Include Air");
        this.includeAirCheckBox.setChecked(true);
        this.includeAirCheckBox.getStyle().setFontSize(19f);
        this.includeAirCheckBox.getStyle().setPosition(Style.PositionType.RELATIVE);
        this.includeAirCheckBox.getStyle().setMinWidth(100);
        this.includeAirCheckBox.getStyle().setMinHeight(30);
        this.includeAirCheckBox.getStyle().getBackground().setColor(.5f, .5f, .5f, .5f);
        this.statusToComponents.get(EditorStatus.SELECT).add(this.includeAirCheckBox);
        this.schematicPanel.add(this.includeAirCheckBox);
        
        //discard button
        Button discardButton = new Button();
        discardButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                editor.clearSchematic();
            }
        });
        discardButton.getTextState().setText("Discard Schematic");
        discardButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        discardButton.getStyle().setMinHeight(30f);
        discardButton.getStyle().setMinWidth(130f);
        discardButton.getStyle().setMargin(10);
        this.statusToComponents.get(EditorStatus.INITIAL_PLACE).add(discardButton);
        
        //save button
        Button saveButton = new Button();
        saveButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                //TODO
            }
        });
        saveButton.getTextState().setText("Save Schematic");
        saveButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        saveButton.getStyle().setMinHeight(30f);
        saveButton.getStyle().setMinWidth(130f);
        saveButton.getStyle().setMargin(10);
        this.statusToComponents.get(EditorStatus.INITIAL_PLACE).add(saveButton);
        
        //save / discard panel
        Panel sdPanel = new Panel();
        sdPanel.setFocusable(false);
        sdPanel.getStyle().setBorder(null);
        sdPanel.getStyle().setShadow(null);
        sdPanel.getStyle().getBackground().setColor(ColorConstants.transparent());
        sdPanel.getStyle().setPosition(Style.PositionType.RELATIVE);
        sdPanel.getStyle().setDisplay(Style.DisplayType.FLEX);
        sdPanel.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.ROW);
        sdPanel.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.CENTER);
        sdPanel.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.CENTER);
        sdPanel.getStyle().setWidth(new Length(98f, LengthType.PERCENT));
        sdPanel.getStyle().setMinHeight(32);
        sdPanel.getStyle().setMargin(20);
        sdPanel.add(saveButton);
        sdPanel.add(discardButton);
        this.schematicPanel.add(sdPanel);
        
        //Brush placement checkbox (panel with checkbox and label)
        CheckBox brushCheckBox = new CheckBox("Brush Placement");
        brushCheckBox.setChecked(false);
        brushCheckBox.addCheckBoxChangeValueListener((event) -> {
            editor.setBrushPlacement(event.getNewValue());
        });
        brushCheckBox.getStyle().setFontSize(19f);
        brushCheckBox.getStyle().setPosition(Style.PositionType.RELATIVE);
        brushCheckBox.getStyle().setMinWidth(130);
        brushCheckBox.getStyle().setMinHeight(30);
        brushCheckBox.getStyle().getBackground().setColor(.5f, .5f, .5f, .5f);
        this.statusToComponents.get(EditorStatus.SELECT).add(brushCheckBox);
        this.statusToComponents.get(EditorStatus.INITIAL_PLACE).add(brushCheckBox);
        this.schematicPanel.add(brushCheckBox);
        
        //rotate left button
        Button rotateLeftButton = new Button();
        rotateLeftButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                editor.rotateLeft();
            }
        });
        rotateLeftButton.getStyle().getBackground().setIcon(ROTATE_LEFT_ICON);
        rotateLeftButton.getTextState().setText("");
        rotateLeftButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        rotateLeftButton.getStyle().setMinHeight(30f);
        rotateLeftButton.getStyle().setMinWidth(30f);
        rotateLeftButton.getStyle().setMargin(10);
        this.statusToComponents.get(EditorStatus.INITIAL_PLACE).add(rotateLeftButton);
        this.statusToComponents.get(EditorStatus.FINE_TUNING).add(rotateLeftButton);
        
        //rotate right button
        Button rotateRightButton = new Button();
        rotateRightButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                editor.rotateRight();
            }
        });
        rotateRightButton.getStyle().getBackground().setIcon(ROTATE_RIGHT_ICON);
        rotateRightButton.getTextState().setText("");
        rotateRightButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        rotateRightButton.getStyle().setMinHeight(30f);
        rotateRightButton.getStyle().setMinWidth(30f);
        rotateRightButton.getStyle().setMargin(10);
        this.statusToComponents.get(EditorStatus.INITIAL_PLACE).add(rotateRightButton);
        this.statusToComponents.get(EditorStatus.FINE_TUNING).add(rotateRightButton);
        
        //y flip button
        Button yFlipButton = new Button();
        yFlipButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                editor.doYFlip();
            }
        });
        yFlipButton.getStyle().getBackground().setIcon(Y_FLIP_ICON);
        yFlipButton.getTextState().setText("");
        yFlipButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        yFlipButton.getStyle().setMinHeight(30f);
        yFlipButton.getStyle().setMinWidth(30f);
        yFlipButton.getStyle().setMargin(10);
        this.statusToComponents.get(EditorStatus.INITIAL_PLACE).add(yFlipButton);
        this.statusToComponents.get(EditorStatus.FINE_TUNING).add(yFlipButton);
        
        //rotate button panel
        Panel rotatePanel = new Panel();
        rotatePanel.setFocusable(false);
        rotatePanel.getStyle().setBorder(null);
        rotatePanel.getStyle().setShadow(null);
        rotatePanel.getStyle().getBackground().setColor(ColorConstants.transparent());
        rotatePanel.getStyle().setPosition(Style.PositionType.RELATIVE);
        rotatePanel.getStyle().setDisplay(Style.DisplayType.FLEX);
        rotatePanel.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.ROW);
        rotatePanel.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.CENTER);
        rotatePanel.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.CENTER);
        rotatePanel.getStyle().setWidth(new Length(98f, LengthType.PERCENT));
        rotatePanel.getStyle().setMinHeight(32);
        rotatePanel.getStyle().setMargin(20);
        rotatePanel.add(rotateLeftButton);
        rotatePanel.add(rotateRightButton);
        rotatePanel.add(yFlipButton);
        this.schematicPanel.add(rotatePanel);
        
        //x+ button
        Button xPlusButton = new Button();
        xPlusButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                editor.adjustOffset(new Vector3i(1, 0, 0));
            }
        });
        xPlusButton.getTextState().setText("X+");
        xPlusButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        xPlusButton.getStyle().setMinHeight(30f);
        xPlusButton.getStyle().setMinWidth(30f);
        xPlusButton.getStyle().setMargin(10);
        this.statusToComponents.get(EditorStatus.FINE_TUNING).add(xPlusButton);
        //x- button
        Button xMinusButton = new Button();
        xMinusButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                editor.adjustOffset(new Vector3i(-1, 0, 0));
            }
        });
        xMinusButton.getTextState().setText("X-");
        xMinusButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        xMinusButton.getStyle().setMinHeight(30f);
        xMinusButton.getStyle().setMinWidth(30f);
        xMinusButton.getStyle().setMargin(10);
        this.statusToComponents.get(EditorStatus.FINE_TUNING).add(xMinusButton);
        
        //y+ button
        Button yPlusButton = new Button();
        yPlusButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                editor.adjustOffset(new Vector3i(0, 1, 0));
            }
        });
        yPlusButton.getTextState().setText("Y+");
        yPlusButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        yPlusButton.getStyle().setMinHeight(30f);
        yPlusButton.getStyle().setMinWidth(30f);
        yPlusButton.getStyle().setMargin(10);
        this.statusToComponents.get(EditorStatus.FINE_TUNING).add(yPlusButton);
        
        //y- button
        Button yMinusButton = new Button();
        yMinusButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                editor.adjustOffset(new Vector3i(0, -1, 0));
            }
        });
        yMinusButton.getTextState().setText("Y-");
        yMinusButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        yMinusButton.getStyle().setMinHeight(30f);
        yMinusButton.getStyle().setMinWidth(30f);
        yMinusButton.getStyle().setMargin(10);
        this.statusToComponents.get(EditorStatus.FINE_TUNING).add(yMinusButton);
        
        //z+ button
        Button zPlusButton = new Button();
        zPlusButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                editor.adjustOffset(new Vector3i(0, 0, 1));
            }
        });
        zPlusButton.getTextState().setText("Z+");
        zPlusButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        zPlusButton.getStyle().setMinHeight(30f);
        zPlusButton.getStyle().setMinWidth(30f);
        zPlusButton.getStyle().setMargin(10);
        this.statusToComponents.get(EditorStatus.FINE_TUNING).add(zPlusButton);
        
        //z- button
        Button zMinusButton = new Button();
        zMinusButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                editor.adjustOffset(new Vector3i(0, 0, -1));
            }
        });
        zMinusButton.getTextState().setText("Z-");
        zMinusButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        zMinusButton.getStyle().setMinHeight(30f);
        zMinusButton.getStyle().setMinWidth(30f);
        zMinusButton.getStyle().setMargin(10);
        this.statusToComponents.get(EditorStatus.FINE_TUNING).add(zMinusButton);
        
        //translation panel
        Panel translatePanel = new Panel();
        translatePanel.setFocusable(false);
        translatePanel.getStyle().setBorder(null);
        translatePanel.getStyle().setShadow(null);
        translatePanel.getStyle().getBackground().setColor(ColorConstants.transparent());
        translatePanel.getStyle().setPosition(Style.PositionType.RELATIVE);
        translatePanel.getStyle().setDisplay(Style.DisplayType.FLEX);
        translatePanel.getStyle().getFlexStyle().setFlexWrap(FlexStyle.FlexWrap.WRAP);
        translatePanel.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.ROW);
        translatePanel.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.CENTER);
        translatePanel.getStyle().getFlexStyle().setAlignContent(FlexStyle.AlignContent.STRETCH);
        translatePanel.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.CENTER);
        translatePanel.getStyle().setMinWidth(160);
        translatePanel.getStyle().setMinHeight(100);
        translatePanel.getStyle().setMargin(10);
        translatePanel.add(xPlusButton);
        translatePanel.add(yPlusButton);
        translatePanel.add(zPlusButton);
        translatePanel.add(xMinusButton);
        translatePanel.add(yMinusButton);
        translatePanel.add(zMinusButton);
        this.schematicPanel.add(translatePanel);
        
        //place button
        Button placeButton = new Button();
        placeButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                editor.placeSchematic();
            }
        });
        placeButton.getTextState().setText("Place Schematic");
        placeButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        placeButton.getStyle().setMinHeight(30f);
        placeButton.getStyle().setMinWidth(130f);
        placeButton.getStyle().setMargin(10);
        this.statusToComponents.get(EditorStatus.FINE_TUNING).add(placeButton);
        this.schematicPanel.add(placeButton);
    }
    
    public Panel getSchematicPanel() {
        return this.schematicPanel;
    }
    
    public final void updateStatus(EditorStatus newStatus) {
        for (Component comp : this.statusToComponents.get(this.status)) {
            comp.setFocusable(false);
        }
        for (Component comp : this.statusToComponents.get(newStatus)) {
            comp.setFocusable(true);
        }
        this.status = newStatus;
    }
    
}
