
package atomicedit.frontend.ui;

import atomicedit.frontend.ui.atomicedit_legui.StringOperationParameterComponent;
import atomicedit.frontend.editor.AreaSelectionEditor;
import atomicedit.frontend.ui.atomicedit_legui.AeStandardPanel;
import atomicedit.frontend.ui.atomicedit_legui.BlockSelectorComponent;
import atomicedit.logging.Logger;
import atomicedit.operations.OperationResult;
import atomicedit.operations.OperationType;
import atomicedit.operations.OperationTypes;
import atomicedit.operations.utils.OperationParameterDescriptor;
import atomicedit.operations.utils.OperationParameterGuiElement;
import atomicedit.operations.utils.OperationParameters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.Component;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.component.Panel;
import org.liquidengine.legui.component.SelectBox;
import org.liquidengine.legui.component.event.selectbox.SelectBoxChangeSelectionEvent;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.listener.EventListener;
import org.liquidengine.legui.style.Style;
import org.liquidengine.legui.style.flex.FlexStyle;

/**
 *
 * @author Justin Bonner
 */
public class AreaSelectionOptionsGui extends Panel{
    
    private static final int BUFFER_WIDTH = 5;
    private static final float GUI_WIDTH = 300;
    private static final float GUI_HEIGHT = 800;
    private static final float GUI_X = 0;
    private static final float GUI_Y = 45; //use 'top bar UI element' height
    private static final String DO_OP_TEXT = "Do Operation";
    
    private final AreaSelectionEditor editor;
    private Map<OperationParameterDescriptor, OperationParameterGuiElement> opParameterComponents;
    private Label firstPointLabel;
    private Label secondPointLabel;
    private SelectBox<OperationType> operationsSelectBox;
    //create custom operation panel from operation parameter descriptiors
    private AeStandardPanel operationPanel;
    private Button doOpButton;
    
    public AreaSelectionOptionsGui(AreaSelectionEditor editor){
        this.editor = editor;
        this.operationsSelectBox = createOpSelectBox();
        this.add(operationsSelectBox);
        this.doOpButton = createDoOpButton();
        this.add(doOpButton);
        this.setFocusable(false);
        this.setSize(GUI_WIDTH, GUI_HEIGHT);
        this.getStyle().setLeft(0f);
        this.getStyle().getBackground().setColor(AtomicEditGui.PANEL_COLOR);
        this.setPosition(GUI_X, GUI_Y);
        this.getStyle().setPosition(Style.PositionType.ABSOLUTE);
        this.getStyle().setMaxWidth(GUI_WIDTH);
        this.getStyle().setTop(GUI_Y);
        this.getStyle().setBottom(GUI_Y);
        this.getStyle().setHeight(GUI_HEIGHT);
        this.getStyle().setWidth(GUI_WIDTH);
        this.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.CENTER);
        this.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
        this.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.CENTER);
        this.getStyle().setDisplay(Style.DisplayType.FLEX);
        updateOperationPanel();
    }
    
    
    public final void updateOperationPanel(){ //TODO make thread safe with do operation button
        if(this.operationPanel != null){
            this.remove(operationPanel);
        }
        this.operationPanel = createOpPanel();
        this.operationPanel.getStyle().setWidth(GUI_WIDTH);
        this.operationPanel.getStyle().setLeft(0f);
        this.operationPanel.getStyle().setTop(70f);
        this.operationPanel.getStyle().setBottom(0f);
        this.operationPanel.setFocusable(false);
        this.add(operationPanel);
    }
    
    private AeStandardPanel createOpPanel(){
        List<OperationParameterDescriptor> descriptors = operationsSelectBox.getSelection().getOperationParameterDescription();
        this.opParameterComponents = new HashMap<>();
        AeStandardPanel opPanel = new AeStandardPanel();
        for(OperationParameterDescriptor descriptor : descriptors){
            OperationParameterGuiElement paramComp;
            switch(descriptor.parameterType){
                case BLOCK_SELECTOR:
                    paramComp = new BlockSelectorComponent(descriptor.name);
                    break;
                case STRING:
                    paramComp = new StringOperationParameterComponent(descriptor.name);
                    break;
                //case INT:
                //    break;
                //case FLOAT:
                //    break;
                default:
                    continue;
            }
            this.opParameterComponents.put(descriptor, paramComp);
            opPanel.addComponent((Component)paramComp);
        }
        return opPanel;
    }
    
    private SelectBox<OperationType> createOpSelectBox(){
        SelectBox<OperationType> opBox = new SelectBox<>();
        for(OperationType opType : OperationTypes.getOperationTypes()){
            opBox.addElement(opType);
        }
        opBox.setSelected(0, true);
        opBox.getSelectBoxChangeSelectionEvents().add((EventListener<SelectBoxChangeSelectionEvent<OperationType>>)(event) -> {
            updateOperationPanel();
        });
        opBox.setSize(100, 30);
        opBox.getStyle().setTop(15f);
        opBox.getStyle().setLeft(15f);
        opBox.getStyle().setHeight(30f);
        opBox.getStyle().setWidth(100f);
        opBox.getStyle().setMargin(20f, 20f);
        return opBox;
    }
    
    private Button createDoOpButton(){
        Button button = new Button();
        button.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                doOperation(operationsSelectBox.getSelection());
            }
        });
        button.getTextState().setText(DO_OP_TEXT);
        button.setSize(100, 30);
        button.getStyle().setTop(15f);
        button.getStyle().setLeft(15f + 100f + 15f);
        button.getStyle().setHeight(30f);
        button.getStyle().setWidth(100f);
        button.getStyle().setMargin(20f, 20f);
        return button;
    }
    
    private void doOperation(OperationType opType){
        OperationParameters params = new OperationParameters();
        for(OperationParameterDescriptor descriptor : this.opParameterComponents.keySet()){
            OperationParameterGuiElement paramComp = this.opParameterComponents.get(descriptor);
            params.setParam(descriptor, paramComp.getInputValue());
        }
        OperationResult result = editor.doOperation(opType, params);
        Logger.info("Operation Result: " + result);
    }
    
}
