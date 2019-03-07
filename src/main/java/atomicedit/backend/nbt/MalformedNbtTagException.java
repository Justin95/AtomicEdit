
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
    
    public MalformedNbtTagException(String message, NbtTag nbtTag){
        super(message + "\nMalformedNbtTag: " + nbtTag);
    }
    
    public MalformedNbtTagException(NbtTag nbtTag){
        super("MalformedNbtTag: " + nbtTag);
    }
    
}
