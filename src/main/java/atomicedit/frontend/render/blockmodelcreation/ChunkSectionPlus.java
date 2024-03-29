
package atomicedit.frontend.render.blockmodelcreation;

import atomicedit.backend.chunk.ChunkSection;
import atomicedit.backend.utils.GeneralUtils;

/**
 *
 * @author Justin Bonner
 */
public class ChunkSectionPlus implements BlockVolumeDataProvider {
    
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

    public ChunkSectionPlus(ChunkSection section, ChunkSection secPlusX, ChunkSection secMinusX, ChunkSection secPlusZ,
                            ChunkSection secMinusZ, ChunkSection secPlusY, ChunkSection secMinusY, int x, int y, int z){
        this.centerSection = section;
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
    
    @Override
    public int getBlockAt(int x, int y, int z){
        return getIntAt(x, y, z, ChunkSectionPlus::getBlockAt);
    }
    
    @Override
    public int getTotalLightAt(int x, int y, int z){
        int blockLight = getIntAt(x, y, z, ChunkSectionPlus::getBlockLightAt);
        int skyLight = getIntAt(x, y, z, ChunkSectionPlus::getSkyLightAt);
        return blockLight > skyLight ? blockLight : skyLight;
    }
    
    private int getIntAt(int x, int y, int z, GetIntegerOp getOp){
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
        return getOp.getIntegerOp(selectionX, selectionY, selectionZ, selectedSection);
    }
    
    private static int getBlockLightAt(int x, int y, int z, ChunkSection selectedSection){
        if(selectedSection == null){
            return 0;
        }
        byte[] light = selectedSection.getBlockLightValues();
        return getLightAt(x, y, z, light);
    }
    
    private static int getSkyLightAt(int x, int y, int z, ChunkSection selectedSection){
        if(selectedSection == null){
            return 15;
        }
        byte[] light = selectedSection.getSkyLightValues();
        return getLightAt(x, y, z, light);
    }
    
    private static int getLightAt(int x, int y, int z, byte[] light){
        int totalSkyLightIndex = GeneralUtils.getIndexYZX(x, y, z, ChunkSection.SIDE_LENGTH);
        int index = totalSkyLightIndex / 2;
        int offset = totalSkyLightIndex % 2;
        byte lightVal = (byte)((light[index] >> (offset * 4)) & 0xF);
        return lightVal;
    }
    
    private static int getBlockAt(int x, int y, int z, ChunkSection selectedSection){
        if(selectedSection == null){
            return 0; //AIR in empty sections
        }
        return selectedSection.getBlockIds()[GeneralUtils.getIndexYZX(x, y, z, ChunkSection.SIDE_LENGTH)];
    }
    
    private static interface GetIntegerOp{
        int getIntegerOp(int x, int y, int z, ChunkSection section);
    }
    
}
