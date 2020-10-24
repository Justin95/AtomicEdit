
package atomicedit.backend.brushes;

import atomicedit.backend.parameters.ParameterDescriptor;
import java.util.List;

/**
 *
 * @author justin
 */
public enum BrushType {
    ELIPSE(
            "Elipse",
            ElipseBrush.PARAM_DESCRIPTORS,
            ElipseBrush::getInstance
    ),
    
    ;
    
    public final String displayName;
    private final List<ParameterDescriptor> paramDescriptors;
    private final BrushConstructor instanceCreator;
    
    BrushType(String displayName, List<ParameterDescriptor> paramDescriptors, BrushConstructor instanceCreator) {
        this.displayName = displayName;
        this.paramDescriptors = paramDescriptors;
        this.instanceCreator = instanceCreator;
    }
    
    public Brush createInstance() {
        return this.instanceCreator.createInstance();
    }
    
    public List<ParameterDescriptor> getParameterDescriptors() {
        return this.paramDescriptors;
    }
    
    private interface BrushConstructor {
        Brush createInstance();
    }
    
    @Override
    public String toString() {
        return this.displayName;
    }
    
}
