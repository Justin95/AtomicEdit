
package atomicedit.frontend.render.blockmodelcreation;

import atomicedit.settings.SettingSelectableClass;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public interface BlockModelCreatorLogic extends SettingSelectableClass{
    
    public void addBlockRenderData(int x, int y, int z, ChunkSectionPlus section, List<Float> vertexData, List<Integer> indicies, boolean includeTranslucent);
    
}
