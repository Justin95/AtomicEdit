
package atomicedit.frontend.render.utils;

import atomicedit.frontend.render.LinesRenderObject;
import atomicedit.frontend.render.OnlyPositionRenderObject;
import atomicedit.frontend.render.RenderObject;
import atomicedit.frontend.render.RenderObjectCollection;
import atomicedit.utils.FloatList;
import atomicedit.utils.IntList;
import atomicedit.volumes.Volume;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 *
 * @author justin
 */
public class VolumeRenderingUtil {
    
    private static final float TRANSLUCENCY = .6f;
    private static final Vector4f COLOR = new Vector4f(.66f, .66f, .66f, TRANSLUCENCY);
    private static final int VERTEX_SIZE = 3;
    
    /**
     * Create a Renderable for a Volume.
     * @param volume
     * @return 
     */
    public static RenderObjectCollection createVolumeRenderable(Volume volume) {
        //Transparent Box Render Object
        FloatList boxVertexData = new FloatList();
        IntList boxIndicies = new IntList();
        IntList linesIndicies = new IntList();
        volume.getEnclosingBox().doForXyz((x, y, z) -> {
            if (!volume.containsXYZ(x, y, z)) {
                return;
            }
            if (!volume.containsXYZ(x + 1, y, z)) {
                int vSize = boxVertexData.size() / VERTEX_SIZE;
                boxVertexData.addAll(
                        x + 1, y    , z    ,
                        x + 1, y + 1, z    ,
                        x + 1, y + 1, z + 1,
                        x + 1, y    , z + 1
                );
                boxIndicies.addAll(
                        vSize, vSize + 1, vSize + 2, vSize, vSize + 2, vSize + 3
                );
                linesIndicies.addAll(
                        vSize, vSize + 1, vSize + 1, vSize + 2, vSize + 2, vSize + 3, vSize + 3, vSize
                );
            }
            if (!volume.containsXYZ(x - 1, y, z)) {
                int vSize = boxVertexData.size() / VERTEX_SIZE;
                boxVertexData.addAll(
                        x    , y    , z    ,
                        x    , y + 1, z    ,
                        x    , y + 1, z + 1,
                        x    , y    , z + 1
                );
                boxIndicies.addAll(
                        vSize, vSize + 2, vSize + 1, vSize, vSize + 3, vSize + 2
                );
                linesIndicies.addAll(
                        vSize, vSize + 1, vSize + 1, vSize + 2, vSize + 2, vSize + 3, vSize + 3, vSize
                );
            }
            if (!volume.containsXYZ(x, y + 1, z)) {
                int vSize = boxVertexData.size() / VERTEX_SIZE;
                boxVertexData.addAll(
                        x    , y + 1, z    ,
                        x + 1, y + 1, z    ,
                        x + 1, y + 1, z + 1,
                        x    , y + 1, z + 1
                );
                boxIndicies.addAll(
                        vSize, vSize + 2, vSize + 1, vSize, vSize + 3, vSize + 2
                );
                linesIndicies.addAll(
                        vSize, vSize + 1, vSize + 1, vSize + 2, vSize + 2, vSize + 3, vSize + 3, vSize
                );
            }
            if (!volume.containsXYZ(x, y - 1, z)) {
                int vSize = boxVertexData.size() / VERTEX_SIZE;
                boxVertexData.addAll(
                        x    , y    , z    ,
                        x + 1, y    , z    ,
                        x + 1, y    , z + 1,
                        x    , y    , z + 1
                );
                boxIndicies.addAll(
                        vSize, vSize + 1, vSize + 2, vSize, vSize + 2, vSize + 3
                );
                linesIndicies.addAll(
                        vSize, vSize + 1, vSize + 1, vSize + 2, vSize + 2, vSize + 3, vSize + 3, vSize
                );
            }
            if (!volume.containsXYZ(x, y, z + 1)) {
                int vSize = boxVertexData.size() / VERTEX_SIZE;
                boxVertexData.addAll(
                        x    , y    , z + 1,
                        x + 1, y    , z + 1,
                        x + 1, y + 1, z + 1,
                        x    , y + 1, z + 1
                );
                boxIndicies.addAll(
                        vSize, vSize + 1, vSize + 2, vSize, vSize + 2, vSize + 3
                );
                linesIndicies.addAll(
                        vSize, vSize + 1, vSize + 1, vSize + 2, vSize + 2, vSize + 3, vSize + 3, vSize
                );
            }
            if (!volume.containsXYZ(x, y, z - 1)) {
                int vSize = boxVertexData.size() / VERTEX_SIZE;
                boxVertexData.addAll(
                        x    , y    , z    ,
                        x + 1, y    , z    ,
                        x + 1, y + 1, z    ,
                        x    , y + 1, z
                );
                boxIndicies.addAll(
                        vSize, vSize + 2, vSize + 1, vSize, vSize + 3, vSize + 2
                );
                linesIndicies.addAll(
                        vSize, vSize + 1, vSize + 1, vSize + 2, vSize + 2, vSize + 3, vSize + 3, vSize
                );
            }
        });
        float[] vertexData = boxVertexData.asArray();
        RenderObject boxRenderObject = new OnlyPositionRenderObject(
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            COLOR,
            true,
            vertexData,
            boxIndicies.asArray()
        );
        
        RenderObject linesRenderObject = new LinesRenderObject(
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector4f(0,0,0,1f),
            true,
            vertexData,
            linesIndicies.asArray()
        );
        return new RenderObjectCollection(boxRenderObject, linesRenderObject);
    }
    
    
    
}
