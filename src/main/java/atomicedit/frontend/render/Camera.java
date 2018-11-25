
package atomicedit.frontend.render;

import atomicedit.frontend.render.utils.RenderMatrixUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class Camera {
    
    private static float NEAR_CLIP = 0.1f;
    private static float FAR_CLIP  = 1000f;
    
    private Vector3f position;
    private Vector3f rotation;
    private float fov;
    private float aspectRatio;
    
    public Camera(Vector3f pos, Vector3f rot, float fov, float aspect){
        this.position = pos;
        this.rotation = rot;
        this.fov = fov;
        this.aspectRatio = aspect;
    }
    
    public void setPosition(Vector3f pos){
        this.position = pos;
    }
    
    public void setRotation(Vector3f rot){
        this.rotation = rot;
    }
    
    public Vector3f getPosition(){
        return this.position;
    }
    
    public Vector3f getRotation(){
        return this.rotation;
    }
    
    public Matrix4f getViewMatrix(){
        return RenderMatrixUtils.createViewMatrix(position, rotation);
    }
    
    public Matrix4f getProjectionMatrix(){
        return RenderMatrixUtils.createProjectionMatrix(fov, aspectRatio, NEAR_CLIP, FAR_CLIP);
    }
    
}
