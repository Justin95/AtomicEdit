
package atomicedit.jarreading.blockmodels;

import atomicedit.logging.Logger;
import java.util.EnumMap;
import java.util.Map;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class TexturedBox {
    
    private static final float NUM_BOX_UNITS_IN_BLOCK = 16f;
    public final Vector3f smallPos;
    public final Vector3f largePos;
    public final Map<CubeFace, TexturedFace> texFaces;
    public final Map<CubeFace, PositionedFace> posFaces;
    public final boolean useShade;
    
    
    TexturedBox(TexturedBoxPrecursor precursor, Vector3f blockRotation){
        this.smallPos = new Vector3f(precursor.smallCorner).div(NUM_BOX_UNITS_IN_BLOCK);
        this.largePos = new Vector3f(precursor.largeCorner).div(NUM_BOX_UNITS_IN_BLOCK);
        Vector3f rotateAbout = new Vector3f(precursor.rotateAbout).div(NUM_BOX_UNITS_IN_BLOCK);
        this.texFaces = new EnumMap<>(CubeFace.class);
        this.posFaces = new EnumMap<>(CubeFace.class);
        for(CubeFace face : precursor.faces.keySet()){
            CubeFace afterRotFace = calcRotatedFace(face, blockRotation);
            posFaces.put(afterRotFace, new PositionedFace(smallPos, largePos, blockRotation, precursor.rotation, rotateAbout, face));
            texFaces.put(afterRotFace, new TexturedFace(precursor.faces.get(face)));
        }
        this.useShade = precursor.useShade;
    }
    
    /**
     * Calculate the replacements from rotating the box in the specified way.
     * @param startFace
     * @param blockRot
     * @return 
     */
    private static CubeFace calcRotatedFace(CubeFace startFace, Vector3f blockRot){
        CubeFace currFace = startFace;
        CubeFace[] xRotFaces = new CubeFace[]{CubeFace.UP, CubeFace.NORTH, CubeFace.DOWN, CubeFace.SOUTH};
        CubeFace[] yRotFaces = new CubeFace[]{CubeFace.NORTH, CubeFace.EAST, CubeFace.SOUTH, CubeFace.WEST};
        CubeFace[] zRotFaces = new CubeFace[]{CubeFace.UP, CubeFace.EAST, CubeFace.DOWN, CubeFace.WEST};
        
        int index = getIndex(currFace, xRotFaces);
        if(index > -1){
            int turnCount = numTurns(blockRot.x);
            int forwardIndex = getForwardsIndexWrapped(index, turnCount, xRotFaces);
            if(forwardIndex > -1){
                currFace = xRotFaces[forwardIndex];
            }
        }
        
        index = getIndex(currFace, yRotFaces);
        if(index > -1){
            int turnCount = numTurns(blockRot.y);
            int forwardIndex = getForwardsIndexWrapped(index, turnCount, yRotFaces);
            if(forwardIndex > -1){
                currFace = yRotFaces[forwardIndex];
            }
        }
        
        index = getIndex(currFace, zRotFaces);
        if(index > -1){
            int turnCount = numTurns(blockRot.z);
            int forwardIndex = getForwardsIndexWrapped(index, turnCount, zRotFaces);
            if(forwardIndex > -1){
                currFace = zRotFaces[forwardIndex];
            } 
        }
        
        return currFace;
    }
    
    private static int numTurns(float rot){
        rot = rot % 360;
        int numTurns = 0;
        float angle = 45;
        while(rot > angle){
            numTurns++;
            angle += 90;
        }
        return numTurns;
    }
    
    private static int getForwardsIndexWrapped(int start, int forward, CubeFace[] array){
        return (start + forward) % array.length;
    }
    
    private static int getIndex(CubeFace face, CubeFace[] array){
        for(int i = 0; i < array.length; i++){
            if(face == array[i]){
                return i;
            }
        }
        return -1;
    }
    
}
