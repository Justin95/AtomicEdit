
package atomicedit.frontend.render.blockmodelcreation;

import atomicedit.utils.FloatList;
import atomicedit.utils.IntList;

/**
 *
 * @author Justin Bonner
 */
public interface BlockModelCreatorLogic {
    
    void addBlockRenderData(int x, int y, int z, BlockVolumeDataProvider section, FloatList vertexData, IntList indicies, boolean includeTranslucent);
    
}
