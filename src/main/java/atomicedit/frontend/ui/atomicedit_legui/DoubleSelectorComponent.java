
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
    
    private ValueSetCallback valueSetCallback;
    
    public DoubleSelectorComponent(double min, double max, double initialValue) {
        initialize(min, max, initialValue);
    }
    
    private void initialize(double min, double max, double initialValue) {
        this.setTextState(new DoubleTextState(min, max, initialValue, this));
        //can use text state validators as well
    }
    
    public double getValue() {
        return Long.parseLong(this.textState.getText());
    }
    
    public void setValueChangeCallback(ValueSetCallback callback) {
        this.valueSetCallback = callback;
    }
    
    private static class DoubleTextState extends TextState {
        
        private final double min;
        private final double max;
        private final DoubleSelectorComponent parent;
        
        DoubleTextState(double min, double max, double initialValue, DoubleSelectorComponent parent) {
            super();
            this.parent = parent;
            this.min = min;
            this.max = max;
            this.setText(Double.toString(initialValue));
        }
        
        @Override
        public void setText(String text) {
            if (text.isEmpty()) {
                text = "0";
            }
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
            if (parent != null && parent.valueSetCallback != null) {
                parent.valueSetCallback.valueSetCallback(value);
            }
            super.setText(Double.toString(value));
        }
        
    }
    
    public interface ValueSetCallback {
        void valueSetCallback(double newValue);
    }
    
}
