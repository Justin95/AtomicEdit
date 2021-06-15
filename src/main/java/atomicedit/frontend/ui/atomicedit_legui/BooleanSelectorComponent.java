
package atomicedit.frontend.ui.atomicedit_legui;

import org.liquidengine.legui.component.CheckBox;

/**
 *
 * @author Justin Bonner
 */
public class BooleanSelectorComponent extends CheckBox {
    
    
    public BooleanSelectorComponent(boolean initialValue) {
        this.textState.setText("");
        this.setEnabled(true);
        this.setChecked(initialValue);
    }
    
    public boolean getValue() {
        return this.isChecked();
    }
    
    public void setValueChangeCallback(ValueSetCallback callback) {
        this.addCheckBoxChangeValueListener((event) -> callback.valueSetCallback(event.getNewValue()));
    }
    
    
    public interface ValueSetCallback {
        void valueSetCallback(boolean newValue);
    }
    
}
