
package atomicedit.frontend.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.joml.Vector4f;
import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.component.ScrollablePanel;
import org.liquidengine.legui.component.Widget;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.listener.MouseClickEventListener;
import org.liquidengine.legui.style.Style;
import org.liquidengine.legui.style.border.SimpleLineBorder;

/**
 * A file selector using LEGUI components.
 * @author Justin Bonner
 */
public class FileSelector extends Widget {
    
    private static final int WIDTH = 400;
    private static final int HEIGHT = 700;
    
    /**
     * Processes the result of the file selection.
     */
    private Consumer<File> consumer;
    
    private final Label pathLabel;
    private final FilePanel panel;
    private final Button selectButton;
    private volatile File selectedFile;
    
    public FileSelector(String startingDir) {
        super("Locate minecraft save folder", 500, 800, WIDTH, HEIGHT);
        this.getStyle().setDisplay(Style.DisplayType.MANUAL);
        this.getStyle().setHeight(HEIGHT);
        this.getStyle().setWidth(WIDTH);
        this.setCloseable(false);
        this.setMinimizable(false);
        this.setResizable(true);
        this.setSize(WIDTH, HEIGHT);
        
        this.pathLabel = new Label(startingDir, 0, 0, WIDTH, 30);
        this.add(pathLabel);
        
        this.panel = new FilePanel(startingDir, this);
        this.add(panel);
        
        this.selectButton = new Button(WIDTH - 50, HEIGHT - 20, 45, 25);
        this.selectButton.getStyle().getBackground().setColor(.3f, .3f, .8f, 1);
        this.selectButton.getTextState().setText("Select");
        this.selectButton.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
            System.out.println("PUSH BUTTON");
            if (event.getAction() == MouseClickEvent.MouseClickAction.CLICK) {
                System.out.println("File: " + getSelectedFile().getAbsolutePath());
                if (getSelectedFile() != null && getSelectedFile().exists() && getSelectedFile().isDirectory()) {
                    System.out.println("Calling consumer");
                    consumer.accept(selectedFile);
                }
            }
        });
        this.add(this.selectButton);
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
        
        FilePanel(String filePath, FileSelector fileSelector) {
            super(0, 40, WIDTH, HEIGHT - 70);
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
                System.out.println("Creating file option: " + file.getAbsolutePath());
                FileOption fileOpt = new FileOption(file, fileSelector);
                fileOpt.setPosition(5, index * 32);
                listedFiles.add(fileOpt);
                this.add(fileOpt);
            }
        }
        
    }
    
    private class FileOption extends Label {
        
        private File file;
        
        FileOption(File file, FileSelector fileSelector) {
            super(file.getName());
            this.getStyle().setWidth(WIDTH);
            this.getStyle().setHeight(30);
            this.getStyle().setBorder(new SimpleLineBorder(new Vector4f(1, 0, 0, 1), 3));
            this.getStyle().getBorder().setEnabled(false);
            this.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
                if (event.getAction() == MouseClickEvent.MouseClickAction.CLICK) {
                    for (FileOption fileOpt : fileSelector.panel.listedFiles) {
                        fileOpt.getStyle().getBorder().setEnabled(false);
                    }
                    this.getStyle().getBorder().setEnabled(true);
                    System.out.println("Setting file: " + file.getAbsolutePath());
                    fileSelector.selectedFile = this.file;
                }
            });
        }
        
    }
    
}
