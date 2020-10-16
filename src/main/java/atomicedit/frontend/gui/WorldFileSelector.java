
package atomicedit.frontend.gui;

import atomicedit.logging.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.joml.Vector4f;
import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.Component;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.component.ScrollablePanel;
import org.liquidengine.legui.component.Widget;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.listener.MouseClickEventListener;
import org.liquidengine.legui.style.Style;
import org.liquidengine.legui.style.border.SimpleLineBorder;
import org.liquidengine.legui.style.flex.FlexStyle;

/**
 * A file selector using LEGUI components.
 * @author Justin Bonner
 */
public class WorldFileSelector extends Widget {
    
    private static final int WIDTH = 600;
    private static final int HEIGHT = 700;
    
    /**
     * Processes the result of the file selection.
     */
    private Consumer<File> consumer;
    
    private final Label pathLabel;
    private final FilePanel filePanel;
    private final Button selectButton;
    private volatile File selectedFile;
    
    public WorldFileSelector(String startingDir) {
        super("Select World File", 500, 800, 0, 0);
        Component panel = super.getContainer();
        this.getStyle().setDisplay(Style.DisplayType.FLEX);
        this.getStyle().getFlexStyle().setAlignContent(FlexStyle.AlignContent.STRETCH);
        this.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.FLEX_START);
        this.getStyle().setHeight(HEIGHT);
        this.getStyle().setWidth(WIDTH);
        this.setCloseable(true);
        this.setMinimizable(false);
        this.setResizable(false);
        this.setSize(WIDTH, HEIGHT);
        
        this.pathLabel = new Label(startingDir, 0, 0, WIDTH, 30);
        this.pathLabel.getStyle().getFlexStyle().setAlignSelf(FlexStyle.AlignSelf.FLEX_START);
        panel.add(pathLabel);
        
        this.filePanel = new FilePanel(startingDir, this);
        panel.add(filePanel);
        
        this.selectButton = new Button(WIDTH - 90, HEIGHT - 60, 70, 30);
        //this.selectButton.getStyle().getFlexStyle().setAlignContent(FlexStyle.AlignContent.CENTER);
        this.selectButton.getStyle().getFlexStyle().setAlignSelf(FlexStyle.AlignSelf.FLEX_END);
        //this.selectButton.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
        //this.selectButton.getStyle().getBackground().setColor(.3f, .3f, .8f, 1);
        //this.selectButton.getTextState().setFontSize(12);
        this.selectButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        //this.selectButton.setEnabled(true);
        this.selectButton.getTextState().setText("Select World");
        this.selectButton.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
            try{
                if (event.getAction() == MouseClickEvent.MouseClickAction.CLICK) {
                    if (getSelectedFile() != null && getSelectedFile().exists() && getSelectedFile().isDirectory()) {
                        consumer.accept(selectedFile);
                    }
                }
            } catch (Exception e) {
                Logger.warning("Exception in File Selector button.", e);
            }
        });
        panel.add(this.selectButton);
    }
    
    public void setCallback(Consumer<File> consumer) {
        this.consumer = consumer;
    }
    
    private File getSelectedFile() {
        return this.selectedFile;
    }
    
    private class FilePanel extends ScrollablePanel {
        
        private File currDir;
        private List<FileOption> listedFiles;
        
        FilePanel(String filePath, WorldFileSelector fileSelector) {
            super(0, 40, WIDTH, HEIGHT - 140);
            this.getStyle().setDisplay(Style.DisplayType.MANUAL);
            this.getStyle().getBackground().setColor(.3f, .3f, .8f, 1);
            this.currDir = new File(filePath);
            if (!currDir.exists()) {
                throw new IllegalArgumentException("Directory `" + filePath + "` does not exist.");
            }
            if (!currDir.isDirectory()) {
                throw new IllegalArgumentException("File is not a directory.");
            }
            this.listedFiles = new ArrayList<>();
            int index = 0;
            for (File file : currDir.listFiles(File::isDirectory)) {
                FileOption fileOpt = new FileOption(file, fileSelector);
                fileOpt.setPosition(5, index * 30 + 5);
                listedFiles.add(fileOpt);
                this.add(fileOpt);
                index++;
            }
        }
        
    }
    
    private class FileOption extends Label {
        
        private final File file;
        
        FileOption(File file, WorldFileSelector fileSelector) {
            super(file.getName());
            this.file = file;
            this.getStyle().setWidth(WIDTH - 10);
            this.getStyle().setHeight(25);
            this.setSize(WIDTH - 10, 25);
            this.getStyle().getBackground().setColor(.8f, .8f, .8f, 1);
            this.getStyle().setBorder(new SimpleLineBorder(new Vector4f(1, 0, 0, 1), 3));
            this.getStyle().getBorder().setEnabled(false);
            this.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
                if (event.getAction() == MouseClickEvent.MouseClickAction.CLICK) {
                    for (FileOption fileOpt : fileSelector.filePanel.listedFiles) {
                        fileOpt.getStyle().getBorder().setEnabled(false);
                    }
                    this.getStyle().getBorder().setEnabled(true);
                    fileSelector.selectedFile = this.file;
                }
            });
        }
        
    }
    
}
