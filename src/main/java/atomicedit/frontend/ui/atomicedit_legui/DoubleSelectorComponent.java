
package atomicedit.frontend.ui.atomicedit_legui;

import java.util.regex.Pattern;
import org.liquidengine.legui.component.TextInput;
import org.liquidengine.legui.component.optional.TextState;

/**
 *
 * @author Justin Bonner
 */
public class DoubleSelectorComponent extends TextInput {
    
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
    
    public DoubleSelectorComponent(double min, double max, double initialValue) {
        initialize(min, max, initialValue);
    }
    
    private void initialize(double min, double max, double initialValue) {
        this.setTextState(new IntegerTextState(min, max, initialValue));
        //can use text state validators as well
    }
    
    public double getValue() {
        return Long.parseLong(this.textState.getText());
    }
    
    private static class IntegerTextState extends TextState {
        
        private double min;
        private double max;
        
        IntegerTextState(double min, double max, double initialValue) {
            super(Double.toString(initialValue));
        }
        
        @Override
        public void setText(String text) {
            if (!NUMBER_PATTERN.matcher(text).matches()) {
                return; //invalid input
            }
            double value;
            try {
                value = Double.parseDouble(text);
            } catch (NumberFormatException e) {
                return; //invalid input
            }
            if (value > max) {
                value = max;
            } else if (value < min) {
                value = min;
            }
            super.setText(Double.toString(value));
        }
        
    }
    
}
