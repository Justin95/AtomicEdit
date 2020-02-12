
package atomicedit.frontend.ui.atomicedit_legui;

import org.liquidengine.legui.component.Component;
import org.liquidengine.legui.component.ScrollablePanel;
import org.liquidengine.legui.style.flex.FlexStyle;

/**
 * https://github.com/SpinyOwl/legui/blob/develop/src/main/java/org/liquidengine/legui/demo/layout/MenuLayerDemo.java
 * @author Justin Bonner
 */
public class ComponentListPanel extends ScrollablePanel {
    
    private static final int BUFFER_WIDTH = 10;
    
    private float addCompYOffset;
    
    public ComponentListPanel(){
        this.getStyle().getFlexStyle().setAlignSelf(FlexStyle.AlignSelf.AUTO);
        this.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
        this.setFocusable(false);
        this.getStyle().getBackground().setColor(0,0,0,0);
        this.getViewport().setFocusable(false);
        this.getViewport().getStyle().getBackground().setColor(0,0,0,0); //transparent
        this.setHorizontalScrollBarVisible(false);
        this.addCompYOffset = BUFFER_WIDTH;
    }
    
    
    public void addComponent(Component comp){
        comp.getStyle().setTop(comp.getPosition().y + addCompYOffset);
        addCompYOffset += BUFFER_WIDTH + comp.getSize().y;
        this.add(comp);
    }
    
}
