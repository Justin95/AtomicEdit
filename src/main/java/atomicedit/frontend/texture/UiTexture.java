package atomicedit.frontend.texture;

import atomicedit.logging.Logger;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.joml.Vector2f;

/**
 *
 * @author justin
 */
public class UiTexture {
    
    public static final UiTexture AREA_SELECT_ICON = new UiTexture("/icons/area_select_icon.png");
    public static final UiTexture BRUSH_ICON = new UiTexture("/icons/brush_icon.png");
    public static final UiTexture SCHEMATIC_ICON = new UiTexture("/icons/schematic_icon.png");
    public static final UiTexture ENTITY_ICON = new UiTexture("/icons/entity_icon.png");
    public static final UiTexture BLOCK_ENTITY_ICON = new UiTexture("/icons/area_select_icon.png");
    public static final UiTexture NBT_ICON = new UiTexture("/icons/nbt_icon.png");
    public static final UiTexture FLIP_ICON = new UiTexture("/icons/flip.png");
    public static final UiTexture ROTATE_LEFT_ICON = new UiTexture("/icons/rotate_left.png");
    public static final UiTexture ROTATE_RIGHT_ICON = new UiTexture("/icons/rotate_right.png");
    
    private static List<UiTexture> UI_TEXTURES = new ArrayList<>();
    
    static {
        UI_TEXTURES.add(AREA_SELECT_ICON);
        UI_TEXTURES.add(BRUSH_ICON);
        UI_TEXTURES.add(SCHEMATIC_ICON);
        UI_TEXTURES.add(ENTITY_ICON);
        UI_TEXTURES.add(BLOCK_ENTITY_ICON);
        UI_TEXTURES.add(NBT_ICON);
        UI_TEXTURES.add(FLIP_ICON);
        UI_TEXTURES.add(ROTATE_LEFT_ICON);
        UI_TEXTURES.add(ROTATE_RIGHT_ICON);
    }
    
    private String filepath;
    private BufferedImage tmpImage;
    private boolean initialized;
    private Texture texture;
    private Vector2f uvLow;
    private Vector2f uvHigh;
    
    private UiTexture(String filepath) {
        this.filepath = filepath;
        initialized = false;
        texture = null;
    }
    
    public Texture getTexture() {
        return this.texture;
    }
    
    public Vector2f getUvLow() {
        return uvLow;
    }
    
    public Vector2f getUvHigh() {
        return uvHigh;
    }
    
    public static void initialize() {
        int maxHeight = 0;
        int totalWidth = 0;
        for (UiTexture tex : UI_TEXTURES) {
            BufferedImage texImg = readImage(tex.filepath);
            tex.tmpImage = texImg;
            maxHeight = Math.max(maxHeight, texImg.getHeight());
            totalWidth += texImg.getWidth();
        }
        BufferedImage superTexture = new BufferedImage(totalWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
        int x = 0;
        for (UiTexture tex : UI_TEXTURES) {
            BufferedImage texImg = tex.tmpImage;
            tex.tmpImage = null;
            tex.uvLow = new Vector2f(x / (float)totalWidth, 0);
            int texLowX = x;
            x += texImg.getWidth();
            tex.uvHigh = new Vector2f(x / (float)totalWidth, 1);
            x++;
            superTexture.getGraphics().drawImage(
                texImg,
                texLowX,
                0,
                texImg.getWidth(),
                texImg.getHeight(),
                null
            );
        }
        Texture texture = new Texture(superTexture);
        texture.initialize();
        for (UiTexture tex : UI_TEXTURES) {
            tex.texture = texture;
            tex.initialized = true;
        }
    }
    
    private static BufferedImage readImage(String filepath) {
        try {
            return ImageIO.read(UiTexture.class.getResourceAsStream(filepath));
        } catch(Exception e) {
            Logger.error("Could not read file: `" + filepath + "`", e);
            throw new RuntimeException(e);
        }
    }
    
}
