
package atomicedit.backend.brushes;

import atomicedit.backend.parameters.Parameters;
import atomicedit.volumes.Volume;

/**
 *
 * @author justin
 */
public interface Brush {
    
    //change this to support random noise in the brush
    Volume getVolume(Parameters brushParameters);
    
}
