
package atomicedit.settings;

/**
 *
 * @author Justin Bonner
 */
public class SettingOption {
    
    private Object value;
    
    SettingOption(Object value){
        this.value = value;
    }
    
    public Object getValue(){
        return this.value;
    }
    
}
