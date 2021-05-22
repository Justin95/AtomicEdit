
package atomicedit.frontend.editor;

import atomicedit.backend.schematic.Schematic;
import atomicedit.frontend.render.LinesRenderObject;
import atomicedit.frontend.render.NoTextureRenderObject;
import atomicedit.frontend.render.OnlyPositionRenderObject;
import atomicedit.frontend.render.RenderObject;
import atomicedit.frontend.render.RenderObjectCollection;
import atomicedit.frontend.render.Renderable;
import atomicedit.frontend.render.SchematicRenderObject;
import atomicedit.frontend.render.blockmodelcreation.SchematicRenderObjectCreator;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

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
            new Vector3f(1,1,1),
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
        Vector3f scale = new Vector3f(1,1,1);
        Vector4f boxColor = new Vector4f(.66f, .66f, .66f, .6f);
        Vector4f lineColor = new Vector4f(0, 0, 0, 1f);
        float min = -0.02f;
        float xLen = Math.abs(pointA.x - pointB.x) + 1 + .02f;
        float yLen = Math.abs(pointA.y - pointB.y) + 1 + .02f;
        float zLen = Math.abs(pointA.z - pointB.z) + 1 + .02f;
        float[] vertexData = new float[]{
             min, min, min,
             min, min,zLen,
             min,yLen, min,
             min,yLen,zLen,
            xLen, min, min,
            xLen, min,zLen,
            xLen,yLen, min,
            xLen,yLen,zLen,
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
            new OnlyPositionRenderObject(position, rotation, scale, boxColor, true, vertexData, faceIndicies),
            new LinesRenderObject(position, rotation, scale, lineColor, false, vertexData, lineIndicies)
        );
    }
    
    
    public static Renderable createSchematicRenderable(Schematic schematic) {
        Vector3f pos = new Vector3f(0, 0, 0);
        SchematicRenderObject schematicRo = SchematicRenderObjectCreator.createSchematicRenderObject(schematic);
        schematicRo.updatePosition(pos);
        Vector3f rotation = new Vector3f(0,0,0);
        Vector3f scale = new Vector3f(1,1,1);
        Vector4f boxColor = new Vector4f(.3f, .7f, .3f, .4f);
        Vector4f lineColor = new Vector4f(0, 0, 0, 1f);
        int schemXLen = schematic.volume.getEnclosingBox().getXLength();
        int schemYLen = schematic.volume.getEnclosingBox().getYLength();
        int schemZLen = schematic.volume.getEnclosingBox().getZLength();
        float min = -0.02f;
        float xLen = schemXLen - min;
        float yLen = schemYLen - min;
        float zLen = schemZLen - min;
        float[] faceVertexData = new float[] {
             min, min, min,
             min, min,zLen,
             min,yLen, min,
             min,yLen,zLen,
            xLen, min, min,
            xLen, min,zLen,
            xLen,yLen, min,
            xLen,yLen,zLen,
        };
        min = -0.03f;
        xLen = schemXLen - min;
        yLen = schemYLen - min;
        zLen = schemZLen - min;
        float[] lineVertexData = new float[] {
             min, min, min,
             min, min,zLen,
             min,yLen, min,
             min,yLen,zLen,
            xLen, min, min,
            xLen, min,zLen,
            xLen,yLen, min,
            xLen,yLen,zLen,
        };
        int[] faceIndicies = new int[] {
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
        int[] lineIndicies = new int[] {
            0,1,  0,2,  0,4,
            1,3,  1,5,
            2,3,  2,6,
            3,7,
            4,5,  4,6,
            5,7,
            6,7
        };
        return new RenderObjectCollection(
            schematicRo,
            new OnlyPositionRenderObject(pos, rotation, scale, boxColor, true, faceVertexData, faceIndicies),
            new LinesRenderObject(pos, rotation, scale, lineColor, false, lineVertexData, lineIndicies)
        );
    }
    
}
