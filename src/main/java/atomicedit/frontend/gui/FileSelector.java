
package atomicedit.frontend.gui;

import java.io.File;
import java.util.function.Consumer;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.component.ScrollablePanel;
import org.liquidengine.legui.component.Widget;

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
    private final Consumer<File> consumer;
    
    private final Label pathLabel;
    private final ScrollablePanel panel;
    
    public FileSelector(String startingDir, Consumer<File> consumer) {
        super("Locate minecraft save folder", 500,800, WIDTH, HEIGHT);
        this.getStyle().setHeight(HEIGHT);
        this.getStyle().setWidth(WIDTH);
        this.setCloseable(false);
        this.setMinimizable(false);
        this.setResizable(true);
        this.setSize(WIDTH, HEIGHT);
        this.consumer = consumer;
        
        this.pathLabel = new Label(startingDir, 0, 0, WIDTH, 30);
        this.add(pathLabel);
        
        this.panel = new ScrollablePanel(0, 40, WIDTH, HEIGHT - 70);
        this.add(panel);
    }
    
}
