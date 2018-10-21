
package com.atomicedit;

import com.atomicedit.frontend.AtomicEditFrontEnd;


/**
 *
 * @author Justin Bonner
 */
public class AtomicEdit {
    
    private AtomicEditFrontEnd frontEnd;
    
    public AtomicEdit(){
        frontEnd = new AtomicEditFrontEnd();
    }
    
    
    
    public void run(){
        frontEnd.run();
    }
    
    
}
