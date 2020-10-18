
package atomicedit.backend.biomes;

import atomicedit.backend.BlockCoord;
import atomicedit.volumes.WorldVolume;

/**
 *
 * @author justin
 */
public interface BiomeMap {
    
    /**
     * Get the biome at the given blockCoordinate.
     * @param blockCoord
     * @return 
     */
    int getBiomeAt(BlockCoord blockCoord);
    
    /**
     * Set the biome in this biome map to be the given biome in the given
     * effect volume.
     * @param effectVolume
     * @param setBiome 
     */
    void setBiome(WorldVolume effectVolume, int setBiome);
    
    /**
     * Find the overlap between the given volume and the given biome in this
     * biome map.
     * @param searchVolume
     * @param biome
     * @return 
     */
    WorldVolume findOverlap(WorldVolume searchVolume, int biome);
    
}
