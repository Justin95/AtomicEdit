
package atomicedit.frontend.ui.editormenu;

import atomicedit.frontend.editor.EditorType;
import org.liquidengine.legui.component.Panel;
import org.liquidengine.legui.component.RadioButtonGroup;
import org.liquidengine.legui.style.Style;
import org.liquidengine.legui.style.flex.FlexStyle;

/**
 *
 * @author Justin Bonner
 */
public class EditorTypesMenu extends Panel{
    
    private static final float BOTTOM_PADDING = 30;
    private static final float INTERNAL_PADDING = 10;
    private EditorTypeSelectorButton[] contents;
    private RadioButtonGroup buttonGroup = new RadioButtonGroup();
    
    public EditorTypesMenu(){
        super();
        this.contents = getContents();
        for(int i = 0; i < contents.length; i++){
            contents[i].setRadioButtonGroup(buttonGroup);
            contents[i].setChecked(false);
            this.add(contents[i]);
        }
        contents[0].setChecked(true);
        contents[0].onSelection(); //select first button by default, and call its 'on selection' method
        layoutContents();
        this.getStyle().getBackground().setColor(.1f, .1f, .1f, .5f);
        this.getStyle().setWidth(INTERNAL_PADDING + (contents.length * (EditorTypeSelectorButton.BUTTON_WIDTH + INTERNAL_PADDING)));
        this.getStyle().setHeight(70f);
        this.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.FLEX_END);
        this.getStyle().setPosition(Style.PositionType.ABSOLUTE);
        this.getStyle().setBottom(BOTTOM_PADDING);
        this.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.CENTER);
        this.setFocusable(false);
    }
    
    
    private void layoutContents(){
        for(int i = 0; i < contents.length; i++){
            contents[i].setPosition(INTERNAL_PADDING + (i * (EditorTypeSelectorButton.BUTTON_WIDTH + INTERNAL_PADDING)), INTERNAL_PADDING);
        }
    }
    
    private EditorTypeSelectorButton[] getContents(){
        return new EditorTypeSelectorButton[]{
            new EditorTypeSelectorButton(
                EditorType.AREA_SELECTION,
                "icons/area_select_icon.png",
                "icons/area_select_icon.png"//"icons/area_select_icon_selected.png"
            ),
            new EditorTypeSelectorButton(
                EditorType.BRUSH_ACTION,
                "icons/NBT_icon.png",//temp use nbt icon
                "icons/NBT_icon.png"
            )
        };
    }
    
    
}
