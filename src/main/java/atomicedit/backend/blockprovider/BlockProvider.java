
package atomicedit.backend.blockprovider;

import atomicedit.volumes.Volume;

/**
 *
 * @author Justin Bonner
 */
public interface BlockProvider {
    
    public Volume getVolume();
    
    public int getBlockAt(int x, int y, int z);
    
    public void doForBlock(ActionForBlock action);
    
}
