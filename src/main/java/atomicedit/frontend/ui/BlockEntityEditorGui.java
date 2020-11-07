
package atomicedit.frontend.ui;

import atomicedit.frontend.editor.BlockEntityEditor;
import java.util.ArrayList;
import java.util.List;
import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.Component;
import org.liquidengine.legui.component.Panel;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.style.Style;
import org.liquidengine.legui.style.flex.FlexStyle;

/**
 *
 * @author Justin Bonner
 */
public class BlockEntityEditorGui {
    
    private static final int GUI_WIDTH = 350;
    private static final int GUI_HEIGHT = 800;
    
    private final BlockEntityEditor editor;
    private Panel opPanel;
    private NbtEditorWidget editorWidget;
    
    public BlockEntityEditorGui(BlockEntityEditor editor) {
        this.editor = editor;
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
        //padding
        this.opPanel.getStyle().setPadding(30, 10, 30, 10); //top, right, bottom, left
        
        //Edit Button
        Button editButton = new Button();
        editButton.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                openEditor();
            }
        });
        editButton.getTextState().setText("Edit NBT");
        editButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        editButton.getStyle().setMinHeight(30f);
        editButton.getStyle().setMinWidth(100f);
        this.opPanel.add(editButton);
    }
    
    private void openEditor() {
        NbtEditorWidget widget = editor.createEditorWidget();
        if (widget != null) {
            this.editorWidget = widget;
        }
    }
    
    public Panel getOpPanel() {
        return this.opPanel;
    }
    
    /**
     * Get all the Components used by this GUI. Useful for ensuring this GUI is cleaned up properly.
     * @return 
     */
    public List<Component> getAllActiveComponents() {
        List<Component> comps = new ArrayList<>();
        comps.add(opPanel);
        if (editorWidget != null) {
            comps.add(editorWidget);
        }
        return comps;
    }
    
}
