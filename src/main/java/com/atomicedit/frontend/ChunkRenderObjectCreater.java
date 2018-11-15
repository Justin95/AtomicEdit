
package com.atomicedit.frontend;

import com.atomicedit.backend.chunk.ChunkReader;
import com.atomicedit.backend.chunk.ChunkSection;
import com.atomicedit.backend.utils.GeneralUtils;
import com.atomicedit.frontend.render.RenderObject;
import java.util.Collection;

/**
 *
 * @author Justin Bonner
 */
public class ChunkRenderObjectCreater {
    
    /*
    public static Collection<RenderObject> createRenderObjects(ChunkReader chunk, Collection<ChunkReader> adjacentChunks){
        
    }
    
    private static RenderObject createChunkSectionRenderObject(ChunkSectionPlus section){
        int totalVerticies = 0;
        int totalIndicies = 0;
        for(int y = 0; y < ChunkSection.SIDE_LENGTH; y++){
            for(int z = 0; z < ChunkSection.SIDE_LENGTH; z++){
                for(int x = 0; x < ChunkSection.SIDE_LENGTH; x++){
                    
                }
            }
        }
    }
    
    
    private static class ChunkSectionPlus{
        
        short[] sectionBlocks;
        short[] secPlusX;
        short[] secMinusX;
        short[] secPlusZ;
        short[] secMinusZ;
        short[] secPlusY;
        short[] secMinusY;
        
        public ChunkSectionPlus(short[] sectionBlocks, short[] secPlusX, short[] secMinusX, short[] secPlusZ, short[] secMinusZ, short[] secPlusY, short[] secMinusY){
            this.sectionBlocks = sectionBlocks;
            this.secPlusX = secPlusX;
            this.secMinusX = secMinusX;
            this.secPlusZ = secPlusZ;
            this.secMinusZ = secMinusZ;
            this.secPlusY = secPlusY;
            this.secMinusY = secMinusY;
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
            } else if(){
                
            }
            return selectedSection[GeneralUtils.getIndexYZX(selectionX, selectionY, selectionZ, ChunkSection.SIDE_LENGTH)];
        }
    }
    */
}
