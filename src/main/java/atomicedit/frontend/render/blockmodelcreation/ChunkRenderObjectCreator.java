
package atomicedit.frontend.render.blockmodelcreation;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.ChunkSectionCoord;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.chunk.Chunk;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.ChunkReader;
import atomicedit.backend.chunk.ChunkSection;
import atomicedit.backend.entity.Entity;
import atomicedit.backend.entity.EntityCoord;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.frontend.render.ChunkSectionRenderObject;
import atomicedit.frontend.render.LinesRenderObject;
import atomicedit.frontend.render.NoTextureRenderObject;
import atomicedit.frontend.render.OnlyPositionRenderObject;
import atomicedit.frontend.render.RenderObject;
import atomicedit.frontend.render.blockmodelcreation.BlockModelCreator;
import atomicedit.frontend.render.blockmodelcreation.ChunkSectionPlus;
import atomicedit.logging.Logger;
import atomicedit.utils.FloatList;
import atomicedit.utils.IntList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 *
 * @author Justin Bonner
 */
public class ChunkRenderObjectCreator {
    
    private static final short[] EMPTY_BLOCK_ARRAY = new short[ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION];
    private static final Vector4f BLOCK_ENTITY_COLOR = new Vector4f(.8f, .5f, .20f, .5f); //RGBA
    private static final Vector4f ENTITY_COLOR = new Vector4f(.8f, .20f, .20f, .5f); //RGBA
    
