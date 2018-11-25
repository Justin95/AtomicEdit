
package atomicedit.settings;

/**
 *
 * @author Justin Bonner
 */
public interface SettingSelectableClass {
    
    public default String getIdentifierString(){ //This can't be final but it would be if it could be, do not override
        return this.getClass().getSimpleName();
    }
    
}
