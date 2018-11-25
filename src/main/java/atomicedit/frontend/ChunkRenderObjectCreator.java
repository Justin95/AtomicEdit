
package atomicedit.frontend;

import atomicedit.backend.GlobalBlockTypeMap;
import atomicedit.backend.chunk.Chunk;
import atomicedit.backend.chunk.ChunkReader;
import atomicedit.backend.chunk.ChunkSection;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.utils.GeneralUtils;
import atomicedit.frontend.render.RenderObject;
import atomicedit.frontend.render.shaders.DataBufferLayoutFormat;
import atomicedit.frontend.texture.MinecraftTexture;
import atomicedit.frontend.texture.TextureLoader;
import atomicedit.logging.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class ChunkRenderObjectCreator {
    
    
    public static Collection<RenderObject> createRenderObjects(ChunkReader chunk, Collection<ChunkReader> adjacentChunks){
        ArrayList<RenderObject> renderObjects = new ArrayList<>(Chunk.NUM_CHUNK_SECTIONS_IN_CHUNK);
        ChunkReader xMinus = null;
        ChunkReader xPlus = null;
        ChunkReader zMinus = null;
        ChunkReader zPlus = null;
        for(ChunkReader adjChunk : adjacentChunks){
            if(adjChunk == null) continue;
            try{
                if(adjChunk.getChunkCoord().x > chunk.getChunkCoord().x){
                    xPlus = adjChunk;
                }else if(adjChunk.getChunkCoord().x < chunk.getChunkCoord().x){
                    xMinus = adjChunk;
                }else if(adjChunk.getChunkCoord().z > chunk.getChunkCoord().z){
                    zPlus = adjChunk;
                }else if(adjChunk.getChunkCoord().z < chunk.getChunkCoord().z){
                    zMinus = adjChunk;
                }else{
                    Logger.warning("Tried to use a chunk as its own adjacent chunk while creating render object");
                }
            }catch(MalformedNbtTagException e){
                Logger.error("Could not read chunk while creating render object");
            }
        }
        for(int i = 0; i < Chunk.NUM_CHUNK_SECTIONS_IN_CHUNK; i++){
            try{
                ChunkSectionPlus section = new ChunkSectionPlus(chunk.getBlocks(i),
                                                                xPlus.getBlocks(i),
                                                                xMinus.getBlocks(i),
                                                                zPlus.getBlocks(i),
                                                                zMinus.getBlocks(i),
                                                                i == Chunk.NUM_CHUNK_SECTIONS_IN_CHUNK - 1 ? null : chunk.getBlocks(i + 1),
                                                                i == 0 ? null : chunk.getBlocks(i - 1),
                                                                chunk.getChunkCoord().x,
                                                                i,
                                                                chunk.getChunkCoord().z
                );
                renderObjects.add(createChunkSectionRenderObject(section));
            }catch(MalformedNbtTagException e){
                Logger.error("MalformedNbtTagException while trying to create render object");
            }
        }
        return renderObjects;
    }
    
    private static RenderObject createChunkSectionRenderObject(ChunkSectionPlus section){
        ArrayList<Float> vertexData = new ArrayList<>();
        ArrayList<Short> indicies = new ArrayList<>();
        for(int y = 0; y < ChunkSection.SIDE_LENGTH; y++){
            for(int z = 0; z < ChunkSection.SIDE_LENGTH; z++){
                for(int x = 0; x < ChunkSection.SIDE_LENGTH; x++){
                    if(section.getBlockAt(x, y, z) == 0){ //AIR
                        continue;
                    }
                    createBlockRenderData(x, y, z, section, vertexData, indicies);
                }
            }
        }
        Vector3f pos = new Vector3f(section.xCoord * ChunkSection.SIDE_LENGTH, section.yCoord * ChunkSection.SIDE_LENGTH, section.zCoord * ChunkSection.SIDE_LENGTH);
        return new RenderObject(pos, new Vector3f(0,0,0), TextureLoader.getMinecraftDefaultTexture(), convertFloat(vertexData), convertShort(indicies));
    }
    
    private static void createBlockRenderData(int x, int y, int z, ChunkSectionPlus section, List<Float> vertexData, List<Short> indicies){
        short centerBlock = section.getBlockAt(x, y, z);
        MinecraftTexture texture = TextureLoader.getMinecraftDefaultTexture();
        int textureIndex = texture.getBlockTypeToIndex().get(GlobalBlockTypeMap.getBlockType(centerBlock).name);
        float xMinTex = texture.getTextureCoordX(textureIndex);
        float xMaxTex = xMinTex + texture.getTextureCoordDelta();
        float yMinTex = texture.getTextureCoordY(textureIndex);
        float yMaxTex = yMinTex + texture.getTextureCoordDelta();
        short numVerticies = (short)(vertexData.size() / DataBufferLayoutFormat.NUM_ELEMENTS_PER_VERTEX);
        if(centerBlock != section.getBlockAt(x - 1, y, z)){
            addAll(vertexData, new float[]{x,y,  z,   xMinTex,yMinTex, 1,1,1,1,
                                           x,y,  z+1, xMaxTex,yMinTex, 1,1,1,1,
                                           x,y+1,z+1, xMaxTex,yMaxTex, 1,1,1,1,
                                           x,y+1,z,   xMinTex,yMaxTex, 1,1,1,1
            });
            addFaceIndicies(numVerticies, indicies);
            numVerticies += 4;
        }
        if(centerBlock != section.getBlockAt(x + 1, y, z)){
            addAll(vertexData, new float[]{x+1,y,  z,   xMinTex,yMinTex, 1,1,1,1,
                                           x+1,y,  z+1, xMaxTex,yMinTex, 1,1,1,1,
                                           x+1,y+1,z+1, xMaxTex,yMaxTex, 1,1,1,1,
                                           x+1,y+1,z,   xMinTex,yMaxTex, 1,1,1,1
            });
            addFaceIndicies(numVerticies, indicies);
            numVerticies += 4;
        }
        if(centerBlock != section.getBlockAt(x, y - 1, z)){
            addAll(vertexData, new float[]{x,  y,z,   xMinTex,yMinTex, 1,1,1,1,
                                           x+1,y,z,   xMaxTex,yMinTex, 1,1,1,1,
                                           x+1,y,z+1, xMaxTex,yMaxTex, 1,1,1,1,
                                           x,  y,z+1, xMinTex,yMaxTex, 1,1,1,1
            });
            addFaceIndicies(numVerticies, indicies);
            numVerticies += 4;
        }
        if(centerBlock != section.getBlockAt(x, y + 1, z)){
            addAll(vertexData, new float[]{x,  y+1,z,   xMinTex,yMinTex, 1,1,1,1,
                                           x+1,y+1,z,   xMaxTex,yMinTex, 1,1,1,1,
                                           x+1,y+1,z+1, xMaxTex,yMaxTex, 1,1,1,1,
                                           x,  y+1,z+1, xMinTex,yMaxTex, 1,1,1,1
            });
            addFaceIndicies(numVerticies, indicies);
            numVerticies += 4;
        }
        if(centerBlock != section.getBlockAt(x, y, z - 1)){
            addAll(vertexData, new float[]{x,  y,  z, xMinTex,yMinTex, 1,1,1,1,
                                           x+1,y,  z, xMaxTex,yMinTex, 1,1,1,1,
                                           x+1,y+1,z, xMaxTex,yMaxTex, 1,1,1,1,
                                           x,  y+1,z, xMinTex,yMaxTex, 1,1,1,1
            });
            addFaceIndicies(numVerticies, indicies);
            numVerticies += 4;
        }
        if(centerBlock != section.getBlockAt(x, y, z + 1)){
            addAll(vertexData, new float[]{x,  y,  z+1, xMinTex,yMinTex, 1,1,1,1,
                                           x+1,y,  z+1, xMaxTex,yMinTex, 1,1,1,1,
                                           x+1,y+1,z+1, xMaxTex,yMaxTex, 1,1,1,1,
                                           x,  y+1,z+1, xMinTex,yMaxTex, 1,1,1,1
            });
            addFaceIndicies(numVerticies, indicies);
            numVerticies += 4;
        }
    }
    
    private static void addFaceIndicies(int numVerticies, List<Short> indicies){
        //triangle 1
        indicies.add((short)(0 + numVerticies));
        indicies.add((short)(2 + numVerticies));
        indicies.add((short)(1 + numVerticies));
        //triangle 2
        indicies.add((short)(0 + numVerticies));
        indicies.add((short)(3 + numVerticies));
        indicies.add((short)(2 + numVerticies));
    }
    
    private static void addAll(List<Float> dest, float[] source){
        for(int i = 0; i < source.length; i++){
            dest.add(source[i]);
        }
    }
    
    private static void addAll(List<Short> dest, short[] source){
        for(int i = 0; i < source.length; i++){
            dest.add(source[i]);
        }
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
    
    private static class ChunkSectionPlus{
        
        int xCoord;
        int yCoord;
        int zCoord;
        short[] sectionBlocks;
        short[] secPlusX;
        short[] secMinusX;
        short[] secPlusZ;
        short[] secMinusZ;
        short[] secPlusY;
        short[] secMinusY;
        
        public ChunkSectionPlus(short[] sectionBlocks, short[] secPlusX, short[] secMinusX, short[] secPlusZ, short[] secMinusZ, short[] secPlusY, short[] secMinusY, int x, int y, int z){
            this.sectionBlocks = sectionBlocks;
            this.secPlusX = secPlusX;
            this.secMinusX = secMinusX;
            this.secPlusZ = secPlusZ;
            this.secMinusZ = secMinusZ;
            this.secPlusY = secPlusY;
            this.secMinusY = secMinusY;
            this.xCoord = x;
            this.yCoord = y;
            this.zCoord = z;
        }
        
        public short getBlockAt(int x, int y, int z){
            short[] selectedSection = sectionBlocks;
            int selectionX = x;
            int selectionY = y;
            int selectionZ = z;
            if(x < 0){
                selectionX += ChunkSection.SIDE_LENGTH;
                selectedSection = secMinusX;
            } else if(x > ChunkSection.SIDE_LENGTH){
                selectionX -= ChunkSection.SIDE_LENGTH;
                selectedSection = secPlusX;
            } else if(y < 0){
                selectionY += ChunkSection.SIDE_LENGTH;
                selectedSection = secMinusY;
            }else if(y > ChunkSection.SIDE_LENGTH){
                selectionY -= ChunkSection.SIDE_LENGTH;
                selectedSection = secPlusY;
            }else if(z < 0){
                selectionZ += ChunkSection.SIDE_LENGTH;
                selectedSection = secMinusZ;
            }else if(z > ChunkSection.SIDE_LENGTH){
                selectionZ -= ChunkSection.SIDE_LENGTH;
                selectedSection = secPlusZ;
            }
            if(selectedSection == null){
                return 0; //AIR in empty sections
            }
            return selectedSection[GeneralUtils.getIndexYZX(selectionX, selectionY, selectionZ, ChunkSection.SIDE_LENGTH)];
        }
    }
    
}
