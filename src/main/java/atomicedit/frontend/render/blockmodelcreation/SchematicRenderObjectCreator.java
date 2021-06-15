
package atomicedit.frontend.render.blockmodelcreation;

import atomicedit.backend.schematic.Schematic;
import atomicedit.backend.utils.GeneralUtils;
import atomicedit.frontend.render.SchematicRenderObject;
import atomicedit.utils.FloatList;
import atomicedit.utils.IntList;
import atomicedit.volumes.Box;
import org.joml.Vector4f;

/**
 *
 * @author Justin Bonner
 */
public class SchematicRenderObjectCreator {
    
    public static SchematicRenderObject createSchematicRenderObject(Schematic schematic) {
        SchematicDataProvider schematicDataProvider = new SchematicDataProvider(schematic);
        BlockModelCreator modelCreator = BlockModelCreator.getInstance();
        FloatList verticies = new FloatList();
        IntList indicies = new IntList();
        schematic.volume.doForXyz((x, y, z, index) -> {
            if (schematicDataProvider.getBlockAt(x, y, z) == 0) {
                return;
            }
            modelCreator.addBlockRenderData(x, y, z, schematicDataProvider, verticies, indicies, false);
        });
        schematic.volume.doForXyz((x, y, z, index) -> {
            if (schematicDataProvider.getBlockAt(x, y, z) == 0) {
                return;
            }
            modelCreator.addBlockRenderData(x, y, z, schematicDataProvider, verticies, indicies, true);
        });
        SchematicRenderObject blocksRenObj = new SchematicRenderObject(
            new Vector4f(.9f, .9f, .9f, .9f),
            true,
            verticies.asArray(),
            indicies.asArray()
        );
        return blocksRenObj;
    }
    
    private static class SchematicDataProvider implements BlockVolumeDataProvider {
        
        private final Schematic schematic;
        
        SchematicDataProvider(Schematic schematic) {
            this.schematic = schematic;
        }

        @Override
        public int getBlockAt(int x, int y, int z) {
            Box box = schematic.volume.getEnclosingBox();
            if (x < 0 || x >= box.getXLength() || y < 0 || y >= box.getYLength() || z < 0 || z >= box.getZLength()) {
                return 0; //AIR
            }
            int index = GeneralUtils.getIndexYZX(x, y, z, box.getXLength(), box.getZLength());
            return schematic.getBlocks()[index];
        }

        @Override
        public int getTotalLightAt(int x, int y, int z) {
            return 15; //schematics are viewed in full brightness
        }
        
        
        
    }
    
}
