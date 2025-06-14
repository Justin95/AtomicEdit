
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
    DIAMOND(
        "Diamond",
        DiamondBrush.PARAM_DESCRIPTORS,
        DiamondBrush::getInstance
    ),
    SHELL(
        "Shell",
        ShellBrush.PARAM_DESCRIPTORS,
        ShellBrush::getInstance
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
    
    public static BrushType fromName(String displayName) {
        for (BrushType brush : values()) {
            if (brush.displayName.equals(displayName)) {
                return brush;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return this.displayName;
    }
    
}
