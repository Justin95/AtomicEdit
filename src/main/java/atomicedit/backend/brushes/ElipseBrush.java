
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
import org.joml.Vector3f;

/**
 *
 * @author justin
 */
public class ElipseBrush implements Brush {
    
    private static final IntegerParameterDescriptor X_RADIUS_PARAM_DESC = new IntegerParameterDescriptor("X Radius", 4, 0, 200);
    private static final IntegerParameterDescriptor Y_RADIUS_PARAM_DESC = new IntegerParameterDescriptor("Y Radius", 4, 0, 200);
    private static final IntegerParameterDescriptor Z_RADIUS_PARAM_DESC = new IntegerParameterDescriptor("Z Radius", 4, 0, 200);
    static final List<ParameterDescriptor> PARAM_DESCRIPTORS = Collections.unmodifiableList(Arrays.asList(
        X_RADIUS_PARAM_DESC,
        Y_RADIUS_PARAM_DESC,
        Z_RADIUS_PARAM_DESC
    ));
    private static final ElipseBrush INSTANCE = new ElipseBrush();
    
    private ElipseBrush() {
        
    }
    
    public static ElipseBrush getInstance() {
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
        Vector3f center = new Vector3f(xLen / 2f, yLen / 2f, zLen / 2f);
        double xRadiusSquared = Math.pow(xRadius, 2);
        double yRadiusSquared = Math.pow(yRadius, 2);
        double zRadiusSquared = Math.pow(zRadius, 2);
        for(int index = 0; index < xLen * yLen * zLen; index++){
            x = GeneralUtils.getXFromIndexYZX(index, xLen);
            y = GeneralUtils.getYFromIndexYZX(index, xLen, zLen);
            z = GeneralUtils.getZFromIndexYZX(index, xLen, zLen);
            boolean inElipse = 1 >=
                      (Math.pow(x - center.x, 2) / xRadiusSquared) 
                    + (Math.pow(y - center.y, 2) / yRadiusSquared)
                    + (Math.pow(z - center.z, 2) / zRadiusSquared);
            includedSet.set(index, inElipse);
        }
        return new Volume(new Box(xLen, yLen, zLen), includedSet);
    }
    
}
