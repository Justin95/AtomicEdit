
package atomicedit.frontend.ui;

import atomicedit.AtomicEdit;
import atomicedit.backend.BackendController;
import atomicedit.backend.ChunkSectionCoord;
import atomicedit.backend.dimension.Dimension;
import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.frontend.ui.editormenu.EditorTypesMenu;
import atomicedit.logging.Logger;
import atomicedit.settings.AtomicEditSettings;
import java.io.File;
import java.util.concurrent.locks.ReentrantLock;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.Component;
import org.liquidengine.legui.component.Frame;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.component.Panel;
import org.liquidengine.legui.component.SelectBox;
import org.liquidengine.legui.component.event.selectbox.SelectBoxChangeSelectionEvent;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.event.MouseClickEvent.MouseClickAction;
import org.liquidengine.legui.listener.EventListener;
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
        root.getStyle().getFlexStyle().setFlexGrow(1);
        
        Vector2f winSize = frame.getContainer().getSize();
        //context.setWindowSize(new Vector2i((int) winSize.x, (int) winSize.y));
        
        EditorTypesMenu editorMenu = new EditorTypesMenu();
        frame.getContainer().add(editorMenu);
        
        Panel topBar = new Panel();
        topBar.getStyle().getBackground().setColor(new Vector4f(.1f, .1f, .1f, 1f));
        topBar.getStyle().setBorder(null);
        topBar.getStyle().setShadow(null);
        topBar.getStyle().setPosition(Style.PositionType.ABSOLUTE);
        topBar.getStyle().setHeight(30f);
        topBar.getStyle().setDisplay(DisplayType.FLEX);
        topBar.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.ROW);
        topBar.getStyle().getFlexStyle().setAlignItems(AlignItems.CENTER);
        topBar.getStyle().getFlexStyle().setJustifyContent(JustifyContent.FLEX_START);
        topBar.getStyle().setLeft(0f);
        topBar.getStyle().setRight(0f);
        topBar.getStyle().setTop(0);
        topBar.getStyle().setPadding(2);
        topBar.setFocusable(false);
        Button selectWorldButton = new Button();
        selectWorldButton.getTextState().setText("Select World");
        selectWorldButton.getStyle().setMinimumSize(80, 20);
        selectWorldButton.getStyle().setMargin(4);
        selectWorldButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        selectWorldButton.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
            if (event.getAction() == MouseClickAction.CLICK) {
                if (WORLD_SELECT_LOCK.tryLock()) {
                    try {
                        WorldFileSelector selector = new WorldFileSelector(
                            AtomicEdit.getSettings().getSettingValueAsString(AtomicEditSettings.MINECRAFT_INSTALL_LOCATION) + "/saves"
                        );
                        selector.setCallback(
                            (File saveFile) -> {
                                try {
                                    if (saveFile != null) {
                                        String worldFilePath = saveFile.getAbsolutePath();
                                        Logger.info("Selected world: " + worldFilePath);
                                        backendController.setWorld(worldFilePath);
                                    }
                                } finally { //exceptions here are unexpected but we have to unlock
                                    root.remove(selector);
                                    WORLD_SELECT_LOCK.unlock(); 
                                }
                            }
                        );
                        root.add(selector);
                    } catch(Exception e) {
                        Logger.error("Exception trying to select a world", e);
                    } finally {
                        //WORLD_SELECT_LOCK.unlock();
                    }
                }
                
            }
        });
        topBar.add(selectWorldButton);
        
        /*
        //Enable this button to print a json of the loaded block states. This is useful with a minecraft debug world
        //to update the known block states json
        Button devButton = new Button();
        devButton.getTextState().setText("Debug Button");
        devButton.getStyle().setMinimumSize(80, 20);
        devButton.getStyle().setMargin(4);
        devButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        devButton.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
            if(event.getAction() == MouseClickAction.CLICK){
                BlockState.debugPrintAllBlockStates();
                //BlockStateModelLookup.debugPrintFootprint();
            }
        });
        topBar.add(devButton);
        */
        
        Button saveWorldButton = new Button();
        saveWorldButton.getTextState().setText("Save World");
        saveWorldButton.getStyle().setMinimumSize(80, 20);
        saveWorldButton.getStyle().setMargin(4);
        saveWorldButton.getStyle().setPosition(Style.PositionType.RELATIVE);
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
        topBar.add(saveWorldButton);
        
        Button undoButton = new Button();
        undoButton.getTextState().setText("Undo");
        undoButton.getStyle().setMinimumSize(80, 20);
        undoButton.getStyle().setMargin(4);
        undoButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        undoButton.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
            if(event.getAction() == MouseClickAction.CLICK){
                try{
                    backendController.undoOperation();
                    Logger.debug("Undid operation.");
                }catch(Exception e){
                    Logger.error("Error while undoing operation.", e);
                }
            }
        });
        topBar.add(undoButton);
        
        Button redoButton = new Button();
        redoButton.getTextState().setText("Redo");
        redoButton.getStyle().setMinimumSize(80, 20);
        redoButton.getStyle().setMargin(4);
        redoButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        redoButton.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
            if(event.getAction() == MouseClickAction.CLICK){
                try{
                    backendController.redoOperation();
                    Logger.debug("Redid operation.");
                }catch(Exception e){
                    Logger.error("Error while redoing operation.", e);
                }
            }
        });
        topBar.add(redoButton);
        
        SelectBox<Dimension> dimensionSelectBox = new SelectBox<>();
        for (Dimension dim : Dimension.getDimensions()) {
            dimensionSelectBox.addElement(dim);
        }
        dimensionSelectBox.setSelected(0, true);
        dimensionSelectBox.getSelectBoxChangeSelectionEvents().add((EventListener<SelectBoxChangeSelectionEvent<Dimension>>)(event) -> {
            backendController.setActiveDimension(event.getNewValue());
        });
        dimensionSelectBox.getStyle().setMinimumSize(80, 20);
        dimensionSelectBox.getStyle().setMargin(4);
        dimensionSelectBox.getStyle().setPosition(Style.PositionType.RELATIVE);
        dimensionSelectBox.setTabFocusable(false);
        topBar.add(dimensionSelectBox);
        
        coordsLabel = new Label();
        coordsLabel.getStyle().setMinimumSize(80, 20);
        coordsLabel.getStyle().setMargin(4);
        coordsLabel.getStyle().setPosition(Style.PositionType.RELATIVE);
        coordsLabel.getStyle().setFontSize(17f);
        coordsLabel.getStyle().setTextColor(1f, 1f, 1f, 1f);
        topBar.add(coordsLabel);
        frame.getContainer().add(topBar);
    }
    
    public static void updateGui(AtomicEditRenderer renderer){
        Vector3f cameraPos = renderer.getCamera().getPosition();
        ChunkSectionCoord sectionCoord = ChunkSectionCoord.getInstanceFromWorldPos(cameraPos.x, cameraPos.y, cameraPos.z);
        String coordsFormatStr = "Pos: %.2f, %.2f, %.2f Chunk Section: %d, %d, %d";
        String coordsString = String.format(coordsFormatStr,
                cameraPos.x, cameraPos.y, cameraPos.z,
                sectionCoord.x, sectionCoord.y, sectionCoord.z
        );
        coordsLabel.getTextState().setText(coordsString);
    }
    
}
