
package atomicedit.frontend.ui.atomicedit_legui;

import atomicedit.frontend.ui.UserSuppliedParameterComponent;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.component.TextInput;

/**
 *
 * @author Justin Bonner
 */
public class StringOperationParameterComponent extends UserSuppliedParameterComponent {
    
    private static final float INPUT_FONT_SIZE = 20;
    private static final float LABEL_FONT_SIZE = 20;
    private static final int LABEL_WIDTH = 50;
    private static final int LABEL_HEIGHT = 25;
    private static final int INPUT_WIDTH = 100;
    private static final int INPUT_HEIGHT = 25;
    private static final int BUFFER_WIDTH = 5;
    private static final int TOTAL_WIDTH = LABEL_WIDTH + BUFFER_WIDTH + INPUT_WIDTH;
    private static final int TOTAL_HEIGHT = LABEL_HEIGHT > INPUT_HEIGHT ? LABEL_HEIGHT : INPUT_HEIGHT;
    
    private Label label;
    private TextInput textInput;
    
    public StringOperationParameterComponent(String labelText){
        this.setPosition(0, 0);
        this.setSize(TOTAL_WIDTH, TOTAL_HEIGHT);
        this.label = new Label(0,0, LABEL_WIDTH, LABEL_HEIGHT);
        this.label.getTextState().setText(labelText);
        this.label.getStyle().setFontSize(LABEL_FONT_SIZE);
        this.textInput = new TextInput(LABEL_WIDTH + BUFFER_WIDTH,0, INPUT_WIDTH, INPUT_HEIGHT);
        this.textInput.getStyle().setFontSize(INPUT_FONT_SIZE);
        this.getStyle().getBackground().setColor(0, 0, 0, 0); //fully transparent panel
        this.add(label);
        this.add(textInput);
    }
    
    @Override
    public String getInputValue(){
        return textInput.getSelection();
    }
    
    
}
