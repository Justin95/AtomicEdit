
package atomicedit.frontend.ui;

import atomicedit.AtomicEdit;
import atomicedit.backend.BackendController;
import atomicedit.backend.ChunkSectionCoord;
import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.frontend.gui.FileSelector;
import atomicedit.frontend.ui.editormenu.EditorTypesMenu;
import atomicedit.jarreading.blockstates.BlockStateModelLookup;
import atomicedit.logging.Logger;
import atomicedit.settings.AtomicEditSettings;
import java.io.File;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JFileChooser;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.Component;
import org.liquidengine.legui.component.Frame;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.component.Panel;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.event.MouseClickEvent.MouseClickAction;
import org.liquidengine.legui.listener.MouseClickEventListener;
import org.liquidengine.legui.style.Style;
import org.liquidengine.legui.style.Style.DisplayType;
import org.liquidengine.legui.style.flex.FlexStyle;
import org.liquidengine.legui.style.flex.FlexStyle.AlignItems;
import org.liquidengine.legui.style.flex.FlexStyle.JustifyContent;
import org.liquidengine.legui.system.context.Context;

/**
 *
 * @author Justin Bonner
 */
public class AtomicEditGui {
    
    public static final Vector4f PANEL_COLOR = new Vector4f(.1f, .1f, .1f, .5f);
    //https://github.com/SpinyOwl/legui/blob/develop/src/main/java/org/liquidengine/legui/demo/ExampleGui.java
    private static Label coordsLabel;
    private static final ReentrantLock WORLD_SELECT_LOCK = new ReentrantLock();
    
    public static void initializeGui(Frame frame, Context context, BackendController backendController, AtomicEditRenderer renderer){
        Component root = frame.getContainer();
        root.getStyle().setDisplay(DisplayType.FLEX);
        root.getStyle().getFlexStyle().setJustifyContent(JustifyContent.CENTER);
        root.getStyle().getFlexStyle().setAlignItems(AlignItems.FLEX_START);
        root.getStyle().setPadding(0f);
        root.getStyle().getFlexStyle().setFlexShrink(1);
        
        Vector2f winSize = frame.getContainer().getSize();
        //context.setWindowSize(new Vector2i((int) winSize.x, (int) winSize.y));
        coordsLabel = new Label(200, 20, 200, 20);
        coordsLabel.getTextState().setTextColor(.3f, .3f, .3f, 1f);
        frame.getContainer().add(coordsLabel);
        
        
        EditorTypesMenu editorMenu = new EditorTypesMenu();
        frame.getContainer().add(editorMenu);
        
        Panel testPanel = new Panel();
        testPanel.getStyle().getBackground().setColor(PANEL_COLOR);
        testPanel.setPosition(0, 0);
        testPanel.getStyle().setPosition(Style.PositionType.ABSOLUTE);
        testPanel.getStyle().setHeight(30f);
        testPanel.getStyle().setMinWidth(100f);
        testPanel.getStyle().setMaxWidth(10000f);
        testPanel.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.ROW);
        testPanel.getStyle().getFlexStyle().setAlignItems(AlignItems.FLEX_START);
        testPanel.getStyle().getFlexStyle().setFlex(1, 1, 1);
        testPanel.getStyle().setLeft(0f);
        testPanel.getStyle().setRight(0f);
        testPanel.setFocusable(false);
        Button selectWorldButton = new Button(10, 5, 80, 20);
        selectWorldButton.getTextState().setText("Select World");
        selectWorldButton.getStyle().getFlexStyle().setAlignSelf(FlexStyle.AlignSelf.FLEX_START);
        selectWorldButton.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
            if(event.getAction() == MouseClickAction.CLICK){
                if(WORLD_SELECT_LOCK.tryLock()){
                    try{
                        FileSelector selector = new FileSelector(
                                AtomicEdit.getSettings().getSettingValueAsString(AtomicEditSettings.MINECRAFT_INSTALL_LOCATION) + "/saves",
                                (File saveFile) -> {
                                    if (saveFile != null) {
                                        String worldFilePath = saveFile.getAbsolutePath();
                                        Logger.info("Selected world: " + worldFilePath);
                                        backendController.setWorld(worldFilePath);
                                        WORLD_SELECT_LOCK.unlock();
                                    }
                                }
                        );
                        root.add(selector);
                    }catch(Exception e){
                        Logger.error("Exception trying to select a world", e);
                    }finally{
                        //WORLD_SELECT_LOCK.unlock();
                    }
                }
            }
        });
        testPanel.add(selectWorldButton);
        
        //blah
        Button tempButton = new Button(190, 5, 80, 20);
        tempButton.getTextState().setText("Debug Button");
        tempButton.getStyle().getFlexStyle().setAlignSelf(FlexStyle.AlignSelf.FLEX_START);
        tempButton.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
            if(event.getAction() == MouseClickAction.CLICK){
                //BlockState.debugPrintAllBlockStates();
                BlockStateModelLookup.debugPrintFootprint();
            }
        });
        testPanel.add(tempButton);
        //blah
        
        Button saveWorldButton = new Button(100, 5, 80, 20);
        saveWorldButton.getTextState().setText("Save World");
        saveWorldButton.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
            if(event.getAction() == MouseClickAction.CLICK){
                try{
                    backendController.saveChanges();
                    Logger.info("Saved the world");
                }catch(Exception e){
                    Logger.error("Error while saving the world", e);
                }
            }
        });
        testPanel.add(saveWorldButton);
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
        JFileChooser chooser = new JFileChooser();
        
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setName("Locate minecraft save folder");
        chooser.setCurrentDirectory(new File(AtomicEdit.getSettings().getSettingValueAsString(AtomicEditSettings.MINECRAFT_INSTALL_LOCATION) + "/saves"));
        
        chooser.showOpenDialog(null);
        File file = chooser.getSelectedFile();
        if (file == null) {
            return null;
        }
        return file.getPath();
        
        /*
        final String title = "Locate minecraft save folder";
        final String path = AtomicEdit.getSettings().getSettingValueAsString(AtomicEditSettings.MINECRAFT_INSTALL_LOCATION) + "/saves";
        String result = TinyFileDialogs.tinyfd_selectFolderDialog(title, path);
        
        if (result == null) {
            return null;
        }
        File choice = new File(result);
        if(!choice.exists()) {
            return null;
        }
        return choice.getPath();
        */
    }
    
}
