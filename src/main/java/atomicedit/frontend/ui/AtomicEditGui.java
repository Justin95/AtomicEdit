
package atomicedit.frontend.ui;

import atomicedit.AtomicEdit;
import atomicedit.backend.BackendController;
import atomicedit.backend.BlockState;
import atomicedit.backend.ChunkSectionCoord;
import atomicedit.backend.brushes.BrushType;
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
        topBar.getStyle().getBackground().setColor(PANEL_COLOR);
        topBar.getStyle().setPosition(Style.PositionType.ABSOLUTE);
        topBar.getStyle().setHeight(30f);
        topBar.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.ROW);
        topBar.getStyle().getFlexStyle().setAlignItems(AlignItems.FLEX_START);
        topBar.getStyle().setLeft(0f);
        topBar.getStyle().setRight(0f);
        topBar.getStyle().setTop(0);
        topBar.setFocusable(false);
        Button selectWorldButton = new Button(10, 5, 80, 20);
        selectWorldButton.getTextState().setText("Select World");
        selectWorldButton.getStyle().getFlexStyle().setAlignSelf(FlexStyle.AlignSelf.FLEX_START);
        selectWorldButton.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
            if (event.getAction() == MouseClickAction.CLICK) {
                //backendController.setWorld("/home/justin/.minecraft/saves/AtomicEdit_1_16_Test");
                
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
        
        //blah
        Button tempButton = new Button(190, 5, 80, 20);
        tempButton.getTextState().setText("Debug Button");
        tempButton.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
            if(event.getAction() == MouseClickAction.CLICK){
                BlockState.debugPrintAllBlockStates();
                //BlockStateModelLookup.debugPrintFootprint();
            }
        });
        topBar.add(tempButton);
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
        topBar.add(saveWorldButton);
        
        Button undoButton = new Button(580, 5, 80, 20);
        undoButton.getTextState().setText("Undo");
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
        
        Button redoButton = new Button(670, 5, 80, 20);
        redoButton.getTextState().setText("Redo");
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
        
        SelectBox<Dimension> dimensionSelectBox = new SelectBox<>(760, 5, 80, 20);
        for (Dimension dim : Dimension.getDimensions()) {
            dimensionSelectBox.addElement(dim);
        }
        dimensionSelectBox.setSelected(0, true);
        dimensionSelectBox.getSelectBoxChangeSelectionEvents().add((EventListener<SelectBoxChangeSelectionEvent<Dimension>>)(event) -> {
            backendController.setActiveDimension(event.getNewValue());
        });
        dimensionSelectBox.setTabFocusable(false);
        topBar.add(dimensionSelectBox);
        
        coordsLabel = new Label(280, 5, 200, 30);
        coordsLabel.getStyle().setFontSize(17f);
        coordsLabel.getStyle().getFlexStyle().setAlignSelf(FlexStyle.AlignSelf.FLEX_START);
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
