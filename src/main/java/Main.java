
import atomicedit.AtomicEdit;
import atomicedit.logging.Logger;
import atomicedit.settings.AtomicEditConstants;
import java.io.File;



/**
 *
 * @author Justin Bonner
 */
public class Main {
    
    
    public static void main(String[] args){
        createInstallDirs();
        Logger.initialize();
        AtomicEdit atomicEdit = AtomicEdit.getInstance();
        atomicEdit.run();
        Logger.cleanUp();
    }
    
    
    /**
     * Create install directories if they do not exist.
     */
    private static void createInstallDirs() {
        new File(AtomicEditConstants.ATOMIC_EDIT_INSTALL_PATH).mkdir();
        new File(AtomicEditConstants.SCHEMATIC_DIR_PATH).mkdir();
    }
    
}
