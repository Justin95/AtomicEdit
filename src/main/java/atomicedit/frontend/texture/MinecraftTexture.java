
package atomicedit.frontend.texture;

import atomicedit.logging.Logger;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class MinecraftTexture extends Texture{
    
    public static final int TEXTURE_RES = 16;
    public static final String UNKNOWN_TEXTURE_NAME = "ATOMICEDIT_UNKNOWN_TEXTURE";
    
    private final Map<String, Integer> texNameToIndex;
    private final boolean[] textureIndexIsTranslucent;
    private final int textureSquareLength;
    
    public MinecraftTexture(Map<String, BufferedImage> namedTextures, BufferedImage defaultTexture){
        super(createSuperImage(namedTextures, defaultTexture));
        this.texNameToIndex = createNameToIndexMap(namedTextures);
        this.textureSquareLength = getSideLengthInTextures(namedTextures.size());
        this.textureIndexIsTranslucent = calculateTranslucency(namedTextures, texNameToIndex);
    }
    
    
    public int getIndexFromTextureName(String texName){
        if(!texNameToIndex.containsKey(texName)){
            texName = UNKNOWN_TEXTURE_NAME;
        }
        return texNameToIndex.get(texName);
    }
    
    public int getBlockTextureLength(){
        return this.textureSquareLength;
    }
    
    public float getTextureCoordDelta(){
        return 1 / (float)textureSquareLength;
    }
    
    public float getTextureCoordX(int index){
        int x = index % textureSquareLength;
        return x / (float)textureSquareLength;
    }
    
    public float getTextureCoordY(int index){
        int y = index / textureSquareLength;
        return y / (float)textureSquareLength;
    }
    
    public static BufferedImage testCreateSuperImage(Map<String, BufferedImage> namedTextures, BufferedImage defaultTexture){
        return createSuperImage(namedTextures, defaultTexture);
    }
    
    private static BufferedImage createSuperImage(Map<String, BufferedImage> namedTextures, BufferedImage defaultTexture){
        int numTextures = namedTextures.size();
        int superTextureSideLength = getSideLengthInTextures(numTextures);
        int superTextureRes = TEXTURE_RES * superTextureSideLength;
        BufferedImage superTexture = new BufferedImage(superTextureRes, superTextureRes, BufferedImage.TYPE_INT_ARGB);
        List<String> names = getTextureNames(namedTextures);
        outer:
        for(int y = 0; y < superTextureSideLength; y++){
            for(int x = 0; x < superTextureSideLength; x++){
                if((y * superTextureSideLength) + x >= names.size()){
                    break outer;
                }
                String name = names.get((y * superTextureSideLength) + x);
                BufferedImage texture = namedTextures.containsKey(name) ? namedTextures.get(name) : defaultTexture;
                superTexture.getGraphics().drawImage(texture,
                                                     x * TEXTURE_RES,
                                                     y * TEXTURE_RES,
                                                    TEXTURE_RES,
                                                    TEXTURE_RES,
                                                     null);
            }
        }
        return superTexture;
    }
    
    private static int getSideLengthInTextures(int numTextures){
        return (int)Math.ceil(Math.sqrt(numTextures));
    }
    
    private static Map<String, Integer> createNameToIndexMap(Map<String, BufferedImage> namedTextures){
        List<String> names = getTextureNames(namedTextures);
        Map<String, Integer> map = new HashMap<>();
        for(int i = 0; i < names.size(); i++){
            map.put(names.get(i), i);
        }
        return map;
    }
    
    private static List<String> getTextureNames(Map<String, BufferedImage> namedTextures){
        List<String> names = new ArrayList<>(namedTextures.keySet());
        names.add(UNKNOWN_TEXTURE_NAME);
        return names;
    }
    
    public boolean isTextureTranslucent(int texIndex){
        return this.textureIndexIsTranslucent[texIndex];
    }
    
    private static boolean[] calculateTranslucency(Map<String, BufferedImage> namedTextures, Map<String, Integer> texNameToIndex){
        boolean[] translucencies = new boolean[namedTextures.size() + 1];
        outer:
        for(String texName : namedTextures.keySet()){
            BufferedImage tex = namedTextures.get(texName);
            for(int x = 0; x < tex.getWidth(); x++){
                for(int y = 0; y < tex.getHeight(); y++){
                    int alpha = ((tex.getRGB(x, y) >> 24) & 0xFF);
                    if(alpha > 0 && alpha < 255){ //not completely opaque and not completely transparent
                        translucencies[texNameToIndex.get(texName)] = true;
                        Logger.info("Translucent texture: " + texName);
                        continue outer;
                    }
                }
            }
        }
        return translucencies;
    }
    
}
