
package atomicedit.frontend.ui;

import atomicedit.backend.BlockState;
import atomicedit.backend.brushes.BrushType;
import atomicedit.backend.parameters.FloatParameterDescriptor;
import atomicedit.backend.parameters.IntegerParameterDescriptor;
import atomicedit.backend.parameters.ParameterDescriptor;
import atomicedit.backend.parameters.Parameters;
import atomicedit.frontend.editor.BrushEditor;
import atomicedit.frontend.ui.atomicedit_legui.BlockSelectorComponent;
import atomicedit.frontend.ui.atomicedit_legui.DoubleSelectorComponent;
import atomicedit.frontend.ui.atomicedit_legui.IntegerSelectorComponent;
import atomicedit.operations.OperationType;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector4f;
import org.liquidengine.legui.component.Component;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.component.Panel;
import org.liquidengine.legui.component.SelectBox;
import org.liquidengine.legui.component.event.selectbox.SelectBoxChangeSelectionEvent;
import org.liquidengine.legui.listener.EventListener;
import org.liquidengine.legui.style.Style;
import org.liquidengine.legui.style.color.ColorConstants;
import org.liquidengine.legui.style.flex.FlexStyle;

/**
 * https://yogalayout.com/docs/
 * @author justin
 */
public class BrushGui {
    
    private static final int GUI_WIDTH = 350;
    private static final int GUI_HEIGHT = 800;
    
    private final BrushEditor editor;
    private final Panel brushPanel;
    private final Panel opPanel;
    private final List<Component> brushParamComponents;
    private BrushType brushType;
    private Parameters brushParameters;
    private final List<Component> opParamComponents;
    private OperationType opType;
    private Parameters opParameters;
    
    public BrushGui(BrushEditor editor) {
        this.editor = editor;
        this.brushParamComponents = new ArrayList<>();
        this.opParamComponents = new ArrayList<>();
        this.brushPanel = new Panel();
        this.opPanel = new Panel();
        initialize();
    }
    
