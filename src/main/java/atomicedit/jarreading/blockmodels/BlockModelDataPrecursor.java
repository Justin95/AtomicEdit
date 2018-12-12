
package atomicedit.jarreading.blockmodels;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
class BlockModelDataPrecursor {
    
    public static final String PARAM_IDENTIFIER = "#";
    
    private String parentName;
    private Map<String, BlockModelDataParameter> params;
    private List<TexturedBoxPrecursor> boxPrecursors;
    private boolean isFullBlock;
    
    BlockModelDataPrecursor(String parentName, Map<String, BlockModelDataParameter> params, List<TexturedBoxPrecursor> boxPrecursors, boolean isFullBlock){
        this.parentName = parentName;
        this.params = params;
        this.boxPrecursors = boxPrecursors;
        this.isFullBlock = isFullBlock;
    }

    public String getParentName() {
        return parentName;
    }

    public Map<String, BlockModelDataParameter> getParams() {
        return params;
    }

    public List<TexturedBoxPrecursor> getBoxPrecursors() {
        return boxPrecursors;
    }

    public boolean getIsFullBlock() {
        return isFullBlock;
    }
    
    
    
}
