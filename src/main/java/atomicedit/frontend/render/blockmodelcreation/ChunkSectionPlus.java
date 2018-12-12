
package atomicedit.frontend.render.blockmodelcreation;

import atomicedit.backend.chunk.ChunkSection;
import atomicedit.backend.utils.GeneralUtils;

/**
 *
 * @author Justin Bonner
 */
public class ChunkSectionPlus {
    public final int xCoord;
    public final int yCoord;
    public final int zCoord;
    public final ChunkSection centerSection;
    public final ChunkSection secPlusX;
    public final ChunkSection secMinusX;
    public final ChunkSection secPlusZ;
    public final ChunkSection secMinusZ;
    public final ChunkSection secPlusY;
    public final ChunkSection secMinusY;

    public ChunkSectionPlus(ChunkSection sectionBlocks, ChunkSection secPlusX, ChunkSection secMinusX, ChunkSection secPlusZ,
                            ChunkSection secMinusZ, ChunkSection secPlusY, ChunkSection secMinusY, int x, int y, int z){
        this.centerSection = sectionBlocks;
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
        ChunkSection selectedSection = centerSection;
        int selectionX = x;
        int selectionY = y;
        int selectionZ = z;
        if(x < 0){
            selectionX += ChunkSection.SIDE_LENGTH;
            selectedSection = secMinusX;
        } else if(x >= ChunkSection.SIDE_LENGTH){
            selectionX -= ChunkSection.SIDE_LENGTH;
            selectedSection = secPlusX;
        } else if(y < 0){
            selectionY += ChunkSection.SIDE_LENGTH;
            selectedSection = secMinusY;
        }else if(y >= ChunkSection.SIDE_LENGTH){
            selectionY -= ChunkSection.SIDE_LENGTH;
            selectedSection = secPlusY;
        }else if(z < 0){
            selectionZ += ChunkSection.SIDE_LENGTH;
            selectedSection = secMinusZ;
        }else if(z >= ChunkSection.SIDE_LENGTH){
            selectionZ -= ChunkSection.SIDE_LENGTH;
            selectedSection = secPlusZ;
        }
        if(selectedSection == null){
            return 0; //AIR in empty sections
        }
        return selectedSection.getBlockIds()[GeneralUtils.getIndexYZX(selectionX, selectionY, selectionZ, ChunkSection.SIDE_LENGTH)];
    }
}
