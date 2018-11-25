
package atomicedit.backend.nbt;

/**
 *
 * @author Justin Bonner
 */
public class MalformedNbtTagException extends Exception {
    
    public MalformedNbtTagException(){
        
    }
    
    public MalformedNbtTagException(String message){
        super(message);
    }
    
}
