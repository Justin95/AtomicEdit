
package atomicedit.backend.utils;

import atomicedit.backend.BlockCoord;
import atomicedit.logging.Logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

/**
 *
 * @author Justin Bonner
 */
public class GeneralUtils {
    
    /**
     * Read an integer of varying length from an array of longs
     * @param elementSize
     * @param offset
     * @param source
     * @return 
     */
    public static int readIntFromPackedLongArray(int elementSize, int offset, long[] source){
        int bitOffset = elementSize * offset;
        int arrayElementSize = 64; //64 bits in a long
        int arrayIndex = bitOffset / arrayElementSize;
        int bitOffsetInArrayElement = bitOffset % arrayElementSize;
        int result = 0;
        for(int i = 0; i < elementSize; i++){
            int bit = (int)((source[arrayIndex] >>> (bitOffsetInArrayElement)) & 1);
            result = result + (bit << i);
            bitOffsetInArrayElement++;
            if(bitOffsetInArrayElement >= arrayElementSize){
                bitOffsetInArrayElement = 0;
                arrayIndex++;
            }
        }
        return result;
    }
    
    public static short readShortFromPackedArray(int elementSize, int offset, long[] source){
        int bitOffset = elementSize * offset;
        int arrayElementSize = 64;
        int arrayIndex = bitOffset / arrayElementSize;
        int bitOffsetInArrayElement = bitOffset % arrayElementSize;
        short result = 0;
        for(int i = 0; i < elementSize; i++){
            short bit = (short)(source[arrayIndex] >> (arrayElementSize - bitOffsetInArrayElement - 1) & 1);
            result = (short) (result << 1);
            result += bit;
            arrayIndex++;
            if(bitOffsetInArrayElement >= arrayElementSize){
                bitOffsetInArrayElement = 0;
                arrayIndex++;
            }
        }
        return result;
    }
    
    /**
     * Gets the array index for the given x, y, z with ordering YZX.
     * The x, y, and z are relative to the array.
     * This method does not do bounds checking.
     * YZX ordering means the x coordinate increasing by 1 increases the index by 1,
     * the z coordinate increasing by 1 increases the index by xLen, and the
     * y coordinate increasing by 1 increases the index by xLen * xLen.
     * @param x 
     * @param y
     * @param z
     * @param xLen
     * @param zLen
     * @return 
     */
    public static int getIndexYZX(int x, int y, int z, int xLen, int zLen){
        return (y * zLen * xLen) + (z * xLen) + x;
    }
    
    
    /**
     * Gets the array index for the given x, y, z with ordering YZX.
     * This method does not do bounds checking.
     * @param x
     * @param y
     * @param z
     * @param sideLength length of a side, x and z lengths are the same
     * @return 
     */
    public static int getIndexYZX(int x, int y, int z, int sideLength){
        return (y * sideLength * sideLength) + (z * sideLength) + x;
    }
    
    public static BlockCoord getBlockCoordFromIndexYZX(int index, BlockCoord minCoord, int xLen, int zLen){
        if(index < 0 || minCoord == null || xLen < 0 || zLen < 0){
            throw new IllegalArgumentException();
        }
        int x = index % xLen;
        int z = (index / xLen) % zLen;
        int y = (index / xLen) / zLen;
        return new BlockCoord(x + minCoord.x, y + minCoord.y, z + minCoord.z);
    }
    
    /**
     * Gets the array index for the given x, z with ordering z, x.
     * @param x
     * @param z
     * @param xLen length of the x side
     * @return 
     */
    public static int getIndexZX(int x, int z, int xLen){
        return (z * xLen) + x;
    }
    
    /**
     * Create a BitSet that is filled with ones.
     * @param size
     * @return 
     */
    public static BitSet getAllTrueBitSet(int size){
        BitSet bitSet = new BitSet(size);
        bitSet.set(0, size);
        return bitSet;
    }
    
    public static int getXFromIndexYZX(int index, int xLen){
        return index % xLen;
    }
    
    public static int getZFromIndexYZX(int index, int xLen, int zLen){
        return (index / xLen) % zLen;
    }
    
    public static int getYFromIndexYZX(int index, int xLen, int zLen){
        return (index / xLen) / zLen;
    }
    
    public static byte[] decompressGZippedByteArray(byte[] compressed) throws IOException{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressed));
        byte[] temp = new byte[4096];
        try{
            while(gzipInputStream.available() != 0){
                int writtenAmount = gzipInputStream.read(temp);
                if(writtenAmount < 0) break;
                outputStream.write(temp, 0, writtenAmount);
            }
        }catch(Exception e){
            Logger.error("Exception decompressing byte array", e);
        }finally{
            try{
                outputStream.close();
            }catch(IOException e){
                Logger.error("Exception closing stream", e);
            }
        }
        return outputStream.toByteArray();
    }
    
    public static byte[] decompressZippedByteArray(byte[] compressed) throws IOException{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Inflater inflater = new Inflater();
        inflater.setInput(compressed);
        byte[] temp = new byte[4096];
        try{
            while(!inflater.finished()){
                int writtenAmount = inflater.inflate(temp);
                if(writtenAmount < 0) break;
                outputStream.write(temp, 0, writtenAmount);
            }
        }catch(Exception e){
            Logger.error("Exception decompressing byte array", e);
        }finally{
            try{
                outputStream.close();
            }catch(IOException e){
                Logger.error("Exception closing stream", e);
            }
        }
        return outputStream.toByteArray();
    }
    
    public static byte[] compressByteArrayToZip(byte[] uncompressed) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Deflater deflater = new Deflater();
        deflater.setInput(uncompressed);
        byte[] temp = new byte[4096];
        try{
            while(!deflater.finished()){
                int writtenAmount = deflater.deflate(temp);
                if(writtenAmount < 0) break;
                outputStream.write(temp, 0, writtenAmount);
            }
        }catch(Exception e){
            Logger.error("Exception while compressing byte array", e);
        }finally{
            try{
                outputStream.close();
            }catch(IOException e){
                Logger.error("Exception closing stream", e);
            }
        }
        return outputStream.toByteArray();
    }
    
}
