
package atomicedit.frontend.editor;

import atomicedit.operations.OperationResult;
import atomicedit.operations.OperationType;
import atomicedit.operations.utils.OperationParameters;

/**
 *
 * @author Justin Bonner
 */
public interface Editor {
    
    public void initialize();
    
    public void renderTick();
    
    public void handleInput(boolean isUiFocused, int key, int action, int mods);
    
    public OperationResult doOperation(OperationType opType, OperationParameters params);
    
    public void destory();
    
}
