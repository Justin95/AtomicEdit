
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
    private IntegerTextState iTextState;
    
    public IntegerSelectorComponent(long min, long max, long initialValue) {
        initialize(min, max, initialValue);
    }
    
    private void initialize(long min, long max, long initialValue) {
        this.iTextState = new IntegerTextState(min, max, initialValue, this);
        this.setTextState(iTextState);
        //can use text state validators as well
    }
    
    public long getValue() {
        return iTextState.value;
    }
    
    public void setValueChangeCallback(ValueSetCallback callback) {
        this.valueSetCallback = callback;
    }
    
    private static class IntegerTextState extends TextState {
        
        private final long min;
        private final long max;
        private final IntegerSelectorComponent parent;
        private long value;
        
        IntegerTextState(long min, long max, long initialValue, IntegerSelectorComponent parent) {
            super();
            this.min = min;
            this.max = max;
            this.parent = parent;
            this.value = initialValue;
            setText(Long.toString(initialValue));
        }
        
        @Override
        public void setText(String text) {
            if (!INT_PATTERN.matcher(text).matches()) {
                super.setText(text); //invalid input
                return;
            }
            long newValue;
            try {
                newValue = Long.parseLong(text);
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
            super.setText(Long.toString(newValue));
        }
        
    }
    
    public interface ValueSetCallback {
        void valueSetCallback(long newValue);
    }
    
}
