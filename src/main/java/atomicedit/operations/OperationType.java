
package atomicedit.operations;

import atomicedit.backend.BlockCoord;
import atomicedit.operations.utils.OperationParameterDescriptor;
import atomicedit.operations.utils.OperationParameters;
import atomicedit.volumes.Volume;
import java.util.List;

/**
 * Describe a type of operation in terms of what parameters it requires and
 * provide a way to instantiate it. All implementing classes should be added
 * to OperationTypes.java.
 * @author Justin Bonner
 */
public interface OperationType {
    
    /**
     * Get the description of the parameters that an operation of this operation type will need to run.
     * @return 
     */
    public List<OperationParameterDescriptor> getOperationParameterDescription();
    
    /**
     * Create an operation of this operation type.
     * @param volume the volume the operation will work over
     * @param smallestCoord the smallest coordinate in the volume, this gives the volume a specific world location
     * @param parameters the operation parameters for the operation, should match what this OperationType describes.
     * @return a ready to run operation
     */
    public Operation getOperationInstance(Volume volume, BlockCoord smallestCoord, OperationParameters parameters);
    
    /**
     * Get the string used to display this operation in the UI.
     * @return 
     */
    public String getOperationName();
    
}
