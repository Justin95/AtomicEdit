
package atomicedit.jarreading.blockmodels;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
class TexturedBoxPrecursor {
    
    Vector3f smallCorner;
    Vector3f largeCorner;
    Map<CubeFace, TexturedFacePrecursor> faces;
    boolean useShade;
    Vector3f rotateAbout;
    Vector3f rotation;
    
    TexturedBoxPrecursor(){
        this.faces = new EnumMap<>(CubeFace.class);
        useShade = true;
        rotateAbout = new Vector3f(0,0,0);
        rotation = new Vector3f(0,0,0);
    }
    
    public List<String> getTextureNames(){
        ArrayList<String> names = new ArrayList<>();
        faces.values().forEach(face -> names.add(face.textureName));
        return names;
    }
    
    @Override
    public String toString(){
        return getTextureNames().toString();
    }
    
    public TexturedBoxPrecursor copy(){
        TexturedBoxPrecursor newBox = new TexturedBoxPrecursor();
        newBox.smallCorner = new Vector3f(smallCorner);
        newBox.largeCorner = new Vector3f(largeCorner);
        newBox.rotateAbout = new Vector3f(this.rotateAbout);
        newBox.rotation = new Vector3f(this.rotation);
        for(CubeFace face : faces.keySet()){
            newBox.faces.put(face, faces.get(face).copy());
        }
        return newBox;
    }
    
}
