
package atomicedit.frontend.ui;

import atomicedit.backend.BlockState;
import atomicedit.backend.parameters.FloatParameterDescriptor;
import atomicedit.backend.parameters.IntegerParameterDescriptor;
import atomicedit.frontend.editor.AreaSelectionEditor;
import atomicedit.operations.OperationType;
import atomicedit.backend.parameters.ParameterDescriptor;
import atomicedit.backend.parameters.Parameters;
import java.util.List;
import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.Component;
import org.liquidengine.legui.component.Panel;
import org.liquidengine.legui.component.SelectBox;
import org.liquidengine.legui.component.event.selectbox.SelectBoxChangeSelectionEvent;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.listener.EventListener;
import org.liquidengine.legui.style.Style;
import org.liquidengine.legui.style.flex.FlexStyle;
import atomicedit.frontend.ui.atomicedit_legui.BlockSelectorComponent;
import atomicedit.frontend.ui.atomicedit_legui.DoubleSelectorComponent;
import atomicedit.frontend.ui.atomicedit_legui.IntegerSelectorComponent;
import java.util.ArrayList;
import org.joml.Vector4f;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.style.color.ColorConstants;
import org.liquidengine.legui.style.length.Length;
import org.liquidengine.legui.style.length.LengthType;

/**
 *
 * @author Justin Bonner
 */
public class AreaSelectionOptionsGui {
    
    
    private static final float GUI_WIDTH = 350;
    private static final float GUI_HEIGHT = 800;
    private static final float GUI_X = 0;
    private static final float GUI_Y = 45; //use 'top bar UI element' height
    private static final String DO_OP_TEXT = "Do Operation";
    
    private final AreaSelectionEditor editor;
    private Panel opPanel;
    private final List<Component> opParamComponents;
    private static OperationType opType = OperationType.SET_BLOCKS_OPERATION;
    private Parameters opParameters;
    private Button doOpButton;
    
    public AreaSelectionOptionsGui(AreaSelectionEditor editor){
        this.editor = editor;
        this.opParamComponents = new ArrayList<>();
        initialize();
    }
    
    private void initialize() {
        this.opPanel = new Panel();
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
        
        //Operation Select Box
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
        opSelectBox.getStyle().setMarginRight(20f);
        opSelectBox.getStyle().getFlexStyle().setFlexGrow(1);
        opSelectBox.getStyle().getFlexStyle().setFlexShrink(1);
        
        //Do Operation Button
        this.doOpButton = new Button();
        this.doOpButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                doOperation(opType);
            }
        });
        this.doOpButton.getTextState().setText(DO_OP_TEXT);
        this.doOpButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        this.doOpButton.getStyle().setMinHeight(30f);
        this.doOpButton.getStyle().setMinWidth(100f);
        
        Component opComp = new Panel();
        opComp.setFocusable(false);
        opComp.getStyle().setBorder(null);
        opComp.getStyle().setShadow(null);
        opComp.getStyle().getBackground().setColor(ColorConstants.transparent());
        opComp.getStyle().setPosition(Style.PositionType.RELATIVE);
        opComp.getStyle().setDisplay(Style.DisplayType.FLEX);
        opComp.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.ROW);
        opComp.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.CENTER);
        opComp.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.CENTER);
        opComp.getStyle().setWidth(new Length(98f, LengthType.PERCENT));
        opComp.getStyle().setMinHeight(30);
        opComp.getStyle().setMaxHeight(30);
        opComp.getStyle().setMargin(20);
        opComp.add(opSelectBox);
        opComp.add(doOpButton);
        
        this.opPanel.add(opComp);
        
        updateOpType(opType);
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
    
    private void doOperation(OperationType opType){
        editor.doOperation(opType, this.opParameters);
    }
    
    public Panel getOpPanel() {
        return this.opPanel;
    }
    
}
