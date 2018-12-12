
package atomicedit.jarreading.blockmodels;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
class TexturedBoxPrecursor {
    
    float smallX;
    float smallY;
    float smallZ;
    float largeX;
    float largeY;
    float largeZ;
    
    /**
     * Up
     */
    String yPlusTexName; //up
    /**
     * Down
     */
    String yMinusTexName; //down
    /**
     * East
     */
    String xPlusTexName; //east
    /**
     * West
     */
    String xMinusTexName; //west
    /**
     * South
     */
    String zPlusTexName; //south
    /**
     * North
     */
    String zMinusTexName; //north
    
    
    TexturedBoxPrecursor(){
        
    }
    
    public List<String> getTextureNames(){
        ArrayList<String> names = new ArrayList();
        names.add(yPlusTexName);
        names.add(yMinusTexName);
        names.add(xPlusTexName);
        names.add(xMinusTexName);
        names.add(zPlusTexName);
        names.add(zMinusTexName);
        return names;
    }
    
    @Override
    public String toString(){
        return getTextureNames().toString();
    }
    
    public TexturedBoxPrecursor copy(){
        TexturedBoxPrecursor newBox = new TexturedBoxPrecursor();
        newBox.largeX = largeX;
        newBox.largeY = largeY;
        newBox.largeZ = largeZ;
        newBox.smallX = smallX;
        newBox.smallY = smallY;
        newBox.smallZ = smallZ;
        newBox.xMinusTexName = xMinusTexName;
        newBox.xPlusTexName = xPlusTexName;
        newBox.yMinusTexName = yMinusTexName;
        newBox.yPlusTexName = yPlusTexName;
        newBox.zMinusTexName = zMinusTexName;
        newBox.zPlusTexName = zPlusTexName;
        return newBox;
    }
    
}
