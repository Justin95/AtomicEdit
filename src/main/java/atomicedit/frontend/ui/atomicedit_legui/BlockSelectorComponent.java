
package atomicedit.frontend.ui.atomicedit_legui;

import atomicedit.backend.BlockState;
import atomicedit.backend.GlobalBlockStateMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.joml.Vector4f;
import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.Component;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.component.ScrollablePanel;
import org.liquidengine.legui.component.TextInput;
import org.liquidengine.legui.component.Widget;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.listener.MouseClickEventListener;
import org.liquidengine.legui.style.Style;
import org.liquidengine.legui.style.border.SimpleLineBorder;

/**
 *
 * @author justin
 */
public class BlockSelectorComponent extends Label {
    
    private BlockState value;
    private boolean isWidgetOpen;
    private static String searchText = "";
    private Consumer<BlockState> parentCallback;
    
    public BlockSelectorComponent(BlockState initialValue) {
        this.value = initialValue;
        this.isWidgetOpen = false;
        initialize();
    }
    
    private void initialize() {
        this.getTextState().setText(value.toString());
        this.getStyle().getBackground().setColor(1, 1, 1, 1);
        this.getStyle().setFontSize(20f);
        this.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
            if (event.getAction() == MouseClickEvent.MouseClickAction.CLICK) {
                if (!isWidgetOpen) {
                    isWidgetOpen = true;
                    SelectBlockWidget selectWidget = new SelectBlockWidget(value);
                    selectWidget.setCallback((blockState) -> {
                        value = blockState;
                        this.getTextState().setText(value.toString());
                        this.getFrame().getContainer().remove(selectWidget);
                        this.isWidgetOpen = false;
                        this.parentCallback.accept(value);
                    });
                    selectWidget.addWidgetCloseEventListener((closeEvent) -> {
                        this.isWidgetOpen = false;
                    });
                    this.getFrame().getContainer().add(selectWidget);
                }
            }
        });
    }
    
    public void setCallback(Consumer<BlockState> callback) {
        this.parentCallback = callback;
    }
    
    private static class SelectBlockWidget extends Widget {
        
        private static final int WIDTH = 400;
        private static final int HEIGHT = 800;
        /**
         * Max options in the scrollable panel at a time.
         */
        private static final int MAX_OPTIONS = 30;
        
        private TextInput textBox;
        private ScrollablePanel blockOptionsPanel;
        private Button selectButton;
        private List<BlockOptionLabel> blockOptions;
        private BlockState selectedBlock;
        private Consumer<BlockState> callback;
        
        SelectBlockWidget(BlockState initValue) {
            super("Select a Block State");
            Component panel = super.getContainer();
            this.getStyle().setWidth(WIDTH);
            this.getStyle().setHeight(HEIGHT);
            this.setCloseable(true);
            this.setMinimizable(false);
            this.setResizable(false);
            this.selectedBlock = initValue;
            this.blockOptions = new ArrayList<>();
            this.blockOptionsPanel = new ScrollablePanel(40, 80, WIDTH - 80, HEIGHT - 160);
            this.blockOptionsPanel.getContainer().getStyle().setDisplay(Style.DisplayType.MANUAL);
            this.blockOptionsPanel.setHorizontalScrollBarVisible(false);
            this.textBox = new TextInput(40, 40, WIDTH - 80, 25);
            this.textBox.getTextState().setText(searchText);
            this.textBox.addTextInputContentChangeEventListener((event) -> {
                searchText = event.getNewValue();
                updatePanelOptions(event.getNewValue());
            });
            this.selectButton = new Button("Select", WIDTH / 2 + 50, HEIGHT - 60, 100, 30);
            this.selectButton.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
                if (event.getAction() == MouseClickEvent.MouseClickAction.CLICK) {
                    if (selectedBlock != null) {
                        this.callback.accept(selectedBlock);
                    } else if (!blockOptions.isEmpty()) {
                        this.callback.accept(blockOptions.get(0).value);
                    }
                }
            });
            this.updatePanelOptions(searchText);
            panel.add(textBox);
            panel.add(selectButton);
            panel.add(blockOptionsPanel);
        }
        
        void setCallback(Consumer<BlockState> callback) {
            this.callback = callback;
        }
        
        private void updatePanelOptions(String filter) {
            this.blockOptionsPanel.getContainer().removeAll(blockOptions);
            this.blockOptions.clear();
            List<BlockState> blockStates = GlobalBlockStateMap.getBlockTypes();
            blockStates.sort((a, b) -> a.toString().length() - b.toString().length());
            int index = 0;
            for (BlockState blockState : blockStates) {
                if (!blockState.name.contains(filter)) {
                    continue;
                }
                if (blockOptions.size() >= MAX_OPTIONS) {
                    break;
                }
                BlockOptionLabel option = new BlockOptionLabel(this, blockState);
                option.setPosition(10, index * 35 + 5);
                this.blockOptions.add(option);
                index++;
            }
            this.blockOptionsPanel.getContainer().addAll(blockOptions);
        }
        
    }
    
    private static class BlockOptionLabel extends Label {
        
        private final BlockState value;
        
        BlockOptionLabel(SelectBlockWidget widget, BlockState value) {
            super(value.toString());
            this.setSize(SelectBlockWidget.WIDTH - 100, 30);
            this.value = value;
            this.getStyle().getBackground().setColor(.8f, .8f, .8f, 1);
            this.getStyle().setBorder(new SimpleLineBorder(new Vector4f(1, 0, 0, 1), 3));
            this.getStyle().getBorder().setEnabled(false);
            this.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) (event) -> {
                if (event.getAction() == MouseClickEvent.MouseClickAction.CLICK) {
                    for (BlockOptionLabel blockOption : widget.blockOptions) {
                        blockOption.getStyle().getBorder().setEnabled(false);
                    }
                    this.getStyle().getBorder().setEnabled(true);
                    widget.selectedBlock = value;
                }
            });
        }
        
    }
    
}
