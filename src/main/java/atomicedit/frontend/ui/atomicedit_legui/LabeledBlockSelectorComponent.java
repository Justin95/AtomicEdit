
package atomicedit.frontend.ui.atomicedit_legui;

import atomicedit.backend.BlockState;
import atomicedit.backend.GlobalBlockStateMap;
import atomicedit.frontend.ui.OperationParameterComponent;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.component.SelectBox;

/**
 *
 * @author Justin Bonner
 */
public class LabeledBlockSelectorComponent extends OperationParameterComponent {
    
    private final Label nameLabel;
    private final SelectBox<BlockState> blockSelect;
    
    public LabeledBlockSelectorComponent(String labelName){ //Change this to be one selector for block type name, and choose properties another way
        super();
        this.blockSelect = new SelectBox<>();
        this.nameLabel = new Label();
        BlockState b = BlockState.AIR; //TODO fix this, this just force loads class
        GlobalBlockStateMap.getBlockTypes().forEach((blockState) -> blockSelect.addElement(blockState)); //alphabetize later
        this.blockSelect.setPosition(90, 0);
        this.blockSelect.setSize(200, 30);
        this.nameLabel.getTextState().setText(labelName);
        this.nameLabel.setPosition(0, 0);
        this.nameLabel.setSize(80, 30);
        this.nameLabel.getTextState().setFontSize(20);
        this.nameLabel.getTextState().setTextColor(1, 1, 1, 1);
        this.add(blockSelect);
        this.add(nameLabel);
        this.setSize(280, 30);
        this.getStyle().setHeight(30f);
        this.getStyle().setWidth(290f);
        this.getStyle().setLeft(10f);
        this.getStyle().setRight(10f);
        this.setFocusable(false);
        this.getStyle().getBackground().setColor(0, 0, 0, 1);
    }
    
    @Override
    public Object getInputValue(){
        return blockSelect.getSelection();
    }
    
}
