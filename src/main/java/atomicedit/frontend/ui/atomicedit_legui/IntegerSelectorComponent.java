
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
    
    public IntegerSelectorComponent(long min, long max, long initialValue) {
        initialize(min, max, initialValue);
    }
    
    private void initialize(long min, long max, long initialValue) {
        this.setTextState(new IntegerTextState(min, max, initialValue));
        //can use text state validators as well
    }
    
    public long getValue() {
        return Long.parseLong(this.textState.getText());
    }
    
    private static class IntegerTextState extends TextState {
        
        private final long min;
        private final long max;
        
        IntegerTextState(long min, long max, long initialValue) {
            super();
            this.min = min;
            this.max = max;
            setText(Long.toString(initialValue));
        }
        
        @Override
        public void setText(String text) {
            if (text.isEmpty()) {
                super.setText("0");
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
            
            super.setText(Long.toString(value));
        }
        
    }
    
}
