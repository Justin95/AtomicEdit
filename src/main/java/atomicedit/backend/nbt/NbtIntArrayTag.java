
package atomicedit.backend.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author Justin Bonner
 */
public class NbtIntArrayTag extends NbtTag{
    
    private final int[] data;
    private final int dataSize;
    
    public NbtIntArrayTag(DataInputStream input, boolean readName) throws IOException{
        super(NbtTypes.TAG_INT_ARRAY, readName ? NbtTag.readUtfString(input) : "");
        this.dataSize = input.readInt();
        this.data = new int[dataSize];
        for(int i = 0; i < dataSize; i++){
            data[i] = input.readInt();
        }
    }
    
    public NbtIntArrayTag(String name, int[] data){
        super(NbtTypes.TAG_INT_ARRAY, name);
        this.data = data;
        this.dataSize = data.length;
    }
    
    @Override
    protected void write(DataOutputStream output) throws IOException{
        output.writeInt(dataSize);
        for(int i = 0; i < dataSize; i++){
            output.writeInt(data[i]);
        }
    }
    
    public int[] getPayload(){
        return this.data;
    }
    
    public int getPayloadSize(){
        return this.dataSize;
    }
    
    @Override
    public NbtIntArrayTag copy() {
        return new NbtIntArrayTag(name, Arrays.copyOf(data, data.length));
    }
    
    @Override
    public String toString(int indent){
        return String.format("%"+indent+"s", "") + this.getName() + ":" + Arrays.toString(data);
    }
    
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NbtIntArrayTag)) {
            return false;
        }
        if (this == other) {
            return true;
        }
        NbtIntArrayTag otherTag = (NbtIntArrayTag) other;
        return this.dataSize == otherTag.dataSize && Arrays.equals(this.data, otherTag.data) && this.name.equals(otherTag.name);
    }
    
}
