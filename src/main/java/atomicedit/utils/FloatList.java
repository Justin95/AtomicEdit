
package atomicedit.utils;

/**
 * A list for float primitives.
 * @author Justin Bonner
 */
public final class FloatList {
    
    private static final int DEFAULT_INITIAL_SIZE = 100;
    private static final float REALLOC_SCALE = 1.5f;
    
    private float[] data;
    private int size;
    
    public FloatList() {
        data = new float[DEFAULT_INITIAL_SIZE];
        size = 0;
    }
    
    public void add(float f) {
        if (size == data.length - 1) {
            float[] newData = new float[(int)(data.length * REALLOC_SCALE)];
            System.arraycopy(data, 0, newData, 0, data.length);
            this.data = newData;
        }
        data[size] = f;
        size++;
    }
    
    public void addAll(final float... addAll) {
        for (float f : addAll) {
            add(f);
        }
    }
    
    public float[] asArray() {
        float[] arr = new float[size];
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
