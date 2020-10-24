
package atomicedit.frontend.render.utils;

import atomicedit.frontend.render.LinesRenderObject;
import atomicedit.frontend.render.NoTextureRenderObject;
import atomicedit.frontend.render.RenderObject;
import atomicedit.frontend.render.RenderObjectCollection;
import atomicedit.utils.FloatList;
import atomicedit.utils.IntList;
import atomicedit.volumes.Volume;
import org.joml.Vector3f;

/**
 *
 * @author justin
 */
public class VolumeRenderingUtil {
    
    private static final float TRANSLUCENCY = .6f;
    
    /**
     * Create a Renderable for a Volume.
     * @param volume
     * @return 
     */
    public static RenderObjectCollection createVolumeRenderable(Volume volume) {
        //Transparent Box Render Object
        FloatList boxVertexData = new FloatList();
        IntList boxIndicies = new IntList();
        volume.getEnclosingBox().doForXyz((x, y, z) -> {
            if (!volume.containsXYZ(x, y, z)) {
                return;
            }
            if (!volume.containsXYZ(x + 1, y, z)) {
                int vSize = boxVertexData.size() / 7;
                boxVertexData.addAll(
                        x + 1, y    , z    ,    .66f, .66f, .66f, TRANSLUCENCY,
                        x + 1, y + 1, z    ,    .66f, .66f, .66f, TRANSLUCENCY,
                        x + 1, y + 1, z + 1,    .66f, .66f, .66f, TRANSLUCENCY,
                        x + 1, y    , z + 1,    .66f, .66f, .66f, TRANSLUCENCY
                );
                boxIndicies.addAll( //add both front and back triangles
                        vSize, vSize + 1, vSize + 2, vSize, vSize + 2, vSize + 3
                        //,vSize + 3, vSize + 2, vSize, vSize + 2, vSize + 1, vSize
                );
            }
            if (!volume.containsXYZ(x - 1, y, z)) {
                int vSize = boxVertexData.size() / 7;
                boxVertexData.addAll(
                        x    , y    , z    ,    .66f, .66f, .66f, TRANSLUCENCY,
                        x    , y + 1, z    ,    .66f, .66f, .66f, TRANSLUCENCY,
                        x    , y + 1, z + 1,    .66f, .66f, .66f, TRANSLUCENCY,
                        x    , y    , z + 1,    .66f, .66f, .66f, TRANSLUCENCY
                );
                boxIndicies.addAll( //add both front and back triangles
                        vSize, vSize + 2, vSize + 1, vSize, vSize + 3, vSize + 2
                        //,vSize + 3, vSize + 2, vSize, vSize + 2, vSize + 1, vSize
                );
            }
            if (!volume.containsXYZ(x, y + 1, z)) {
                int vSize = boxVertexData.size() / 7;
                boxVertexData.addAll(
                        x    , y + 1, z    ,    .66f, .66f, .66f, TRANSLUCENCY,
                        x + 1, y + 1, z    ,    .66f, .66f, .66f, TRANSLUCENCY,
                        x + 1, y + 1, z + 1,    .66f, .66f, .66f, TRANSLUCENCY,
                        x    , y + 1, z + 1,    .66f, .66f, .66f, TRANSLUCENCY
                );
                boxIndicies.addAll( //add both front and back triangles
                        vSize, vSize + 2, vSize + 1, vSize, vSize + 3, vSize + 2
                        //,vSize + 3, vSize + 2, vSize, vSize + 2, vSize + 1, vSize
                );
            }
            if (!volume.containsXYZ(x, y - 1, z)) {
                int vSize = boxVertexData.size() / 7;
                boxVertexData.addAll(
                        x    , y    , z    ,    .66f, .66f, .66f, TRANSLUCENCY,
                        x + 1, y    , z    ,    .66f, .66f, .66f, TRANSLUCENCY,
                        x + 1, y    , z + 1,    .66f, .66f, .66f, TRANSLUCENCY,
                        x    , y    , z + 1,    .66f, .66f, .66f, TRANSLUCENCY
                );
                boxIndicies.addAll( //add both front and back triangles
                        vSize, vSize + 1, vSize + 2, vSize, vSize + 2, vSize + 3
                        //,vSize + 3, vSize + 2, vSize, vSize + 2, vSize + 1, vSize
                );
            }
            if (!volume.containsXYZ(x, y, z + 1)) {
                int vSize = boxVertexData.size() / 7;
                boxVertexData.addAll(
                        x    , y    , z + 1,    .66f, .66f, .66f, TRANSLUCENCY,
                        x + 1, y    , z + 1,    .66f, .66f, .66f, TRANSLUCENCY,
                        x + 1, y + 1, z + 1,    .66f, .66f, .66f, TRANSLUCENCY,
                        x    , y + 1, z + 1,    .66f, .66f, .66f, TRANSLUCENCY
                );
                boxIndicies.addAll( //add both front and back triangles
                        vSize, vSize + 1, vSize + 2, vSize, vSize + 2, vSize + 3
                        //,vSize + 3, vSize + 2, vSize, vSize + 2, vSize + 1, vSize
                );
            }
            if (!volume.containsXYZ(x, y, z - 1)) {
                int vSize = boxVertexData.size() / 7;
                boxVertexData.addAll(
                        x    , y    , z    ,    .66f, .66f, .66f, TRANSLUCENCY,
                        x + 1, y    , z    ,    .66f, .66f, .66f, TRANSLUCENCY,
                        x + 1, y + 1, z    ,    .66f, .66f, .66f, TRANSLUCENCY,
                        x    , y + 1, z    ,    .66f, .66f, .66f, TRANSLUCENCY
                );
                boxIndicies.addAll( //add both front and back triangles
                        vSize, vSize + 2, vSize + 1, vSize, vSize + 3, vSize + 2
                        //,vSize + 3, vSize + 2, vSize, vSize + 2, vSize + 1, vSize
                );
            }
        });
        RenderObject boxRenderObject = new NoTextureRenderObject(
                new Vector3f(0,0,0),
                new Vector3f(0,0,0),
                true,
                boxVertexData.asArray(),
                boxIndicies.asArray()
        );
        //Lines Render Object
        FloatList linesVertexData = new FloatList();
        IntList linesIndicies = new IntList();
        volume.getEnclosingBox().doForXyz((x, y, z) -> {
            if (!volume.containsXYZ(x, y, z)) {
                return;
            }
            if (!volume.containsXYZ(x + 1, y, z)) {
                int vSize = linesVertexData.size() / 7;
                linesVertexData.addAll(
                        x + 1, y    , z    ,    0,0,0,1,
                        x + 1, y + 1, z    ,    0,0,0,1,
                        x + 1, y + 1, z + 1,    0,0,0,1,
                        x + 1, y    , z + 1,    0,0,0,1
                );
                linesIndicies.addAll( //add both front and back triangles
                        vSize, vSize + 1, vSize + 1, vSize + 2, vSize + 2, vSize + 3, vSize + 3, vSize
                );
            }
            if (!volume.containsXYZ(x - 1, y, z)) {
                int vSize = linesVertexData.size() / 7;
                linesVertexData.addAll(
                        x    , y    , z    ,    0,0,0,1,
                        x    , y + 1, z    ,    0,0,0,1,
                        x    , y + 1, z + 1,    0,0,0,1,
                        x    , y    , z + 1,    0,0,0,1
                );
                linesIndicies.addAll( //add both front and back triangles
                        vSize, vSize + 1, vSize + 1, vSize + 2, vSize + 2, vSize + 3, vSize + 3, vSize
                );
            }
            if (!volume.containsXYZ(x, y + 1, z)) {
                int vSize = linesVertexData.size() / 7;
                linesVertexData.addAll(
                        x    , y + 1, z    ,    0,0,0,1,
                        x + 1, y + 1, z    ,    0,0,0,1,
                        x + 1, y + 1, z + 1,    0,0,0,1,
                        x    , y + 1, z + 1,    0,0,0,1
                );
                linesIndicies.addAll( //add both front and back triangles
                        vSize, vSize + 1, vSize + 1, vSize + 2, vSize + 2, vSize + 3, vSize + 3, vSize
                );
            }
            if (!volume.containsXYZ(x, y - 1, z)) {
                int vSize = linesVertexData.size() / 7;
                linesVertexData.addAll(
                        x    , y    , z    ,    0,0,0,1,
                        x + 1, y    , z    ,    0,0,0,1,
                        x + 1, y    , z + 1,    0,0,0,1,
                        x    , y    , z + 1,    0,0,0,1
                );
                linesIndicies.addAll( //add both front and back triangles
                        vSize, vSize + 1, vSize + 1, vSize + 2, vSize + 2, vSize + 3, vSize + 3, vSize
                );
            }
            if (!volume.containsXYZ(x, y, z + 1)) {
                int vSize = linesVertexData.size() / 7;
                linesVertexData.addAll(
                        x    , y    , z + 1,    0,0,0,1,
                        x + 1, y    , z + 1,    0,0,0,1,
                        x + 1, y + 1, z + 1,    0,0,0,1,
                        x    , y + 1, z + 1,    0,0,0,1
                );
                linesIndicies.addAll( //add both front and back triangles
                        vSize, vSize + 1, vSize + 1, vSize + 2, vSize + 2, vSize + 3, vSize + 3, vSize
                );
            }
            if (!volume.containsXYZ(x, y, z - 1)) {
                int vSize = linesVertexData.size() / 7;
                linesVertexData.addAll(
                        x    , y    , z    ,    0,0,0,1,
                        x + 1, y    , z    ,    0,0,0,1,
                        x + 1, y + 1, z    ,    0,0,0,1,
                        x    , y + 1, z    ,    0,0,0,1
                );
                linesIndicies.addAll( //add both front and back triangles
                        vSize, vSize + 1, vSize + 1, vSize + 2, vSize + 2, vSize + 3, vSize + 3, vSize
                );
            }
        });
        RenderObject linesRenderObject = new LinesRenderObject(
                new Vector3f(0,0,0),
                new Vector3f(0,0,0),
                true,
                linesVertexData.asArray(),
                linesIndicies.asArray()
        );
        return new RenderObjectCollection(boxRenderObject, linesRenderObject);
    }
    
    
    
}
