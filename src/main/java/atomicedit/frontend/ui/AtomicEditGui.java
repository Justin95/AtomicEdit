
package atomicedit.frontend.ui;

import atomicedit.AtomicEdit;
import atomicedit.backend.BackendController;
import atomicedit.backend.ChunkSectionCoord;
import atomicedit.backend.dimension.Dimension;
import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.frontend.ui.editormenu.EditorTypesMenu;
import atomicedit.logging.Logger;
import atomicedit.settings.AtomicEditSettings;
import atomicedit.utils.FileUtils;
import atomicedit.utils.VersionUtils;
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
import org.liquidengine.legui.component.Tooltip;
import org.liquidengine.legui.component.event.selectbox.SelectBoxChangeSelectionEvent;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.event.MouseClickEvent.MouseClickAction;
import org.liquidengine.legui.icon.ImageIcon;
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
    
    public static final Vector4f PANEL_COLOR = new Vector4f(.2f, .2f, .2f, .8f);
    //https://github.com/SpinyOwl/legui/blob/develop/src/main/java/org/liquidengine/legui/demo/ExampleGui.java
    private static Label coordsLabel;
    private static SelectBox<Dimension> dimensionSelectBox;
    private static final ReentrantLock WORLD_SELECT_LOCK = new ReentrantLock();
    
    private static final int ICON_SIZE = 26;
    private static final int CHAR_WIDTH = 7;
    private static final ImageIcon SAVE_ICON = FileUtils.loadIcon("icons/save.png");
    private static final ImageIcon LOAD_ICON = FileUtils.loadIcon("icons/load.png");
    private static final ImageIcon UNDO_ICON = FileUtils.loadIcon("icons/undo.png");
    private static final ImageIcon REDO_ICON = FileUtils.loadIcon("icons/redo.png");
    
    static {
        SAVE_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
        LOAD_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
        UNDO_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
        REDO_ICON.setSize(new Vector2f(ICON_SIZE, ICON_SIZE));
    }
    
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
        Button selectWorldButton = new Button("");
        selectWorldButton.getStyle().setMinimumSize(ICON_SIZE, ICON_SIZE);
        selectWorldButton.getStyle().setMargin(4);
        selectWorldButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        selectWorldButton.getStyle().getBackground().setIcon(LOAD_ICON);
        Tooltip loadToolTip = new Tooltip();
        loadToolTip.getTextState().setText("Select World");
        loadToolTip.setSize("Select World".length() * CHAR_WIDTH, 20);
        loadToolTip.getStyle().getBackground().setColor(.8f, .8f, .25f, .7f);
        loadToolTip.getStyle().setTextColor(0f, 0f, 0f, 1f);
        selectWorldButton.setTooltip(loadToolTip);
        selectWorldButton.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
            if (event.getAction() == MouseClickAction.CLICK) {
                if (WORLD_SELECT_LOCK.tryLock()) {
                    try {
                        FileSelectorWidget selector = new FileSelectorWidget(
                            "Select Save File",
                            AtomicEdit.getSettings().getSettingValueAsString(AtomicEditSettings.MINECRAFT_INSTALL_LOCATION) + "/saves",
                            (File file) -> {
                                return file.isDirectory(); //could look for a level.dat too
                            },
                            (File saveFile) -> {
                                try {
                                    if (saveFile != null) {
                                        String worldFilePath = saveFile.getAbsolutePath();
                                        Logger.info("Selected world: " + worldFilePath);
                                        backendController.setWorld(worldFilePath);
                                        updateDimensionsBox(worldFilePath);
                                    }
                                } finally { //exceptions here are unexpected but we have to unlock
                                    WORLD_SELECT_LOCK.unlock();
                                }
                            }
                        );
                        root.add(selector);
                    } catch(Exception e) {
                        Logger.error("Exception trying to select a world", e);
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
        saveWorldButton.getTextState().setText("");
        saveWorldButton.getStyle().setMinimumSize(ICON_SIZE, ICON_SIZE);
        saveWorldButton.getStyle().setMargin(4);
        saveWorldButton.getStyle().setMarginRight(100f); //put some space between save and undo
        saveWorldButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        saveWorldButton.getStyle().getBackground().setIcon(SAVE_ICON);
        Tooltip saveToolTip = new Tooltip();
        saveToolTip.getTextState().setText("Save World");
        saveToolTip.setSize("Save World".length() * CHAR_WIDTH, 20);
        saveToolTip.getStyle().getBackground().setColor(.8f, .8f, .25f, .7f);
        saveToolTip.getStyle().setTextColor(0f, 0f, 0f, 1f);
        saveWorldButton.setTooltip(saveToolTip);
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
        undoButton.getTextState().setText("");
        undoButton.getStyle().setMinimumSize(ICON_SIZE, ICON_SIZE);
        undoButton.getStyle().setMargin(4);
        undoButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        undoButton.getStyle().getBackground().setIcon(UNDO_ICON);
        Tooltip undoToolTip = new Tooltip();
        undoToolTip.getTextState().setText("Undo");
        undoToolTip.setSize("Undo".length() * (CHAR_WIDTH + 1), 20);
        undoToolTip.getStyle().getBackground().setColor(.8f, .8f, .25f, .7f);
        undoToolTip.getStyle().setTextColor(0f, 0f, 0f, 1f);
        undoButton.setTooltip(undoToolTip);
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
        redoButton.getTextState().setText("");
        redoButton.getStyle().setMinimumSize(ICON_SIZE, ICON_SIZE);
        redoButton.getStyle().setMargin(4);
        redoButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        redoButton.getStyle().getBackground().setIcon(REDO_ICON);
        Tooltip redoToolTip = new Tooltip();
        redoToolTip.getTextState().setText("Redo");
        redoToolTip.setSize("Redo".length() * CHAR_WIDTH, 20);
        redoToolTip.getStyle().getBackground().setColor(.8f, .8f, .25f, .7f);
        redoToolTip.getStyle().setTextColor(0f, 0f, 0f, 1f);
        redoButton.setTooltip(redoToolTip);
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
        
        dimensionSelectBox = new SelectBox<>();
        for (Dimension dim : Dimension.getDefaultDimensions()) {
            dimensionSelectBox.addElement(dim);
        }
        dimensionSelectBox.setVisibleCount(10);
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
        
        if (VersionUtils.isUpdateAvailable()) {
            Label updateLabel = new Label();
            updateLabel.getStyle().setMinimumSize(150, 20);
            updateLabel.getStyle().setMargin(4);
            updateLabel.getStyle().setPosition(Style.PositionType.ABSOLUTE);
            updateLabel.getStyle().setRight(4);
            updateLabel.getStyle().setFontSize(17f);
            updateLabel.getStyle().setTextColor(1f, 1f, 1f, 1f);
            updateLabel.getTextState().setText("Update Available: " + VersionUtils.getNewestAvailableVersion());
            updateLabel.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
                if(event.getAction() == MouseClickAction.CLICK){
                    VersionUtils.openAtomicEditDownloadPage();
                }
            });
            topBar.add(updateLabel);
        }
        
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
    
    private static void updateDimensionsBox(String worldFilePath) {
        int numElements = dimensionSelectBox.getElements().size();
        for (int i = numElements - 1; i >= 0; i--) {
            dimensionSelectBox.removeElement(i);
        }
        for (Dimension dim : Dimension.getDimensions(worldFilePath)) {
            dimensionSelectBox.addElement(dim);
        }
    }
    
}
