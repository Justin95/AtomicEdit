
package com.atomicedit.backend.blockprovider;

import com.atomicedit.backend.schematic.Schematic;
import com.atomicedit.backend.utils.GeneralUtils;
import com.atomicedit.logging.Logger;
import com.atomicedit.volumes.Box;
import com.atomicedit.volumes.Volume;
import java.util.BitSet;

/**
 *
 * @author Justin Bonner
 */
public class SchematicBlockProvider implements BlockProvider{
    
    private short[] blocks;
    private Volume volume;
    private short[] uncompressedBlocks;
    
    
    public SchematicBlockProvider(Schematic schematic){
        if(schematic.volume.getNumBlocksContained() != blocks.length){
            throw new IllegalArgumentException("Volume size and number of blocks differ");
        }
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
        if(uncompressedBlocks == null){
            Logger.warning("Uncompressing schematic in schematic block provider");
            uncompressSchematic();
        }
        int index = GeneralUtils.getIndexYZX(x, y, z, volume.getEnclosingBox().getXLength(), volume.getEnclosingBox().getZLength());
        return uncompressedBlocks[index];
    }
    
    public void doForBlock(ActionForBlock action){
        int volumeIndex = 0;
        int enclosingBoxIndex = 0;
        
        Box enclosingBox = volume.getEnclosingBox();
        BitSet includedSet = volume.getIncludedSet();
        for(int y = 0; y < enclosingBox.getYLength(); y++){
            for(int z = 0; z < enclosingBox.getZLength(); z++){
                for(int x = 0; x < enclosingBox.getXLength(); x++){
                    if(includedSet.get(enclosingBoxIndex)){
                        action.doAction(x, y, z, blocks[volumeIndex]);
                        volumeIndex++;
                    }
                    enclosingBoxIndex++;
                }
            }
        }
    }
    
    private void uncompressSchematic(){
        if(uncompressedBlocks != null) return;
        uncompressedBlocks = new short[volume.getEnclosingBox().getNumBlocksContained()];
        int xLen = volume.getEnclosingBox().getXLength();
        int zLen = volume.getEnclosingBox().getZLength();
        doForBlock((x, y, z, block) -> {
            //if a block isn't set it defaults to 0 which is always counted as minecraft:air, but we shouldnt try to access those anyway as they aren't in the schematic
            uncompressedBlocks[GeneralUtils.getIndexYZX(x, y, z, xLen, zLen)] = block;
        });
    }
    
}