    /**
     * Create a collection of Render Objects which together render a chunk.
     * @param chunk
     * @param xMinus
     * @param xPlus
     * @param zMinus
     * @param zPlus
     * @return 
     */
    public static Collection<ChunkSectionRenderObject> createRenderObjects(ChunkReader chunk, ChunkReader xMinus, ChunkReader xPlus, ChunkReader zMinus, ChunkReader zPlus){
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
            }catch(MalformedNbtTagException e){
                Logger.warning("MalformedNbtTagException while trying to create render object", e);
            }catch(Exception e){
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
    
    public static Collection<RenderObject> createMiscRenderObjects(ChunkReader chunk) {
        final ChunkCoord chunkCoord;
        try {
            chunkCoord = chunk.getChunkCoord();
        } catch (MalformedNbtTagException e) {
            Logger.warning("Could not render all of chunk.", e);
            return new ArrayList<>();
        }
        List<RenderObject> miscRenderObjects = new ArrayList<>();
        //create block entity render object
        FloatList vertexData = new FloatList();
        IntList faceIndicies = new IntList();
        IntList lineIndicies = new IntList();
        List<BlockEntity> blockEntities;
        try {
            blockEntities = chunk.getBlockEntities();
        } catch (MalformedNbtTagException e) {
            Logger.warning("Could not render Block Entities in chunk.", e);
            blockEntities = new ArrayList<>();
        }
        for (BlockEntity blockEntity : blockEntities) {
            BlockCoord coord;
            try {
                coord = blockEntity.getBlockCoord();
            } catch (MalformedNbtTagException e) {
                Logger.warning("Could not render Block Entity in chunk.", e);
                continue;
            }
            addBlockEntity(vertexData, faceIndicies, lineIndicies, coord);
        }
        Vector3f pos = new Vector3f(chunkCoord.getMinBlockCoord().x, chunkCoord.getMinBlockCoord().y, chunkCoord.getMinBlockCoord().z);
        miscRenderObjects.add(
            new OnlyPositionRenderObject(pos, new Vector3f(0,0,0), BLOCK_ENTITY_COLOR, true, vertexData.asArray(), faceIndicies.asArray())
        );
        miscRenderObjects.add(
            new LinesRenderObject(pos, new Vector3f(0,0,0), new Vector4f(0,0,0,1f), false, vertexData.asArray(), lineIndicies.asArray())
        );
        //create entity render object
        vertexData.reset();
        faceIndicies.reset();
        lineIndicies.reset();
        List<Entity> entities;
        try {
            entities = chunk.getEntities();
        } catch (MalformedNbtTagException e) {
            Logger.warning("Could not render Entities in chunk.", e);
            entities = new ArrayList<>();
        }
        for (Entity entity : entities) {
            Vector3f entityPos;
            EntityCoord coord;
            try {
                coord = entity.getCoord();
            } catch (MalformedNbtTagException e) {
                Logger.warning("Could not render Entity in chunk.", e);
                continue;
            }
            entityPos = coord.getChunkRelativePosition();
            addEntity(vertexData, faceIndicies, lineIndicies, entityPos);
        }
        
        miscRenderObjects.add(
            new OnlyPositionRenderObject(pos, new Vector3f(0,0,0), ENTITY_COLOR, true, vertexData.asArray(), faceIndicies.asArray())
        );
        miscRenderObjects.add(
            new LinesRenderObject(pos, new Vector3f(0,0,0), new Vector4f(0,0,0,1f), false, vertexData.asArray(), lineIndicies.asArray())
        );
        return miscRenderObjects;
    }
    
    private static void addBlockEntity(FloatList vertexData, IntList faceIndicies, IntList lineIndicies, BlockCoord coord) {
        final float buffer = 0.02f;
        final float xMin = coord.getChunkLocalX() - buffer;
        final float yMin = coord.y                - buffer;
        final float zMin = coord.getChunkLocalZ() - buffer;
        final float xMax = coord.getChunkLocalX() + 1 + buffer;
        final float yMax = coord.y                + 1 + buffer;
        final float zMax = coord.getChunkLocalZ() + 1 + buffer;
        makeCube(xMin, yMin, zMin, xMax, yMax, zMax, vertexData, faceIndicies, lineIndicies);
    }
    
    private static void addEntity(FloatList vertexData, IntList faceIndicies, IntList lineIndicies, Vector3f pos) {
        final float buffer = 0.02f;
        final float width = .5f;
        final float xMin = pos.x - width - buffer;
        final float yMin = pos.y - buffer;
        final float zMin = pos.z - width - buffer;
        final float xMax = pos.x + width + buffer;
        final float yMax = pos.y + (2 * width) + buffer; //bottom center of box is the entity coord
        final float zMax = pos.z + width + buffer;
        makeCube(xMin, yMin, zMin, xMax, yMax, zMax, vertexData, faceIndicies, lineIndicies);
    }
    
    private static void makeCube(
        float xMin, float yMin, float zMin,
        float xMax, float yMax, float zMax,
        FloatList vertexData,
        IntList faceIndicies,
        IntList lineIndicies
    ) {
        final int numVerticies = vertexData.size() / 3;
        vertexData.addAll(
            xMin, yMin, zMin,
            xMin, yMin, zMax,
            xMin, yMax, zMin,
            xMin, yMax, zMax,
            xMax, yMin, zMin,
            xMax, yMin, zMax,
            xMax, yMax, zMin,
            xMax, yMax, zMax
        );
        
        //add num verticies, not vertex data size
        faceIndicies.addAll(
            numVerticies + 0, numVerticies + 1, numVerticies + 3,   numVerticies + 0, numVerticies + 3, numVerticies + 2,  //x = 0  face
            numVerticies + 0, numVerticies + 4, numVerticies + 5,   numVerticies + 0, numVerticies + 5, numVerticies + 1,  //y = 0  face
            numVerticies + 0, numVerticies + 2, numVerticies + 6,   numVerticies + 0, numVerticies + 6, numVerticies + 4,  //z = 0  face
            numVerticies + 4, numVerticies + 6, numVerticies + 7,   numVerticies + 4, numVerticies + 7, numVerticies + 5,  //x = 1  face
            numVerticies + 2, numVerticies + 3, numVerticies + 7,   numVerticies + 2, numVerticies + 7, numVerticies + 6,  //y = 1  face
            numVerticies + 1, numVerticies + 5, numVerticies + 7,   numVerticies + 1, numVerticies + 7, numVerticies + 3   //z = 1  face
            //render both sides of the triangles
            /*
            3,1,0,  2,3,0,
            5,4,0,  1,5,0,
            6,2,0,  4,6,0,
            7,6,4,  5,7,4,
            7,3,2,  6,7,2,
            7,5,1,  3,7,1
            */
        );
        lineIndicies.addAll(
            numVerticies + 0, numVerticies + 1,   numVerticies + 0, numVerticies + 2,   numVerticies + 0, numVerticies + 4,
            numVerticies + 1, numVerticies + 3,   numVerticies + 1, numVerticies + 5,
            numVerticies + 2, numVerticies + 3,   numVerticies + 2, numVerticies + 6,
            numVerticies + 3, numVerticies + 7,
            numVerticies + 4, numVerticies + 5,   numVerticies + 4, numVerticies + 6,
            numVerticies + 5, numVerticies + 7,
            numVerticies + 6, numVerticies + 7
        );
    }
    
}
