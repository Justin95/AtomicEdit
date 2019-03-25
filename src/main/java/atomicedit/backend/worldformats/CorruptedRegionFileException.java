
package atomicedit.backend.worldformats;

/**
 *
 * @author Justin Bonner
 */
public class CorruptedRegionFileException extends Exception {
    
    public CorruptedRegionFileException(){
        
    }
    
    public CorruptedRegionFileException(String message){
        super(message);
    }
    
}
