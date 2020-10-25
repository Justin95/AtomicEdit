
package atomicedit.frontend.ui;

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
import org.liquidengine.legui.style.length.Length;
import org.liquidengine.legui.style.length.LengthType;

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
        this.getStyle().setHeight(HEIGHT);
        this.getStyle().setWidth(WIDTH);
        this.setCloseable(true);
        this.setDraggable(true);
        this.setMinimizable(false);
        this.setResizable(false);
        
        panel.getStyle().setDisplay(Style.DisplayType.FLEX);
        panel.getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
        panel.getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.FLEX_START);
        panel.getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.CENTER);
        panel.getStyle().setPadding(5);
        panel.setFocusable(false);
        
        this.pathLabel = new Label(startingDir);
        this.pathLabel.getStyle().getBackground().setColor(.9f, .9f, .9f, 1f);
        this.pathLabel.getStyle().setPosition(Style.PositionType.RELATIVE);
        this.pathLabel.getStyle().getFlexStyle().setAlignSelf(FlexStyle.AlignSelf.FLEX_START);
        this.pathLabel.getStyle().setMinHeight(20);
        this.pathLabel.getStyle().setMinWidth(new Length(99f, LengthType.PERCENT));
        this.pathLabel.getStyle().setMargin(5);
        panel.add(pathLabel);
        
        this.filePanel = new FilePanel(startingDir, this);
        panel.add(filePanel);
        
        this.selectButton = new Button();
        this.selectButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        this.selectButton.getTextState().setText("Select World");
        this.selectButton.getStyle().setMinWidth(100);
        this.selectButton.getStyle().setMinHeight(30);
        this.selectButton.getStyle().setMargin(20);
        this.selectButton.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
            try {
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
        private WorldFileSelector fileSelector;
        
        FilePanel(String filePath, WorldFileSelector fileSelector) {
            super();
            this.currDir = new File(filePath);
            if (!currDir.exists()) {
                throw new IllegalArgumentException("Directory `" + filePath + "` does not exist.");
            }
            if (!currDir.isDirectory()) {
                throw new IllegalArgumentException("File is not a directory.");
            }
            this.fileSelector = fileSelector;
            this.listedFiles = new ArrayList<>();
            initialize();
        }
        
        private void initialize() {
            this.setAutoResize(true);
            this.setFocusable(false);
            this.setHorizontalScrollBarVisible(false);
            
            this.getStyle().getFlexStyle().setFlexGrow(1);
            this.getStyle().getFlexStyle().setFlexShrink(1);
            this.getStyle().setMinWidth(WIDTH - 10);
            this.getStyle().setMinHeight(200);
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            
            this.getViewport().setFocusable(false);
            this.getViewport().getStyle().setDisplay(Style.DisplayType.FLEX);
            
            this.getContainer().setFocusable(false);
            this.getContainer().getStyle().setPosition(Style.PositionType.ABSOLUTE);
            this.getContainer().getStyle().setTop(0);
            this.getContainer().getStyle().setLeft(0);
            this.getContainer().getStyle().setBottom(0);
            this.getContainer().getStyle().setRight(0);
            this.getContainer().getStyle().setDisplay(Style.DisplayType.FLEX);
            this.getContainer().getStyle().getFlexStyle().setFlexDirection(FlexStyle.FlexDirection.COLUMN);
            this.getContainer().getStyle().getFlexStyle().setJustifyContent(FlexStyle.JustifyContent.FLEX_START);
            this.getContainer().getStyle().getFlexStyle().setAlignItems(FlexStyle.AlignItems.FLEX_START);
            this.getContainer().getStyle().setPadding(2);
            this.getContainer().getStyle().getBackground().setColor(.4f, .4f, .4f, 1);
            
            for (File file : currDir.listFiles(File::isDirectory)) {
                FileOption fileOpt = new FileOption(file, fileSelector);
                listedFiles.add(fileOpt);
                this.getContainer().add(fileOpt);
            }
        }
        
        
    }
    
    private class FileOption extends Label {
        
        private final File file;
        private final WorldFileSelector fileSelector;
        
        FileOption(File file, WorldFileSelector fileSelector) {
            super(file.getName());
            this.file = file;
            this.fileSelector = fileSelector;
            initialize();
        }
        
        private void initialize() {
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setWidth(new Length(99f, LengthType.PERCENT));
            this.getStyle().setMinHeight(25);
            this.getStyle().setMaxHeight(25);
            this.getStyle().setMargin(2);
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
