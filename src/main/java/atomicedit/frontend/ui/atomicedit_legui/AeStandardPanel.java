
package atomicedit.frontend.ui.atomicedit_legui;

import org.liquidengine.legui.component.Component;
import org.liquidengine.legui.component.ScrollablePanel;
import org.liquidengine.legui.style.flex.FlexStyle;

/**
 *
 * @author Justin Bonner
 */
public class AeStandardPanel extends ScrollablePanel {
    
    //https://github.com/SpinyOwl/legui/blob/develop/src/main/java/org/liquidengine/legui/demo/layout/MenuLayerDemo.java
    
    public AeStandardPanel(){
        this.getStyle().getFlexStyle().setAlignSelf(FlexStyle.AlignSelf.AUTO);
        this.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
        this.setFocusable(false);
        this.getStyle().getBackground().setColor(0,0,0,0);
        this.getViewport().setFocusable(false);
        this.getViewport().getStyle().getBackground().setColor(0,0,0,0); //transparent
        this.setHorizontalScrollBarVisible(false);
    }
    
    
    public void addComponent(Component comp){
        this.add(comp);
    }
    
}
