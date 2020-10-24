
package atomicedit.frontend.editor;

/**
 *
 * @author Justin Bonner
 */
public interface Editor {
    
    void initialize();
    
    void renderTick();
    
    void handleInput(boolean isUiFocused, int key, int action, int mods);
    
    void cleanUp();
    
}
