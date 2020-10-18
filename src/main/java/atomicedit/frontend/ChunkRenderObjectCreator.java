
package atomicedit.frontend;

import atomicedit.backend.ChunkSectionCoord;
import atomicedit.backend.chunk.Chunk;
import atomicedit.backend.chunk.ChunkSection;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.frontend.render.ChunkSectionRenderObject;
import atomicedit.frontend.render.blockmodelcreation.BlockModelCreator;
import atomicedit.frontend.render.blockmodelcreation.ChunkSectionPlus;
import atomicedit.logging.Logger;
import atomicedit.utils.FloatList;
import atomicedit.utils.IntList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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
    public static Collection<ChunkSectionRenderObject> createRenderObjects(Chunk chunk, Chunk xMinus, Chunk xPlus, Chunk zMinus, Chunk zPlus){
        ArrayList<ChunkSectionRenderObject> renderObjects = new ArrayList<>();
        FloatList vertexBuffer = new FloatList();
        IntList indiciesBuffer = new IntList();
        for(int i = 0; i < Chunk.NUM_CHUNK_SECTIONS_IN_CHUNK; i++){
            try{
                if(Arrays.equals(chunk.getBlocks(i), EMPTY_BLOCK_ARRAY)){
                    continue;
                }
                ChunkSectionPlus section = new ChunkSectionPlus(
                    chunk.getChunkSection(i),
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
                ChunkSectionRenderObject sectionRenderObject = createChunkSectionRenderObject(section, true, vertexBuffer, indiciesBuffer);
                if(sectionRenderObject != null){
                    renderObjects.add(sectionRenderObject);
                }
                vertexBuffer.reset();
                indiciesBuffer.reset();
                sectionRenderObject = createChunkSectionRenderObject(section, false, vertexBuffer, indiciesBuffer);
                if(sectionRenderObject != null){
                    renderObjects.add(sectionRenderObject);
                }
            }catch (MalformedNbtTagException e){
                Logger.warning("MalformedNbtTagException while trying to create render object", e);
            }catch (Exception e){
                Logger.warning("Exception while trying to create chunk render object", e);
            }
            vertexBuffer.reset();
            indiciesBuffer.reset();
        }
        return renderObjects;
    }
    
    /**
     * Create a render object for a chunk section.
     * @param section
     * @return A render object to allow rendering of a chunk section
     */
    private static ChunkSectionRenderObject createChunkSectionRenderObject(ChunkSectionPlus section, boolean onlyTranslucent, FloatList vertexData, IntList indicies){
        BlockModelCreator blockModelCreator = BlockModelCreator.getInstance();
        for(int y = 0; y < ChunkSection.SIDE_LENGTH; y++){
            for(int z = 0; z < ChunkSection.SIDE_LENGTH; z++){
                for(int x = 0; x < ChunkSection.SIDE_LENGTH; x++){
                    if(section.getBlockAt(x, y, z) == 0){ //AIR
                        continue;
                    }
                    blockModelCreator.addBlockRenderData(x, y, z, section, vertexData, indicies, onlyTranslucent);
                }
            }
        }
        if(vertexData.isEmpty()){
            return null;
        }
        return new ChunkSectionRenderObject(
            new ChunkSectionCoord(section.xCoord, section.yCoord, section.zCoord),
            onlyTranslucent,
            vertexData.asArray(),
            indicies.asArray()
        );
    }
    
    
    
}
