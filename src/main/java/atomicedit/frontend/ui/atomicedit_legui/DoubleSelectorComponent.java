
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
    private DoubleTextState dTextState;
    
    public DoubleSelectorComponent(double min, double max, double initialValue) {
        initialize(min, max, initialValue);
    }
    
    private void initialize(double min, double max, double initialValue) {
        this.dTextState = new DoubleTextState(min, max, initialValue, this);
        this.setTextState(dTextState);
        //can use text state validators as well
    }
    
    public double getValue() {
        return this.dTextState.value;
    }
    
    public void setValueChangeCallback(ValueSetCallback callback) {
        this.valueSetCallback = callback;
    }
    
    private static class DoubleTextState extends TextState {
        
        private final double min;
        private final double max;
        private final DoubleSelectorComponent parent;
        private double value;
        
        DoubleTextState(double min, double max, double initialValue, DoubleSelectorComponent parent) {
            super();
            this.parent = parent;
            this.min = min;
            this.max = max;
            this.value = initialValue;
            this.setText(Double.toString(initialValue));
        }
        
        @Override
        public void setText(String text) {
            if (!NUMBER_PATTERN.matcher(text).matches()) {
                super.setText(text);
                return; //invalid input
            }
            double newValue;
            try {
                newValue = Double.parseDouble(text);
            } catch (NumberFormatException e) {
                super.setText(text);
                return; //invalid input
            }
            if (newValue > max) {
                newValue = max;
            } else if (newValue < min) {
                newValue = min;
            }
            this.value = newValue;
            if (parent != null && parent.valueSetCallback != null) {
                parent.valueSetCallback.valueSetCallback(newValue);
            }
            super.setText(Double.toString(newValue));
        }
        
    }
    
    public interface ValueSetCallback {
        void valueSetCallback(double newValue);
    }
    
}
