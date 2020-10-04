
package atomicedit.operations;

import atomicedit.operations.implementations.ReplaceBlocksOperation;
import atomicedit.operations.implementations.SetBlocksOperation;
import atomicedit.operations.utils.OperationParameterDescriptor;
import atomicedit.operations.utils.OperationParameters;
import atomicedit.volumes.WorldVolume;
import java.util.List;

/**
 * Describe a type of operation in terms of what parameters it requires and
 * provide a way to instantiate it. All implementing classes should be added
 * to this enum.
 * @author Justin Bonner
 */
public enum OperationType {
    SET_BLOCKS_OPERATION(
        "Set Blocks",
        SetBlocksOperation.PARAM_DESCRIPTORS,
        SetBlocksOperation::getInstance
    ),
    REPLACE_BLOCKS_OPERATION(
        "Replace Blocks",
        ReplaceBlocksOperation.PARAM_DESCRIPTORS,
        ReplaceBlocksOperation::getInstance
    ),
    ;
    
    private final String displayName;
    private final List<OperationParameterDescriptor> parameterDescription;
    private final OperationInstanceCreator opCreator;
    
    OperationType(String displayName, List<OperationParameterDescriptor> opParamDescriptors, OperationInstanceCreator opCreator) {
        this.displayName = displayName;
        this.parameterDescription = opParamDescriptors;
        this.opCreator = opCreator;
    }
    
    /**
     * Get the description of the parameters that an operation of this operation type will need to run.
     * @return 
     */
    public List<OperationParameterDescriptor> getOperationParameterDescription() {
        return parameterDescription;
    }
    
    /**
     * Create an operation of this operation type.
     * @param volume the volume the operation will work over
     * @param parameters the operation parameters for the operation, should match what this OperationType describes.
     * @return a ready to run operation
     */
    public Operation getOperationInstance(WorldVolume volume, OperationParameters parameters) {
        return opCreator.createInstance(volume, parameters);
    }
    
    /**
     * Get the string used to display this operation in the UI.
     * @return 
     */
    public String getOperationName() {
        return displayName;
    }
    
    private interface OperationInstanceCreator {
        Operation createInstance(WorldVolume volume, OperationParameters parameters);
    }
    
}
