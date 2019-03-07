
package atomicedit.jarreading.blockmodels;

import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class PositionedFace {
    
    
    public final Vector3f pos1;
    public final Vector3f pos2;
    public final Vector3f pos3;
    public final Vector3f pos4;
    public final int[] indicies;
    private static final Vector3f BLOCK_ROTATE_ABOUT = new Vector3f(.5f, .5f, .5f);
    
    PositionedFace(Vector3f minCorner, Vector3f maxCorner, Vector3f blockRotation, Vector3f modelRotation, Vector3f modelRotateAbout, CubeFace cubeFace){
        int[] base = cubeFace.coordAdditions;
        pos1 = new Vector3f(base[0]==0 ? minCorner.x : maxCorner.x, base[1]==0  ? minCorner.y : maxCorner.y, base[2]==0  ? minCorner.z : maxCorner.z);
        pos2 = new Vector3f(base[3]==0 ? minCorner.x : maxCorner.x, base[4]==0  ? minCorner.y : maxCorner.y, base[5]==0  ? minCorner.z : maxCorner.z);
        pos3 = new Vector3f(base[6]==0 ? minCorner.x : maxCorner.x, base[7]==0  ? minCorner.y : maxCorner.y, base[8]==0  ? minCorner.z : maxCorner.z);
        pos4 = new Vector3f(base[9]==0 ? minCorner.x : maxCorner.x, base[10]==0 ? minCorner.y : maxCorner.y, base[11]==0 ? minCorner.z : maxCorner.z);
        indicies = cubeFace.indicies;
        rotateFace(new Vector3f(modelRotation).negate(), modelRotateAbout); //dont know why model rotation needs to be negated but not block rotation
        rotateFace(blockRotation, BLOCK_ROTATE_ABOUT);
    }
    
    private void rotateFace(Vector3f rot, Vector3f rotAbout){
        rot = new Vector3f((float)Math.toRadians(rot.x), (float)Math.toRadians(rot.y), (float)Math.toRadians(rot.z));
        //I dont know why it needs to be multiplied by -1
        pos1.add(-rotAbout.x, -rotAbout.y, -rotAbout.z).rotateX(-rot.x).rotateY(-rot.y).rotateZ(-rot.z).add(rotAbout.x, rotAbout.y, rotAbout.z);
        pos2.add(-rotAbout.x, -rotAbout.y, -rotAbout.z).rotateX(-rot.x).rotateY(-rot.y).rotateZ(-rot.z).add(rotAbout.x, rotAbout.y, rotAbout.z); 
        pos3.add(-rotAbout.x, -rotAbout.y, -rotAbout.z).rotateX(-rot.x).rotateY(-rot.y).rotateZ(-rot.z).add(rotAbout.x, rotAbout.y, rotAbout.z);
        pos4.add(-rotAbout.x, -rotAbout.y, -rotAbout.z).rotateX(-rot.x).rotateY(-rot.y).rotateZ(-rot.z).add(rotAbout.x, rotAbout.y, rotAbout.z);
    }
    
}
