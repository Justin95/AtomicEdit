
package atomicedit.backend.worldformats;

import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.logging.Logger;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utility functions for parsing minecraft world files.
 * @author Justin Bonner
 */
public class WorldFormatUtils {
    
    public static String getRegionFileName(ChunkCoord chunkCoord){
        return "r." + (int) Math.floor(chunkCoord.x / 32.0) + "." + (int) Math.floor(chunkCoord.z / 32.0) + ".mca";
    }
    
    public static DataInputStream getRegionAsDataInputStream(String dimensionFilepath, ChunkCoord chunkCoord){
        String filepath = dimensionFilepath + "/region/" + getRegionFileName(chunkCoord);
        DataInputStream regionInput = null;
        try{
            regionInput = new DataInputStream(new FileInputStream(filepath));
        }catch(FileNotFoundException e){
            Logger.info("Region file not found, chunk not generated yet: " + chunkCoord);
        }
        return regionInput;
    }
    
    public static DataInputStream getRegionAsDataInputStream(String dimensionFilepath, String regionFileName) throws IOException {
        String filepath = dimensionFilepath + "/region/" + regionFileName;
        DataInputStream regionInput = null;
        try{
            byte[] rawRegionFile = Files.readAllBytes(Paths.get(filepath));
            regionInput = new DataInputStream(new ByteArrayInputStream(rawRegionFile));
        }catch(FileNotFoundException e){
            Logger.info("Region file not found " + regionFileName + ", chunk not generated yet.");
        }
        return regionInput;
    }
    
}
