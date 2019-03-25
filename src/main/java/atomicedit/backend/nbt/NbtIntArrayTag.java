
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
    
    private int[] data;
    private int dataSize;
    
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
    public String toString(int indent){
        return String.format("%"+indent+"s", "") + this.getName() + ":" + Arrays.toString(data);
    }
    
}
