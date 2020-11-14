
package atomicedit.frontend.ui.atomicedit_legui;

import java.util.regex.Pattern;
import org.liquidengine.legui.component.TextInput;
import org.liquidengine.legui.component.optional.TextState;

/**
 *
 * @author Justin Bonner
 */
public class IntegerSelectorComponent extends TextInput {
    
    private static final Pattern INT_PATTERN = Pattern.compile("-?\\d+");
    public static final int RECOMMENDED_WIDTH = 150;
    
    private ValueSetCallback valueSetCallback;
    
    public IntegerSelectorComponent(long min, long max, long initialValue) {
        initialize(min, max, initialValue);
    }
    
    private void initialize(long min, long max, long initialValue) {
        this.setTextState(new IntegerTextState(min, max, initialValue, this));
        //can use text state validators as well
    }
    
    public long getValue() {
        return Long.parseLong(this.textState.getText());
    }
    
    public void setValueChangeCallback(ValueSetCallback callback) {
        this.valueSetCallback = callback;
    }
    
    private static class IntegerTextState extends TextState {
        
        private final long min;
        private final long max;
        private final IntegerSelectorComponent parent;
        
        IntegerTextState(long min, long max, long initialValue, IntegerSelectorComponent parent) {
            super();
            this.min = min;
            this.max = max;
            this.parent = parent;
            setText(Long.toString(initialValue));
        }
        
        @Override
        public void setText(String text) {
            if (text.isEmpty()) {
                text = "0";
            }
            if (!INT_PATTERN.matcher(text).matches()) {
                return; //invalid input
            }
            long value;
            try {
                value = Long.parseLong(text);
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
            super.setText(Long.toString(value));
        }
        
    }
    
    public interface ValueSetCallback {
        void valueSetCallback(long newValue);
    }
    
}
