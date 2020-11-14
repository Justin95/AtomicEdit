
package atomicedit.operations;

import atomicedit.operations.implementations.ReplaceBlocksOperation;
import atomicedit.operations.implementations.SetBlocksOperation;
import atomicedit.backend.parameters.ParameterDescriptor;
import atomicedit.backend.parameters.Parameters;
import atomicedit.operations.implementations.ErosionOperation;
import atomicedit.operations.implementations.NoiseOperation;
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
    EROSION_OPERATION(
        "Erode Blocks",
        ErosionOperation.PARAM_DESCRIPTORS,
        ErosionOperation::getInstance
    ),
    NOISE_OPERATION(
        "Noise Set",
        NoiseOperation.PARAM_DESCRIPTORS,
        NoiseOperation::getInstance
    ),
    ;
    
    private final String displayName;
    private final List<ParameterDescriptor> parameterDescription;
    private final OperationInstanceCreator opCreator;
    
    OperationType(String displayName, List<ParameterDescriptor> opParamDescriptors, OperationInstanceCreator opCreator) {
        this.displayName = displayName;
        this.parameterDescription = opParamDescriptors;
        this.opCreator = opCreator;
    }
    
    /**
     * Get the description of the parameters that an operation of this operation type will need to run.
     * @return 
     */
    public List<ParameterDescriptor> getOperationParameterDescription() {
        return parameterDescription;
    }
    
    /**
     * Create an operation of this operation type.
     * @param volume the volume the operation will work over
     * @param parameters the operation parameters for the operation, should match what this OperationType describes.
     * @return a ready to run operation
     */
    public Operation getOperationInstance(WorldVolume volume, Parameters parameters) {
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
        Operation createInstance(WorldVolume volume, Parameters parameters);
    }
    
    //toString is used in the GUI for the display name
    @Override
    public String toString() {
        return this.displayName;
    }
    
}
