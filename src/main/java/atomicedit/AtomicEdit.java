
package atomicedit;

import atomicedit.backend.BackendController;
import atomicedit.frontend.AtomicEditFrontEnd;
import atomicedit.settings.AeSettingValues;
import atomicedit.settings.AtomicEditSettingsCreator;


/**
 *
 * @author Justin Bonner
 */
public class AtomicEdit {
    
    private AtomicEditFrontEnd frontEnd;
    private static AeSettingValues settings;
    
    public AtomicEdit(){
        initializeSettings();
        frontEnd = new AtomicEditFrontEnd(new BackendController());
    }
    
    
    
    public void run(){
        frontEnd.run();
    }
    
    public static AeSettingValues getSettings(){
        return settings;
    }
    
    public static void initializeSettings(){
        settings = AtomicEditSettingsCreator.createSettings();
    }
    
}
