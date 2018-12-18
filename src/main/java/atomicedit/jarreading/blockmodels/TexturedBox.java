
package atomicedit.jarreading.blockmodels;

import java.util.EnumMap;
import java.util.Map;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class TexturedBox {
    
    private static final Vector3f BLOCK_CENTER = new Vector3f(.5f, .5f, .5f);
    private static final float NUM_BOX_UNITS_IN_BLOCK = 16f;
    public final Vector3f smallPos;
    public final Vector3f largePos;
    public final Map<CubeFace, Face> faces;
    public final boolean useShade;
    public final Vector3f rotateAbout;
    public final Vector3f rotation;
    
    
    TexturedBox(TexturedBoxPrecursor precursor, Vector3f blockRotation){
        this.smallPos = new Vector3f(precursor.smallCorner).div(NUM_BOX_UNITS_IN_BLOCK);
        this.largePos = new Vector3f(precursor.largeCorner).div(NUM_BOX_UNITS_IN_BLOCK);
        this.faces = new EnumMap<>(CubeFace.class);
        for(CubeFace face : precursor.faces.keySet()){
            faces.put(calcRotatedFace(face, blockRotation), new Face(precursor.faces.get(face)));
        }
        this.useShade = precursor.useShade;
        this.rotateAbout = precursor.rotateAbout;
        this.rotation = precursor.rotation;
    }
    
    /**
     * Calculate the replacements from rotating the box in the specified way.
     * @param startFace
     * @param blockRot
     * @return 
     */
    private static CubeFace calcRotatedFace(CubeFace startFace, Vector3f blockRot){
        CubeFace currFace = startFace;
        float xRot = blockRot.x % 360; //in degrees
        
        return currFace;
    }
    
    
}
