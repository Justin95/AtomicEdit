
package atomicedit.backend.parameters;

/**
 *
 * @author Justin Bonner
 */
public enum ParameterType {
    //make sure there is a getter in Operation Parameters for the needed type
    STRING(),
    INT(),
    FLOAT(),
    BLOCK_SELECTOR()
    ;
    
    //reference to legui component subclass for each type, in frontend.ui package, these will have a getValue returning the correct type
    //dont need to store expected parameter value type in this enum.
    
    
    
}
