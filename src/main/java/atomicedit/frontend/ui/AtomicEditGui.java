
package atomicedit.frontend.ui;

import atomicedit.AtomicEdit;
import atomicedit.backend.BackendController;
import atomicedit.logging.Logger;
import atomicedit.settings.AtomicEditSettings;
import java.io.File;
import javax.swing.JFileChooser;
import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.Frame;
import org.liquidengine.legui.component.Panel;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.event.MouseClickEvent.MouseClickAction;
import org.liquidengine.legui.listener.MouseClickEventListener;
import org.liquidengine.legui.system.context.Context;

/**
 *
 * @author Justin Bonner
 */
public class AtomicEditGui {
    
    //https://github.com/SpinyOwl/legui/blob/develop/src/main/java/org/liquidengine/legui/demo/ExampleGui.java
    
    public static void initializeGui(Frame frame, Context context, BackendController backendController){
        Panel testPanel = new Panel(20, 20, 140, 170);
        testPanel.setVisible(true);
        Button selectWorldButton = new Button(20, 20, 60, 20);
        selectWorldButton.getTextState().setText("Select World");
        selectWorldButton.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
            if(event.getAction() == MouseClickAction.CLICK){
                String worldFilePath = getWorldFilePath();
                if(worldFilePath != null){
                    Logger.info("Selected world: " + worldFilePath);
                    backendController.setWorld(worldFilePath);
                }
            }
        });
        testPanel.add(selectWorldButton);
        frame.getContainer().add(testPanel);
    }
    
    private static String getWorldFilePath(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setName("Locate minecraft save folder");
        fileChooser.setCurrentDirectory(new File(AtomicEdit.getSettings().getSettingValueAsString(AtomicEditSettings.MINECRAFT_INSTALL_LOCATION)));
        int status;
        File choice;
        //try to make this on top somehow
        status = fileChooser.showOpenDialog(null);
        choice = fileChooser.getSelectedFile();
        if(choice == null) return null;
        return choice.getPath();
    }
    
}
