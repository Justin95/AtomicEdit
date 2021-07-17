
package atomicedit.frontend.render.blockmodelcreation;

import atomicedit.backend.ChunkSectionCoord;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.ChunkReader;
import atomicedit.backend.chunk.ChunkSection;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.utils.GeneralUtils;
import atomicedit.logging.Logger;

/**
 *
 * @author Justin Bonner
 */
public class ChunkPlus implements BlockVolumeDataProvider {
    
    public final ChunkCoord coord;
    public final ChunkReader centerSection;
    public final ChunkReader secPlusX;
    public final ChunkReader secMinusX;
    public final ChunkReader secPlusZ;
    public final ChunkReader secMinusZ;

    public ChunkPlus(ChunkReader section, ChunkReader secPlusX, ChunkReader secMinusX, ChunkReader secPlusZ, ChunkReader secMinusZ, ChunkCoord coord) {
        this.centerSection = section;
        this.secPlusX = secPlusX;
        this.secMinusX = secMinusX;
        this.secPlusZ = secPlusZ;
        this.secMinusZ = secMinusZ;
        this.coord = coord;
    }
    
    @Override
    public int getBlockAt(int x, int y, int z){
        return getIntAt(x, y, z, ChunkPlus::getBlockAt);
    }
    
    @Override
    public int getTotalLightAt(int x, int y, int z){
        int blockLight = getIntAt(x, y, z, ChunkPlus::getBlockLightAt);
        int skyLight = getIntAt(x, y, z, ChunkPlus::getSkyLightAt);
        return blockLight > skyLight ? blockLight : skyLight;
    }
    
    private int getIntAt(int x, int y, int z, GetIntegerOp getOp){
        ChunkReader selectedChunk = centerSection;
        int selectionX = x;
        int selectionY = ChunkSectionCoord.getRelativeChunkSectionYFromWorldY(y);
        int selectionZ = z;
        if(x < 0){
            selectionX += ChunkSection.SIDE_LENGTH;
            selectedChunk = secMinusX;
        } else if(x >= ChunkSection.SIDE_LENGTH){
            selectionX -= ChunkSection.SIDE_LENGTH;
            selectedChunk = secPlusX;
        } else if(z < 0){
            selectionZ += ChunkSection.SIDE_LENGTH;
            selectedChunk = secMinusZ;
        }else if(z >= ChunkSection.SIDE_LENGTH){
            selectionZ -= ChunkSection.SIDE_LENGTH;
            selectedChunk = secPlusZ;
        }
        
        ChunkSection selectedSection = null;
        final int minSectionY = 0;
        final int maxSectionY = 15;
        int sectionY = ChunkSectionCoord.getChunkSectionYFromWorldY(y);
        try {
            if (sectionY >= minSectionY && sectionY <= maxSectionY) {
                selectedSection = selectedChunk == null ? null : selectedChunk.getChunkSection(sectionY);
            }
        } catch (MalformedNbtTagException e) {
            Logger.error("Exception creating chunk renderable", e);
            return 0;
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
