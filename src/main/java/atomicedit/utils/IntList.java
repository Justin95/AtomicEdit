
package atomicedit.utils;

/**
 * A list for int primitives.
 * @author Justin Bonner
 */
public final class IntList {
    
    private static final int DEFAULT_INITIAL_SIZE = 100;
    private static final float REALLOC_SCALE = 1.5f;
    
    private int[] data;
    private int size;
    
    public IntList() {
        data = new int[DEFAULT_INITIAL_SIZE];
        size = 0;
    }
    
    public void add(int i) {
        if (size == data.length - 1) {
            int[] newData = new int[(int)(data.length * REALLOC_SCALE)];
            System.arraycopy(data, 0, newData, 0, data.length);
            this.data = newData;
        }
        data[size] = i;
        size++;
    }
    
    public void addAll(final int... addAll) {
        for (int i : addAll) {
            add(i);
        }
    }
    
    public int[] asArray() {
        int[] arr = new int[size];
        System.arraycopy(data, 0, arr, 0, size);
        return arr;
    }
    
    public void reset() {
        this.size = 0;
    }
    
    public int size() {
        return this.size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
}
