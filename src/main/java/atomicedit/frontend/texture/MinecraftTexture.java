
package atomicedit.frontend.texture;

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
    
    private final Map<String, Integer> blockTypeToIndex;
    private final int textureSquareLength;
    
    public MinecraftTexture(Map<String, BufferedImage> namedTextures, BufferedImage defaultTexture){
        super(createSuperImage(namedTextures, defaultTexture));
        this.blockTypeToIndex = createNameToIndexMap(namedTextures);
        this.textureSquareLength = getSideLengthInTextures(namedTextures.size());
    }
    
    
    public int getIndexFromTextureName(String texName){
        if(!blockTypeToIndex.containsKey(texName)){
            //Logger.warning("Unknown texture name: " + blockName);
            texName = UNKNOWN_TEXTURE_NAME;
        }
        return blockTypeToIndex.get(texName);
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
    
}
