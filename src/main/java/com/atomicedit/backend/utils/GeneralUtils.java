
package com.atomicedit.backend.utils;

import com.atomicedit.backend.BlockCoord;
import java.util.BitSet;

/**
 *
 * @author Justin Bonner
 */
public class GeneralUtils {
    
    
    public static int readIntFromPackedArray(int elementSize, int offset, long[] source){
        int bitOffset = elementSize * offset;
        int arrayElementSize = 64;
        int arrayIndex = bitOffset / arrayElementSize;
        int bitOffsetInArrayElement = bitOffset % arrayElementSize;
        int result = 0;
        for(int i = 0; i < elementSize; i++){
            int bit = (int)(source[arrayIndex] >> (arrayElementSize - bitOffsetInArrayElement - 1) & 1);
            result = result << 1;
            result += bit;
            arrayIndex++;
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
    
}
