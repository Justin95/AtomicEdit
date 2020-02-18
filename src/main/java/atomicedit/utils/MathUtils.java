
package atomicedit.utils;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;

/**
 *
 * @author Justin Bonner
 */
public class MathUtils {
    
    /**
     * Rotate the vector around the x axis.
     * @param toRot
     * @param angle the angle in degrees
     */
    public static void rotateXAxis(Vector3f toRot, float angle){
        angle = (float)Math.toRadians(angle);
        float y = toRot.y;
        float z = toRot.z;
        toRot.y = (float)(y * Math.cos(angle) - z * Math.sin(angle));
        toRot.z = (float)(y * Math.sin(angle) + z * Math.cos(angle));
    }
    
    /**
     * Rotate the vector around the y axis.
     * @param toRot
     * @param angle the angle in degrees
     */
    public static void rotateYAxis(Vector3f toRot, float angle){
        angle = (float)Math.toRadians(angle);
        float x = toRot.x;
        float z = toRot.z;
        toRot.x = (float)(x * Math.cos(angle) + z * Math.sin(angle));
        toRot.z = (float)(-x * Math.sin(angle) + z * Math.cos(angle));
    }
    
    /**
     * Rotate the vector around the z axis.
     * @param toRot
     * @param angle the angle in degrees
     */
    public static void rotateZAxis(Vector3f toRot, float angle){
        angle = (float)Math.toRadians(angle);
        float x = toRot.x;
        float y = toRot.y;
        toRot.x = (float)(x * Math.cos(angle) - y * Math.sin(angle));
        toRot.y = (float)(x * Math.sin(angle) + y * Math.cos(angle));
    }
    
    /**
     * Rotate a vector around each axis by the specified angles in degrees.
     * https://stackoverflow.com/questions/14607640/rotating-a-vector-in-3d-space
     * @param toRot
     * @param angles the angles in degrees
     */
    public static void rotateAllAxis(Vector3f toRot, Vector3f angles){
        rotateXAxis(toRot, angles.x);
        rotateYAxis(toRot, angles.y);
        rotateZAxis(toRot, angles.z);
    }
    
    /**
     * Rotate a vector around each axis by the specified angles in degrees.
     * Rotate about the specified point.
     * @param toRot
     * @param angles
     * @param rotateAbout 
     */
    public static void rotateAllAxisAbout(Vector3f toRot, Vector3f angles, Vector3f rotateAbout){
        toRot.sub(rotateAbout, toRot);
        rotateAllAxis(toRot, angles);
        toRot.add(rotateAbout, toRot);
    }
    
    public static Vector3f average(Vector3f... vectors){
        Vector3f avg = new Vector3f();
        for(Vector3f vec : vectors){
            avg.add(vec);
        }
        avg.div(vectors.length);
        return avg;
    }
    
    /**
     * Create a directional vector from a rotational vector.
     * OpenGL's right handed coordinate scheme is used.
     * @param rotation the rotation about the x and y axis, the z rotation is unused here
     * @return 
     */
    public static Vector3f rotationVectorToDirectionVector(Vector3f rotation){
        Vector3f dir = new Vector3f(0, 0, -1);
        dir.rotateX((float)Math.toRadians(-rotation.x));
        dir.rotateY((float)Math.toRadians(-rotation.y));
        dir.normalize();
        return dir;
    }
    
}
