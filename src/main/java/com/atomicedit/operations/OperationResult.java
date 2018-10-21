
package com.atomicedit.operations;

/**
 *
 * @author Justin Bonner
 */
public class OperationResult {
    
    private boolean success;
    private String message;
    private Exception exception;
    
    public OperationResult(boolean success, String message){
        this.success = success;
        this.message = message;
    }
    
    public OperationResult(boolean success){
        this.success = success;
        this.message = "";
    }
    
    public OperationResult(boolean success, String message, Exception e){
        this.success = success;
        this.message = message;
        this.exception = e;
    }
    
    public OperationResult(boolean success, Exception e){
        this.success = success;
        this.message = "";
        this.exception = e;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
    
    public Exception getException(){
        return exception;
    }
    
}
