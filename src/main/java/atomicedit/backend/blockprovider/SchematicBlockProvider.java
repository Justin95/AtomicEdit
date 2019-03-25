
package atomicedit.backend.blockprovider;

import atomicedit.backend.schematic.Schematic;
import atomicedit.backend.utils.BitArray;
import atomicedit.backend.utils.GeneralUtils;
import atomicedit.volumes.Box;
import atomicedit.volumes.Volume;

/**
 *
 * @author Justin Bonner
 */
public class SchematicBlockProvider implements BlockProvider{
    
    private short[] blocks;
    private Volume volume;
    private short[] uncompressedBlocks;
    
    
    public SchematicBlockProvider(Schematic schematic){
        this.volume = schematic.volume;
        this.blocks = schematic.getBlocks();
        this.uncompressedBlocks = null;
    }

    @Override
    public Volume getVolume() {
        return this.volume;
    }

    @Override
    public short getBlockAt(int x, int y, int z) {
        if(!volume.containsXYZ(x, y, z)){
            throw new IllegalArgumentException("Volume does not contain (" + x + ", " + y + ", " + z + ")");
        }
        /* no longer store schematic blocks compressed
        if(uncompressedBlocks == null){
            Logger.notice("Uncompressing schematic in schematic block provider");
            uncompressSchematic();
        }*/
        int index = GeneralUtils.getIndexYZX(x, y, z, volume.getEnclosingBox().getXLength(), volume.getEnclosingBox().getZLength());
        return blocks[index];
    }
    
    @Override
    public void doForBlock(ActionForBlock action){
        int schematicIndex = 0;
        
        Box enclosingBox = volume.getEnclosingBox();
        BitArray includedSet = volume.getIncludedSet();
        for(int y = 0; y < enclosingBox.getYLength(); y++){
            for(int z = 0; z < enclosingBox.getZLength(); z++){
                for(int x = 0; x < enclosingBox.getXLength(); x++){
                    if(includedSet.get(schematicIndex)){
                        action.doAction(x, y, z, blocks[schematicIndex]);
                    }
                    schematicIndex++;
                }
            }
        }
    }
    
}
