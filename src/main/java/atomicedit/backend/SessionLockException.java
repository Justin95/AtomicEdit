
package atomicedit.backend;

/**
 *
 * @author justin
 */
public class SessionLockException extends Exception {
    
    public SessionLockException() {
        
    }
    
    public SessionLockException(String message) {
        super(message);
    }
    
    public SessionLockException(String message, Exception e) {
        super(message, e);
    }
    
}
