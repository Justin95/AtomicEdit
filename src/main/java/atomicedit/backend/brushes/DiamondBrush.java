
package atomicedit.backend.brushes;

import atomicedit.backend.parameters.IntegerParameterDescriptor;
import atomicedit.backend.parameters.ParameterDescriptor;
import atomicedit.backend.parameters.Parameters;
import atomicedit.backend.utils.BitArray;
import atomicedit.backend.utils.GeneralUtils;
import atomicedit.volumes.Box;
import atomicedit.volumes.Volume;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.joml.Vector3d;

/**
 *
 * @author justin
 */
public class DiamondBrush implements Brush {
    
    private static final IntegerParameterDescriptor X_RADIUS_PARAM_DESC = new IntegerParameterDescriptor("X Radius", 4, 0, 200);
    private static final IntegerParameterDescriptor Y_RADIUS_PARAM_DESC = new IntegerParameterDescriptor("Y Radius", 4, 0, 200);
    private static final IntegerParameterDescriptor Z_RADIUS_PARAM_DESC = new IntegerParameterDescriptor("Z Radius", 4, 0, 200);
    static final List<ParameterDescriptor> PARAM_DESCRIPTORS = Collections.unmodifiableList(Arrays.asList(
        X_RADIUS_PARAM_DESC,
        Y_RADIUS_PARAM_DESC,
        Z_RADIUS_PARAM_DESC
    ));
    private static final DiamondBrush INSTANCE = new DiamondBrush();
    
    private DiamondBrush() {
        
    }
    
    public static DiamondBrush getInstance() {
        return INSTANCE;
    }
    
    @Override
    public Volume getVolume(Parameters brushParameters) {
        int xRadius = brushParameters.getParamAsInteger(X_RADIUS_PARAM_DESC);
        int yRadius = brushParameters.getParamAsInteger(Y_RADIUS_PARAM_DESC);
        int zRadius = brushParameters.getParamAsInteger(Z_RADIUS_PARAM_DESC);
        int xLen = 1 + 2 * xRadius;
        int yLen = 1 + 2 * yRadius;
        int zLen = 1 + 2 * zRadius;
        BitArray includedSet = new BitArray(xLen * yLen * zLen, false);
        int x;
        int y;
        int z;
        Vector3d center = new Vector3d(xLen / 2.0 - .5, yLen / 2.0 - .5, zLen / 2.0 - .5);
        for(int index = 0; index < xLen * yLen * zLen; index++){
            x = GeneralUtils.getXFromIndexYZX(index, xLen);
            y = GeneralUtils.getYFromIndexYZX(index, xLen, zLen);
            z = GeneralUtils.getZFromIndexYZX(index, xLen, zLen);
            boolean inShape = 1.00001 >= //use 1.00001 instead of 1 to account for small rounding errors
                      (Math.abs(x - center.x) / xRadius) 
                    + (Math.abs(y - center.y) / yRadius)
                    + (Math.abs(z - center.z) / zRadius);
            includedSet.set(index, inShape);
        }
        return new Volume(new Box(xLen, yLen, zLen), includedSet);
    }
    
}
