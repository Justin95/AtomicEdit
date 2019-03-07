
package atomicedit.frontend.ui.editormenu;

import atomicedit.frontend.editor.EditorSystem;
import atomicedit.frontend.editor.EditorType;
import org.joml.Vector2f;
import org.liquidengine.legui.component.RadioButton;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.icon.ImageIcon;
import org.liquidengine.legui.image.BufferedImage;

/**
 *
 * @author Justin Bonner
 */
public class EditorTypeSelectorButton extends RadioButton{
    
    protected static final int BUTTON_WIDTH = 50;
    private EditorType editorType;
    
    public EditorTypeSelectorButton(EditorType editorType, String unselectedIconPath, String selectedIconPath){
        this(editorType, loadIcon(unselectedIconPath), loadIcon(selectedIconPath));
    }
    
    public EditorTypeSelectorButton(EditorType editorType, ImageIcon unselectedIcon, ImageIcon selectedIcon){
        super(0,0, BUTTON_WIDTH, BUTTON_WIDTH);
        this.setSize(BUTTON_WIDTH, BUTTON_WIDTH);
        selectedIcon.setSize(new Vector2f(BUTTON_WIDTH, BUTTON_WIDTH));
        unselectedIcon.setSize(new Vector2f(BUTTON_WIDTH, BUTTON_WIDTH));
        this.setIconChecked(selectedIcon);
        this.setIconUnchecked(unselectedIcon);
        this.editorType = editorType;
        this.getListenerMap().addListener(MouseClickEvent.class, (event) -> {
            if(event.getAction() == MouseClickEvent.MouseClickAction.CLICK){
                onSelection();
            }
        });
        this.setFocusable(false);
    }
    
    public void onSelection(){
        EditorSystem.setEditorType(editorType);
    }
    
    private static ImageIcon loadIcon(String path){
        BufferedImage iconImage = new BufferedImage(path);
        ImageIcon icon = new ImageIcon(iconImage);
        return icon;
    }
    
    @Override
    public boolean isFocused(){ //turn off focusing on this button
        return false;
    }
    
}
