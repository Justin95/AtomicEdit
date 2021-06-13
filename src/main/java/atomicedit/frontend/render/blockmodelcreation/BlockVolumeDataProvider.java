
package atomicedit.frontend.render.blockmodelcreation;

/**
 *
 * @author Justin Bonner
 */
public interface BlockVolumeDataProvider {
    
    int getBlockAt(int x, int y, int z);
    
    int getTotalLightAt(int x, int y, int z);
    
}
