
package atomicedit.frontend.editor;

import atomicedit.frontend.render.LinesRenderObject;
import atomicedit.frontend.render.NoTextureRenderObject;
import atomicedit.frontend.render.RenderObject;
import atomicedit.frontend.render.RenderObjectCollection;
import atomicedit.frontend.render.Renderable;
import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 *
 * @author Justin Bonner
 */
public class EditorUtils {
    
    static RenderObject createEditorPointerRenderObject(Vector3i position){
        float min = -0.01f;
        float max = 1.01f;
        return new NoTextureRenderObject(
            new Vector3f(position),
            new Vector3f(0,0,0),
            true,
            new float[]{
                min,min,min,    .33f, .33f, .66f,  .7f,
                min,min,max,    .66f, .66f, .66f,  .7f,
                min,max,min,    .66f, .66f, .66f,  .7f,
                min,max,max,    .66f, .66f, .66f,  .7f,
                max,min,min,    .66f, .66f, .66f,  .7f,
                max,min,max,    .66f, .66f, .66f,  .7f,
                max,max,min,    .66f, .66f, .66f,  .7f,
                max,max,max,    .99f, .99f, .66f,  .7f,
            },
            new int[]{
                0,1,3,  0,3,2, //x = 0 face
                0,4,5,  0,5,1, //y = 0 face
                0,2,6,  0,6,4, //z = 0 face
                4,6,7,  4,7,5, //x = 1 face
                2,3,7,  2,7,6, //y = 1 face
                1,5,7,  1,7,3  //z = 1 face
            }
        );
    }
    
    static Renderable createSelectionBoxRenderable(Vector3i pointA, Vector3i pointB){
        Vector3f position = new Vector3f(Math.min(pointA.x, pointB.x), Math.min(pointA.y, pointB.y), Math.min(pointA.z, pointB.z));
        Vector3f rotation = new Vector3f(0,0,0);
        float min = -0.02f;
        float xLen = Math.abs(pointA.x - pointB.x) + 1 + .02f;
        float yLen = Math.abs(pointA.y - pointB.y) + 1 + .02f;
        float zLen = Math.abs(pointA.z - pointB.z) + 1 + .02f;
        float[] vertexData = new float[]{
             min, min, min,    .33f, .33f, .66f,  .6f,
             min, min,zLen,    .66f, .66f, .66f,  .6f,
             min,yLen, min,    .66f, .66f, .66f,  .6f,
             min,yLen,zLen,    .66f, .66f, .66f,  .6f,
            xLen, min, min,    .66f, .66f, .66f,  .6f,
            xLen, min,zLen,    .66f, .66f, .66f,  .6f,
            xLen,yLen, min,    .66f, .66f, .66f,  .6f,
            xLen,yLen,zLen,    .99f, .99f, .66f,  .6f,
        };
        int[] faceIndicies = new int[]{
            0,1,3,  0,3,2, //x = 0 face
            0,4,5,  0,5,1, //y = 0 face
            0,2,6,  0,6,4, //z = 0 face
            4,6,7,  4,7,5, //x = 1 face
            2,3,7,  2,7,6, //y = 1 face
            1,5,7,  1,7,3, //z = 1 face
            //render both sides of the triangles
            3,1,0,  2,3,0,
            5,4,0,  1,5,0,
            6,2,0,  4,6,0,
            7,6,4,  5,7,4,
            7,3,2,  6,7,2,
            7,5,1,  3,7,1
        };
        int[] lineIndicies = new int[]{
            0,1,  0,2,  0,4,
            1,3,  1,5,
            2,3,  2,6,
            3,7,
            4,5,  4,6,
            5,7,
            6,7
        };
        return new RenderObjectCollection(
            new NoTextureRenderObject(position, rotation, true, vertexData, faceIndicies),
            new LinesRenderObject(position, rotation, false, vertexData, lineIndicies)
        );
    }
    
}