    private void initialize() {
        this.brushPanel.setFocusable(false);
        this.brushPanel.getStyle().getBackground().setColor(AtomicEditGui.PANEL_COLOR);
        //absolute pos in root
        this.brushPanel.getStyle().setPosition(Style.PositionType.ABSOLUTE);
        this.brushPanel.getStyle().setRight(0);
        this.brushPanel.getStyle().setTop(50);
        
        //self size
        this.brushPanel.getStyle().setMinWidth(GUI_WIDTH);
        this.brushPanel.getStyle().setMinHeight(GUI_HEIGHT);
        //Flex layout
        this.brushPanel.getStyle().setDisplay(Style.DisplayType.FLEX);
        this.brushPanel.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
        this.brushPanel.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.FLEX_START);
        this.brushPanel.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.CENTER);
        
        this.brushPanel.getStyle().setPadding(30, 10, 30, 10); //top, right, bottom, left
        
        SelectBox<BrushType> selectBox = new SelectBox<>();
        for (BrushType brushType : BrushType.values()) {
            selectBox.addElement(brushType);
        }
        selectBox.setSelected(0, true);
        selectBox.getSelectBoxChangeSelectionEvents().add((EventListener<SelectBoxChangeSelectionEvent<BrushType>>)(event) -> {
            updateBrushType(event.getNewValue());
        });
        selectBox.setTabFocusable(false);
        selectBox.getStyle().setPosition(Style.PositionType.RELATIVE);
        selectBox.getStyle().setMaxWidth(200);
        selectBox.getStyle().setMaxHeight(30);
        selectBox.getStyle().setHeight(30);
        selectBox.getStyle().setMinWidth(100);
        selectBox.getStyle().setMinHeight(30);
        selectBox.getStyle().setWidth(200);
        selectBox.getStyle().setMargin(20);
        selectBox.getStyle().getFlexStyle().setFlexGrow(1);
        selectBox.getStyle().getFlexStyle().setFlexShrink(1);
        
        this.brushPanel.add(selectBox);
        updateBrushType(BrushType.ELIPSE);
        
        //OP panel
        this.opPanel.setFocusable(false);
        this.opPanel.getStyle().getBackground().setColor(AtomicEditGui.PANEL_COLOR);
        //absolute pos in root
        this.opPanel.getStyle().setPosition(Style.PositionType.ABSOLUTE);
        this.opPanel.getStyle().setLeft(0);
        this.opPanel.getStyle().setTop(50);
        
        //self size
        this.opPanel.getStyle().setMinWidth(GUI_WIDTH);
        this.opPanel.getStyle().setMinHeight(GUI_HEIGHT);
        //Flex layout
        this.opPanel.getStyle().setDisplay(Style.DisplayType.FLEX);
        this.opPanel.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
        this.opPanel.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.FLEX_START);
        this.opPanel.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.CENTER);
        
        this.opPanel.getStyle().setPadding(30, 10, 30, 10); //top, right, bottom, left
        
        SelectBox<OperationType> opSelectBox = new SelectBox<>();
        for (OperationType opType : OperationType.values()) {
            opSelectBox.addElement(opType);
        }
        opSelectBox.setSelected(0, true);
        opSelectBox.getSelectBoxChangeSelectionEvents().add((EventListener<SelectBoxChangeSelectionEvent<OperationType>>)(event) -> {
            updateOpType(event.getNewValue());
        });
        opSelectBox.setTabFocusable(false);
        opSelectBox.getStyle().setPosition(Style.PositionType.RELATIVE);
        opSelectBox.getStyle().setMaxWidth(200);
        opSelectBox.getStyle().setMaxHeight(30);
        opSelectBox.getStyle().setHeight(30);
        opSelectBox.getStyle().setMinWidth(100);
        opSelectBox.getStyle().setMinHeight(30);
        opSelectBox.getStyle().setWidth(200);
        opSelectBox.getStyle().setMargin(20);
        opSelectBox.getStyle().getFlexStyle().setFlexGrow(1);
        opSelectBox.getStyle().getFlexStyle().setFlexShrink(1);
        
        this.opPanel.add(opSelectBox);
        updateOpType(OperationType.SET_BLOCKS_OPERATION);
    }
    
    private void updateBrushType(BrushType newBrushType) {
        this.brushType = newBrushType;
        this.brushParameters = new Parameters();
        this.brushPanel.removeAll(brushParamComponents);
        brushParamComponents.clear();
        for (ParameterDescriptor paramDesc : newBrushType.getParameterDescriptors()) {
            this.brushParameters.setParam(paramDesc, paramDesc.defaultValue);
            
            final int paramPanelHeight = 50;
            Panel paramPanel = new Panel();
            paramPanel.setFocusable(false);
            paramPanel.getStyle().setBorder(null);
            paramPanel.getStyle().setShadow(null);
            paramPanel.getStyle().getBackground().setColor(ColorConstants.transparent());
            paramPanel.getStyle().setPosition(Style.PositionType.RELATIVE);
            paramPanel.getStyle().setMargin(10);
            paramPanel.getStyle().setMaxWidth(Float.MAX_VALUE);
            paramPanel.getStyle().setMinWidth(GUI_WIDTH - 20);
            paramPanel.getStyle().setMaxHeight(paramPanelHeight);
            paramPanel.getStyle().setMinHeight(paramPanelHeight);
            paramPanel.getStyle().setHeight(paramPanelHeight);
            paramPanel.getStyle().getFlexStyle().setFlexGrow(1);
            paramPanel.getStyle().getFlexStyle().setFlexShrink(1);
            paramPanel.getStyle().setDisplay(Style.DisplayType.FLEX);
            paramPanel.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.CENTER);
            paramPanel.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.FLEX_START);
            paramPanel.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.ROW);
            
            Label paramLabel = new Label(paramDesc.name);
            paramLabel.getStyle().setFontSize(20f);
            paramLabel.getStyle().setPosition(Style.PositionType.RELATIVE);
            paramLabel.getStyle().setMaxWidth(Float.MAX_VALUE);
            paramLabel.getStyle().setMaxHeight(50);
            paramLabel.getStyle().setHeight(30);
            paramLabel.getStyle().setMinWidth(100);
            paramLabel.getStyle().setMinHeight(30);
            paramLabel.getStyle().getBackground().setColor(new Vector4f(.5f, .5f, .5f, .5f));
            paramLabel.getStyle().setBorder(null);
            paramLabel.getStyle().getFlexStyle().setFlexGrow(1);
            paramLabel.getStyle().getFlexStyle().setFlexShrink(1);
            paramLabel.setTabFocusable(false);
            paramLabel.setFocusable(false);
            paramPanel.add(paramLabel);
            
            /*
            this.brushParameters.setParam(paramDesc, (int)event.getNewValue());
                        editor.setBrush(this.brushType.createInstance(), this.brushParameters);
            */
            
            switch(paramDesc.parameterType) {
                case INT:
                    IntegerParameterDescriptor intDesc = (IntegerParameterDescriptor)paramDesc;
                    IntegerSelectorComponent intSelector = new IntegerSelectorComponent(intDesc.minAllowed, intDesc.maxAllowed, intDesc.defaultValue);
                    intSelector.setValueChangeCallback((long newValue) -> {
                        this.brushParameters.setParam(paramDesc, (int)newValue);
                        editor.setBrush(this.brushType.createInstance(), this.brushParameters);
                    });
                    intSelector.getStyle().setMinimumSize(100, 30);
                    intSelector.getStyle().setPosition(Style.PositionType.RELATIVE);
                    intSelector.getStyle().getFlexStyle().setFlexGrow(1);
                    intSelector.getStyle().getFlexStyle().setFlexShrink(1);
                    paramPanel.add(intSelector);
                    break;
                case FLOAT:
                    FloatParameterDescriptor floatDesc = (FloatParameterDescriptor)paramDesc;
                    DoubleSelectorComponent floatSelector = new DoubleSelectorComponent(floatDesc.minAllowed, floatDesc.maxAllowed, floatDesc.defaultValue);
                    floatSelector.setValueChangeCallback((double newValue) -> {
                        this.brushParameters.setParam(paramDesc, (float)newValue);
                        editor.setBrush(this.brushType.createInstance(), this.brushParameters);
                    });
                    floatSelector.getStyle().setMinimumSize(100, 30);
                    floatSelector.getStyle().setPosition(Style.PositionType.RELATIVE);
                    floatSelector.getStyle().getFlexStyle().setFlexGrow(1);
                    floatSelector.getStyle().getFlexStyle().setFlexShrink(1);
                    paramPanel.add(floatSelector);
                    break;
                case BLOCK_SELECTOR:
                    BlockSelectorComponent blockSelector = new BlockSelectorComponent((BlockState)paramDesc.defaultValue);
                    blockSelector.setCallback((blockState) -> {
                        this.brushParameters.setParam(paramDesc, blockState);
                        editor.setBrush(this.brushType.createInstance(), this.brushParameters);
                    });
                    blockSelector.getStyle().setMinWidth(200);
                    blockSelector.getStyle().setMaxWidth(Float.MAX_VALUE);
                    blockSelector.getStyle().setMinHeight(30);
                    blockSelector.getStyle().setMaxHeight(30);
                    blockSelector.getStyle().setPosition(Style.PositionType.RELATIVE);
                    blockSelector.getStyle().getFlexStyle().setFlexGrow(1);
                    blockSelector.getStyle().getFlexStyle().setFlexShrink(1);
                    paramPanel.add(blockSelector);
                    break;
            }
            
            this.brushParamComponents.add(paramPanel);
            this.brushPanel.add(paramPanel);
        }
        this.editor.setBrush(this.brushType.createInstance(), this.brushParameters);
    }
    
    private void updateOpType(OperationType newOpType) {
        this.opType = newOpType;
        this.opParameters = new Parameters();
        this.opPanel.removeAll(opParamComponents);
        opParamComponents.clear();
        for (ParameterDescriptor paramDesc : newOpType.getOperationParameterDescription()) {
            this.opParameters.setParam(paramDesc, paramDesc.defaultValue);
            
            final int paramPanelHeight = 50;
            Panel paramPanel = new Panel();
            paramPanel.setFocusable(false);
            paramPanel.getStyle().setBorder(null);
            paramPanel.getStyle().setShadow(null);
            paramPanel.getStyle().getBackground().setColor(ColorConstants.transparent());
            paramPanel.getStyle().setPosition(Style.PositionType.RELATIVE);
            paramPanel.getStyle().setMargin(10);
            paramPanel.getStyle().setMaxWidth(Float.MAX_VALUE);
            paramPanel.getStyle().setMinWidth(GUI_WIDTH - 20);
            paramPanel.getStyle().setMaxHeight(paramPanelHeight);
            paramPanel.getStyle().setMinHeight(paramPanelHeight);
            paramPanel.getStyle().setHeight(paramPanelHeight);
            paramPanel.getStyle().getFlexStyle().setFlexGrow(1);
            paramPanel.getStyle().getFlexStyle().setFlexShrink(1);
            paramPanel.getStyle().setDisplay(Style.DisplayType.FLEX);
            paramPanel.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.CENTER);
            paramPanel.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.FLEX_START);
            paramPanel.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.ROW);
            
            Label paramLabel = new Label(paramDesc.name);
            paramLabel.getStyle().setFontSize(20f);
            paramLabel.getStyle().setPosition(Style.PositionType.RELATIVE);
            paramLabel.getStyle().setMaxWidth(Float.MAX_VALUE);
            paramLabel.getStyle().setMaxHeight(50);
            paramLabel.getStyle().setHeight(30);
            paramLabel.getStyle().setMinWidth(100);
            paramLabel.getStyle().setMinHeight(30);
            paramLabel.getStyle().getBackground().setColor(new Vector4f(.5f, .5f, .5f, .5f));
            paramLabel.getStyle().setBorder(null);
            paramLabel.getStyle().getFlexStyle().setFlexGrow(1);
            paramLabel.getStyle().getFlexStyle().setFlexShrink(1);
            paramLabel.setTabFocusable(false);
            paramLabel.setFocusable(false);
            paramPanel.add(paramLabel);
            
            switch(paramDesc.parameterType) {
                case INT:
                    IntegerParameterDescriptor intDesc = (IntegerParameterDescriptor)paramDesc;
                    IntegerSelectorComponent intSelector = new IntegerSelectorComponent(intDesc.minAllowed, intDesc.maxAllowed, intDesc.defaultValue);
                    intSelector.setValueChangeCallback((long newValue) -> {
                        this.opParameters.setParam(paramDesc, (int)newValue);
                    });
                    intSelector.getStyle().setMinimumSize(100, 30);
                    intSelector.getStyle().setPosition(Style.PositionType.RELATIVE);
                    intSelector.getStyle().getFlexStyle().setFlexGrow(1);
                    intSelector.getStyle().getFlexStyle().setFlexShrink(1);
                    paramPanel.add(intSelector);
                    break;
                case FLOAT:
                    FloatParameterDescriptor floatDesc = (FloatParameterDescriptor)paramDesc;
                    DoubleSelectorComponent floatSelector = new DoubleSelectorComponent(floatDesc.minAllowed, floatDesc.maxAllowed, floatDesc.defaultValue);
                    floatSelector.setValueChangeCallback((double newValue) -> {
                        this.opParameters.setParam(paramDesc, (float)newValue);
                    });
                    floatSelector.getStyle().setMinimumSize(100, 30);
                    floatSelector.getStyle().setPosition(Style.PositionType.RELATIVE);
                    floatSelector.getStyle().getFlexStyle().setFlexGrow(1);
                    floatSelector.getStyle().getFlexStyle().setFlexShrink(1);
                    paramPanel.add(floatSelector);
                    break;
                case BLOCK_SELECTOR:
                    BlockSelectorComponent blockSelector = new BlockSelectorComponent((BlockState)paramDesc.defaultValue);
                    blockSelector.setCallback((blockState) -> {
                        this.opParameters.setParam(paramDesc, blockState);
                    });
                    blockSelector.getStyle().setMinWidth(200);
                    blockSelector.getStyle().setMaxWidth(Float.MAX_VALUE);
                    blockSelector.getStyle().setMinHeight(30);
                    blockSelector.getStyle().setMaxHeight(30);
                    blockSelector.getStyle().setPosition(Style.PositionType.RELATIVE);
                    blockSelector.getStyle().getFlexStyle().setFlexGrow(1);
                    blockSelector.getStyle().getFlexStyle().setFlexShrink(1);
                    paramPanel.add(blockSelector);
                    break;
            }
            
            this.opParamComponents.add(paramPanel);
            this.opPanel.add(paramPanel);
        }
    }
    
    public Panel getBrushPanel() {
        return this.brushPanel;
    }
    
    public Panel getOpPanel() {
        return this.opPanel;
    }
    
    public OperationType getOperationType() {
        return this.opType;
    }
    
    public Parameters getOperationParameters() {
        return this.opParameters;
    }
    
    public BrushType getBrushType() {
        return this.brushType;
    }
    
    public Parameters getBrushParameters() {
        return this.brushParameters;
    }
    
}
