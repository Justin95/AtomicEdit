
package atomicedit.backend.utils;

import java.util.Arrays;

/**
 *
 * @author Justin Bonner
 */
public class BitArray {
    
    private final long[] bitArray;
    private final int sizeInBits;
    
    public BitArray(int size){
        this.sizeInBits = size;
        int backingArraySize = size % 64 == 0 ? size / 64 : (size / 64) + 1;
        this.bitArray = new long[backingArraySize];
    }
    
    public BitArray(int size, boolean initialValue){
        this(size);
        long backedValue = initialValue ? ~0 : 0;
        for(int i = 0; i < bitArray.length; i++){
            bitArray[i] = backedValue; //all ones
        }
    }
    
    private int getBackingArrayIndex(int index){
        return index / 64;
    }
    
    private int getElementInternalIndex(int index){
        return index % 64;
    }
    
    public boolean get(int index){
        if(index < 0 || index >= sizeInBits){
            throw new ArrayIndexOutOfBoundsException("index [" + index + "] is not in bounds. BitArray size: " + size());
        }
        int backingArrayIndex = getBackingArrayIndex(index);
        int internalIndex = getElementInternalIndex(index);
        long backingElement = bitArray[backingArrayIndex];
        byte bit = (byte)((backingElement >> internalIndex) & 1);
        return bit == 1;
    }
    
    public void set(int index, boolean value){
        if(index < 0 || index >= sizeInBits){
            throw new ArrayIndexOutOfBoundsException("index [" + index + "] is not in bounds. BitArray size: " + size());
        }
        int backingArrayIndex = getBackingArrayIndex(index);
        int internalIndex = getElementInternalIndex(index);
        long toSet = bitArray[backingArrayIndex];
        if(value){
            toSet |= 1L << internalIndex;
        }else{
            toSet &= ~(1L << internalIndex);
        }
        bitArray[backingArrayIndex] = toSet;
    }
    
    /**
     * Get the number of bits in this bit array.
     * @return 
     */
    public int size(){
        return this.sizeInBits;
    }
    
    @Override
    public String toString() {
        return Arrays.toString(bitArray); //for debugging
    }
    
}
