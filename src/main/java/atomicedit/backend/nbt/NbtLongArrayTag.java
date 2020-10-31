
package atomicedit.backend.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author Justin Bonner
 */
public class NbtLongArrayTag extends NbtTag{
    
    private final long[] data;
    private final int dataSize;
    
    public NbtLongArrayTag(DataInputStream input, boolean readName) throws IOException{
        super(NbtTypes.TAG_LONG_ARRAY, readName ? NbtTag.readUtfString(input) : "");
        this.dataSize = input.readInt();
        this.data = new long[dataSize];
        for(int i = 0; i < dataSize; i++){
            data[i] = input.readLong();
        }
    }
    
    public NbtLongArrayTag(String name, long[] data){
        super(NbtTypes.TAG_LONG_ARRAY, name);
        this.data = data;
        this.dataSize = data.length;
    }
    
    @Override
    protected void write(DataOutputStream output) throws IOException{
        output.writeInt(dataSize);
        for(int i = 0; i < dataSize; i++){
            output.writeLong(data[i]);
        }
    }
    
    public long[] getPayload(){
        return this.data;
    }
    
    public int getPayloadSize(){
        return this.dataSize;
    }
    
    @Override
    public NbtLongArrayTag copy() {
        return new NbtLongArrayTag(name, data);
    }
    
    @Override
    public String toString(int indent){
        return String.format("%"+indent+"s", "") + this.getName() + ":" + Arrays.toString(data);
    }
    
}
