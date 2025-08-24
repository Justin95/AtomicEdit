
package atomicedit.frontend.ui;

import atomicedit.AtomicEdit;
import atomicedit.backend.BackendController;
import atomicedit.backend.BlockState;
import atomicedit.backend.ChunkSectionCoord;
import atomicedit.backend.dimension.Dimension;
import atomicedit.frontend.AtomicEditRenderer;
import atomicedit.frontend.AtomicEditUi;
import atomicedit.frontend.editor.EditorSystem;
import atomicedit.frontend.editor.EditorType;
import atomicedit.frontend.texture.UiTexture;
import atomicedit.logging.Logger;
import atomicedit.settings.AtomicEditSettings;
import atomicedit.utils.VersionUtils;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;
import java.io.File;
import java.util.List;
import java.util.concurrent.Semaphore;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class AtomicEditGui {
    
    private static final Semaphore WORLD_SELECT_SEM = new Semaphore(1);
    
    private static final ImInt currDimItem = new ImInt();
    
    private static final int DIMENSION_SELECT_HEIGHT = 6;
    
    //https://github.com/ocornut/imgui/blob/master/imgui_demo.cpp#L8550
    public static void uiUpdate(AtomicEditRenderer renderer, BackendController backendController) {
        final boolean debugMode = AtomicEdit.getSettings().getSettingValueAsBoolean(AtomicEditSettings.DEBUG_MODE);
        //top menu bar
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.button("Open###select_world_button")) {
                if (WORLD_SELECT_SEM.tryAcquire()) {
                    try {
                        new Thread(
                            () -> {
                                try {
                                    JFileChooser fileChooser = new JFileChooser(
                                        AtomicEdit.getSettings().getSettingValueAsString(AtomicEditSettings.MINECRAFT_INSTALL_LOCATION) + "/saves"
                                    );
                                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                    fileChooser.setMultiSelectionEnabled(false);
                                    fileChooser.setDialogTitle("Select Save Folder");
                                    fileChooser.setAcceptAllFileFilterUsed(false);
                                    fileChooser.setFileFilter(new WorldDirFileFilter());
                                    int check = fileChooser.showOpenDialog(null);
                                    if (check == JFileChooser.APPROVE_OPTION) {
                                        File saveFile = fileChooser.getSelectedFile();
                                        if (saveFile != null && isValidMcSave(saveFile)) {
                                            String worldFilePath = saveFile.getAbsolutePath();
                                            Logger.info("Selected world: " + worldFilePath);
                                            backendController.setWorld(worldFilePath);
                                        } else {
                                            Logger.notice("Invalid save file: " + saveFile);
                                        }
                                    } 
                                } finally {
                                    WORLD_SELECT_SEM.release();
                                }
                            },
                            "Level Chooser Thread"
                        ).start();
                    } catch(Exception e) {
                        Logger.error("Exception trying to select a world", e);
                    }
                }
            }
            if (ImGui.button("Save###save_world_button")) {
                try {
                    backendController.saveChanges();
                    Logger.info("Saved the world");
                } catch(Exception e) {
                    Logger.error("Error while saving the world", e);
                }
            }
            //Dimension selector
            {
                String[] dimensionStrs = getDimensionNames(Dimension.getDimensions(backendController.getWorldPath()));
                ImGui.setNextItemWidth(200); //temp solution
                if (ImGui.combo("###dimension_combo", currDimItem, dimensionStrs, DIMENSION_SELECT_HEIGHT)) {
                    Dimension newDim = Dimension.DEFAULT_DIMENSION;
                    final String newDimName = dimensionStrs[currDimItem.intValue()];
                    for (Dimension dim : Dimension.getDimensions(backendController.getWorldPath())) {
                        if (dim.getName().equals(newDimName)) {
                            newDim = dim;
                            break;
                        }
                    }
                    backendController.setActiveDimension(newDim);
                }
            }
            if (ImGui.button("Undo###undo_button")) {
                try {
                    backendController.undoOperation();
                    Logger.debug("Undid operation.");
                } catch(Exception e) {
                    Logger.error("Error while undoing operation.", e);
                }
            }
            if (ImGui.button("Redo###redo_button")) {
                try {
                    backendController.redoOperation();
                    Logger.debug("Redid operation.");
                } catch(Exception e) {
                    Logger.error("Error while redoing operation.", e);
                }
            }
            if (debugMode) {
                if (ImGui.button("Debug Print Blockstates###debug_print_blockstates_button")) {
                    BlockState.debugPrintAllBlockStates();
                }
            }
            //coords label
            {
                Vector3f cameraPos = renderer.getCamera().getPosition();
                ChunkSectionCoord sectionCoord = ChunkSectionCoord.getInstanceFromWorldPos(cameraPos.x, cameraPos.y, cameraPos.z);
                String coordsFormatStr = "Pos: %.2f, %.2f, %.2f Chunk: %d, %d, %d";
                String coordsString = String.format(coordsFormatStr,
                        cameraPos.x, cameraPos.y, cameraPos.z,
                        sectionCoord.x, sectionCoord.y, sectionCoord.z
                );
                ImGui.text(coordsString);
            }
            if (VersionUtils.isUpdateAvailable()) {
                if (ImGui.button("Update Available: " + VersionUtils.getNewestAvailableVersion() + "###update_button")) {
                    VersionUtils.openAtomicEditDownloadPage();
                }  
            }
            ImGui.endMainMenuBar();
        }
        
        //bottom mode select bar
        int winFlags = ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoDecoration;
        ImVec2 totalSize = AtomicEditUi.getWindowSize();
        ImGui.setNextWindowPos(totalSize.x / 2, totalSize.y - 50, 0.5f, 1f);
        ImGui.setNextWindowSize(588f, 122f);
        if (ImGui.begin("###bottom_mode_bar", winFlags)) {
            imageButton(UiTexture.AREA_SELECT_ICON, EditorType.AREA_SELECTION);
            imageButton(UiTexture.BRUSH_ICON, EditorType.BRUSH_ACTION);
            imageButton(UiTexture.SCHEMATIC_ICON, EditorType.SCHEMATIC_EDITOR);
            imageButton(UiTexture.ENTITY_ICON, EditorType.ENTITY_EDITOR);
            imageButton(UiTexture.BLOCK_ENTITY_ICON, EditorType.BLOCK_ENTITY_EDITOR);
            ImGui.end();
        }
        
    }
    
    private static void imageButton(UiTexture tex, EditorType editorType) {
        final EditorType currEditorType = EditorSystem.getCurrentEditorType();
        final ImVec4 backgroundColor = new ImVec4(.2f, .2f, .2f, .8f);
        final ImVec4 tintColor = new ImVec4(1f, 1f, 1f, 1f);
        final ImVec4 selTintColor = new ImVec4(.7f, .7f, .7f, 1f);
        long texId = tex.getTexture().getTextureID();
        ImVec2 size = new ImVec2(100, 100);
        ImVec2 uv1 = new ImVec2(tex.getUvLow().x, tex.getUvLow().y);
        ImVec2 uv2 = new ImVec2(tex.getUvHigh().x, tex.getUvHigh().y);
        boolean selected = currEditorType == editorType;
        if (ImGui.imageButton("###button_" + editorType, texId, size, uv1, uv2, backgroundColor, selected ? selTintColor : tintColor)) {
            EditorSystem.setEditorType(editorType);
        }
        ImGui.sameLine();
    }
    
    private static String[] getDimensionNames(List<Dimension> dimentions) {
        String[] names = new String[dimentions.size()];
        for (int i = 0; i < dimentions.size(); i++) {
            names[i] = dimentions.get(i).getName();
        }
        return names;
    }
    
    private static boolean isValidMcSave(File file) {
        if (!file.isDirectory()) {
            return false;
        }
        //only accept directories containing a 'level.dat'

        String[] subFiles = file.list((dir, filename) -> "level.dat".equals(filename));
        return subFiles.length > 0;
    }
    
    public static class WorldDirFileFilter extends FileFilter {

        @Override
        public boolean accept(File file) {
            return isValidMcSave(file);
        }

        @Override
        public String getDescription() {
            return "Minecraft save folders";
        }
        
    }
    
}
