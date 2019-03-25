
import atomicedit.AtomicEdit;
import atomicedit.logging.Logger;



/**
 *
 * @author Justin Bonner
 */
public class Main {
    
    
    public static void main(String[] args){
        Logger.initialize();
        AtomicEdit atomicEdit = AtomicEdit.getInstance();
        atomicEdit.run();
        Logger.cleanUp();
    }
    
}
