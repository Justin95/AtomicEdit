
package atomicedit.operations;

import atomicedit.operations.implementations.*;
import java.util.Arrays;
import java.util.List;

/**
 * Store all Operation Types that AtomicEdit can use.
 * @author Justin Bonner
 */
public class OperationTypes {
    
    /**
     * In order for an operation type to be available for use it needs to be listed
     * in this array.
     */
    private static final OperationType[] OPERATION_TYPES = {
        SetBlocksOperationType.getInstance(),
        ReplaceBlocksOperationType.getInstance()
    };
    
    public static List<OperationType> getOperationTypes(){
        return Arrays.asList(OPERATION_TYPES);
    }
    
}
