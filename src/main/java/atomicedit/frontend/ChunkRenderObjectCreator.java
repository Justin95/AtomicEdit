
package atomicedit.frontend;

import atomicedit.backend.chunk.Chunk;
import atomicedit.backend.chunk.ChunkReader;
import atomicedit.backend.chunk.ChunkSection;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.frontend.render.RenderObject;
import atomicedit.frontend.render.blockmodelcreation.BlockModelCreator;
import atomicedit.frontend.render.blockmodelcreation.ChunkSectionPlus;
import atomicedit.jarreading.texture.TextureLoader;
import atomicedit.logging.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class ChunkRenderObjectCreator {
    
    private static final short[] EMPTY_BLOCK_ARRAY = new short[ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION];
    
    /**
     * Create a collection of Render Objects which together render a chunk.
     * @param chunk
     * @param xMinus
     * @param xPlus
     * @param zMinus
     * @param zPlus
     * @return 
     */
    public static Collection<RenderObject> createRenderObjects(ChunkReader chunk, ChunkReader xMinus, ChunkReader xPlus, ChunkReader zMinus, ChunkReader zPlus){
        ArrayList<RenderObject> renderObjects = new ArrayList<>(Chunk.NUM_CHUNK_SECTIONS_IN_CHUNK);
        for(int i = 0; i < Chunk.NUM_CHUNK_SECTIONS_IN_CHUNK; i++){
            try{
                if(Arrays.equals(chunk.getBlocks(i), EMPTY_BLOCK_ARRAY)){
                    continue;
                }
                ChunkSectionPlus section = new ChunkSectionPlus(chunk.getChunkSection(i),
                                                                xPlus != null ? xPlus.getChunkSection(i) : null,
                                                                xMinus != null ? xMinus.getChunkSection(i) : null,
                                                                zPlus != null ? zPlus.getChunkSection(i) : null,
                                                                zMinus != null ? zMinus.getChunkSection(i) : null,
                                                                i == Chunk.NUM_CHUNK_SECTIONS_IN_CHUNK - 1 ? null : chunk.getChunkSection(i + 1),
                                                                i == 0 ? null : chunk.getChunkSection(i - 1),
                                                                chunk.getChunkCoord().x,
                                                                i,
                                                                chunk.getChunkCoord().z
                );
                renderObjects.add(createChunkSectionRenderObject(section));
            }catch(MalformedNbtTagException e){
                Logger.error("MalformedNbtTagException while trying to create render object", e);
            }
        }
        return renderObjects;
    }
    
    /**
     * Create a render object for a chunk section.
     * @param section
     * @return A render object to allow rendering of a chunk section
     */
    private static RenderObject createChunkSectionRenderObject(ChunkSectionPlus section){
        BlockModelCreator blockModelCreator = BlockModelCreator.getInstance();
        ArrayList<Float> vertexData = new ArrayList<>();
        ArrayList<Short> indicies = new ArrayList<>();
        for(int y = 0; y < ChunkSection.SIDE_LENGTH; y++){
            for(int z = 0; z < ChunkSection.SIDE_LENGTH; z++){
                for(int x = 0; x < ChunkSection.SIDE_LENGTH; x++){
                    if(section.getBlockAt(x, y, z) == 0){ //AIR
                        continue;
                    }
                    blockModelCreator.addBlockRenderData(x, y, z, section, vertexData, indicies);
                }
            }
        }
        Vector3f pos = new Vector3f(section.xCoord * ChunkSection.SIDE_LENGTH, section.yCoord * ChunkSection.SIDE_LENGTH, section.zCoord * ChunkSection.SIDE_LENGTH);
        return new RenderObject(pos, new Vector3f(0,0,0), TextureLoader.getMinecraftDefaultTexture(), convertFloat(vertexData), convertShort(indicies));
    }
    
    private static float[] convertFloat(List<Float> source){
        float[] dest = new float[source.size()];
        for(int i = 0; i < source.size(); i++){
            dest[i] = source.get(i);
        }
        return dest;
    }
    
    private static short[] convertShort(List<Short> source){
        short[] dest = new short[source.size()];
        for(int i = 0; i < source.size(); i++){
            dest[i] = source.get(i);
        }
        return dest;
    }
    
}
