
package atomicedit.frontend.render.blockmodelcreation;

/**
 *
 * @author Justin Bonner
 */
public interface BlockVolumeDataProvider {
    
    short getBlockAt(int x, int y, int z);
    
    short getTotalLightAt(int x, int y, int z);
    
}
