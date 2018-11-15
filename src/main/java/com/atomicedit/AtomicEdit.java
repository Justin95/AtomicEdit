
package com.atomicedit;

import com.atomicedit.backend.BackendController;
import com.atomicedit.frontend.AtomicEditFrontEnd;


/**
 *
 * @author Justin Bonner
 */
public class AtomicEdit {
    
    private AtomicEditFrontEnd frontEnd;
    
    public AtomicEdit(){
        frontEnd = new AtomicEditFrontEnd(new BackendController());
    }
    
    
    
    public void run(){
        frontEnd.run();
    }
    
    
}
