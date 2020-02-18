
package atomicedit.frontend.render.blockmodelcreation;

import atomicedit.settings.SettingSelectableClass;
import atomicedit.utils.FloatList;
import atomicedit.utils.IntList;

/**
 *
 * @author Justin Bonner
 */
public interface BlockModelCreatorLogic extends SettingSelectableClass{
    
    void addBlockRenderData(int x, int y, int z, ChunkSectionPlus section, FloatList vertexData, IntList indicies, boolean includeTranslucent);
    
}
