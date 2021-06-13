
package atomicedit.frontend.ui;

import atomicedit.logging.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.Component;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.component.ScrollablePanel;
import org.liquidengine.legui.component.TextInput;
import org.liquidengine.legui.component.Widget;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.listener.MouseClickEventListener;
import org.liquidengine.legui.style.Style;
import org.liquidengine.legui.style.flex.FlexStyle;
import org.liquidengine.legui.style.length.Length;
import org.liquidengine.legui.style.length.LengthType;

/**
 *
 * @author Justin Bonner
 */
public class SaveFileWidget extends Widget {
    
    private final Predicate<File> fileCheck;
    private final String forcedFileSuffix;
    private final Label pathLabel;
    private final FilePanel filePanel;
    private final TextInput textBox;
    private final Button selectButton;
    
    public SaveFileWidget(String title, String startingDir, String forcedFileSuffix, Predicate<File> fileCheck, Consumer<File> fileCallback) {
        super(title, 500, 800, 0, 0);
        this.fileCheck = (File file) -> fileCheck.test(file) && forcedFileSuffix != null ? file.getName().endsWith(forcedFileSuffix) : true;
        this.forcedFileSuffix = forcedFileSuffix;
        this.getStyle().setMinHeight(800);
        this.getStyle().setMinWidth(500);
        this.setCloseable(true);
        this.setDraggable(true);
        this.setMinimizable(false);
        
        Component panel = super.getContainer();
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
        
        this.textBox = new TextInput();
        this.textBox.getStyle().setPosition(Style.PositionType.RELATIVE);
        this.textBox.getStyle().setMinimumSize(300, 25);
        this.textBox.getStyle().setMargin(5);
        panel.add(textBox);
        
        this.selectButton = new Button();
        this.selectButton.getStyle().setPosition(Style.PositionType.RELATIVE);
        this.selectButton.getTextState().setText("Save");
        this.selectButton.getStyle().setMinWidth(100);
        this.selectButton.getStyle().setMinHeight(30);
        this.selectButton.getStyle().setMargin(20);
        this.selectButton.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
            try {
                if (event.getAction() == MouseClickEvent.MouseClickAction.CLICK) {
                    String filename = this.textBox.getTextState().getText();
                    if (forcedFileSuffix != null && filename.endsWith(forcedFileSuffix)) {
                        filename = filename.substring(0, filename.length() - forcedFileSuffix.length());
                    }
                    if (filename.trim().isEmpty()) {
                        return;
                    }
                    File file = new File(startingDir + "/" + filename + (forcedFileSuffix != null ? forcedFileSuffix : ""));
                    file.createNewFile();
                    if (fileCheck.test(file)) {
                        fileCallback.accept(file);
                        this.getParent().remove(this);
                    }
                }
            } catch (Exception e) {
                Logger.warning("Exception in File Selector button.", e);
            }
        });
        panel.add(this.selectButton);
    }
    
    private class FilePanel extends ScrollablePanel {
        
        private File currDir;
        private List<FileOption> listedFiles;
        private SaveFileWidget fileSaver;
        
        FilePanel(String filePath, SaveFileWidget fileSaver) {
            super();
            this.currDir = new File(filePath);
            if (!currDir.exists()) {
                throw new IllegalArgumentException("Directory `" + filePath + "` does not exist.");
            }
            if (!currDir.isDirectory()) {
                throw new IllegalArgumentException("File is not a directory.");
            }
            this.fileSaver = fileSaver;
            this.listedFiles = new ArrayList<>();
            initialize();
        }
        
        private void initialize() {
            this.setAutoResize(true);
            this.setFocusable(false);
            this.setHorizontalScrollBarVisible(false);
            
            this.getStyle().getFlexStyle().setFlexGrow(1);
            this.getStyle().getFlexStyle().setFlexShrink(1);
            this.getStyle().setMinWidth(500 - 10);
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
            this.getContainer().getStyle().getBackground().setColor(.8f, .8f, .8f, 1);
            
            for (File file : currDir.listFiles()) {
                if (!fileSaver.fileCheck.test(file)) {
                    continue;
                }
                FileOption fileOpt = new FileOption(file, fileSaver);
                listedFiles.add(fileOpt);
                this.getContainer().add(fileOpt);
            }
            this.getContainer().setSize(500 - 10, listedFiles.size() * 20 + 10);
        }
        
    }
    
    private class FileOption extends Label {
        
        private final File file;
        private final SaveFileWidget fileSaver;
        
        FileOption(File file, SaveFileWidget fileSaver) {
            super(file.getName());
            this.file = file;
            this.fileSaver = fileSaver;
            initialize();
        }
        
        private void initialize() {
            this.getStyle().setPosition(Style.PositionType.RELATIVE);
            this.getStyle().setWidth(new Length(99f, LengthType.PERCENT));
            this.getStyle().setMinHeight(20);
            this.getStyle().setMaxHeight(20);
            this.getStyle().getBackground().setColor(.8f, .8f, .8f, 1);
            this.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
                if (event.getAction() == MouseClickEvent.MouseClickAction.PRESS) {
                    for (FileOption fileOpt : fileSaver.filePanel.listedFiles) {
                        fileOpt.getStyle().getBackground().setColor(.8f, .8f, .8f, 1);
                    }
                    this.getStyle().getBackground().setColor(.5f, .5f, .5f, 1);
                    String filename;
                    if (fileSaver.forcedFileSuffix != null) {
                        if (file.getName().endsWith(fileSaver.forcedFileSuffix)) {
                            filename = file.getName().substring(0, file.getName().length() - fileSaver.forcedFileSuffix.length());
                        } else {
                            filename = file.getName();
                        }
                    } else {
                        filename = file.getName();
                    }
                    fileSaver.textBox.getTextState().setText(filename);
                }
            });
        }
        
    }
    
}
