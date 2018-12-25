
package atomicedit.frontend.ui;

import atomicedit.AtomicEdit;
import atomicedit.backend.BackendController;
import atomicedit.backend.ChunkSectionCoord;
import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.logging.Logger;
import atomicedit.settings.AtomicEditSettings;
import java.io.File;
import javax.swing.JFileChooser;
import org.joml.Vector3f;
import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.Frame;
import org.liquidengine.legui.component.Label;
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
    private static Label coordsLabel;
    
    public static void initializeGui(Frame frame, Context context, BackendController backendController, AtomicEditRenderer renderer){
        coordsLabel = new Label(200, 20, 200, 20);
        coordsLabel.setVisible(true);
        coordsLabel.getTextState().setTextColor(.3f, .3f, .3f, 1f);
        frame.getContainer().add(coordsLabel);
        
        Panel testPanel = new Panel(20, 20, 120, 170);
        testPanel.setVisible(true);
        Button selectWorldButton = new Button(20, 20, 80, 20);
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
    
    public static void updateGui(AtomicEditRenderer renderer){
        Vector3f cameraPos = renderer.getCamera().getPosition();
        ChunkSectionCoord sectionCoord = ChunkSectionCoord.getInstanceFromWorldPos(cameraPos.x, cameraPos.y, cameraPos.z);
        String coordsString = "Pos: " + cameraPos.x + ", " + cameraPos.y + ", " + cameraPos.z + "\n"
                            + "Chunk Section: " + sectionCoord.x + ", " + sectionCoord.y + ", " + sectionCoord.z;
        coordsLabel.getTextState().setText(coordsString);
    }
    
    private static String getWorldFilePath(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setName("Locate minecraft save folder");
        fileChooser.setCurrentDirectory(new File(AtomicEdit.getSettings().getSettingValueAsString(AtomicEditSettings.MINECRAFT_INSTALL_LOCATION) + "/saves"));
        int status;
        File choice;
        //try to make this on top somehow
        status = fileChooser.showOpenDialog(null);
        choice = fileChooser.getSelectedFile();
        if(choice == null) return null;
        return choice.getPath();
    }
    
}
