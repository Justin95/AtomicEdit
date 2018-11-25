
package atomicedit.backend.nbt;

import atomicedit.logging.Logger;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Justin Bonner
 */
public class NbtIntArrayTag extends NbtTag{
    
    private int[] data;
    private int dataSize;
    
    public NbtIntArrayTag(DataInputStream input) throws IOException{
        super(NbtTypes.TAG_INT_ARRAY, NbtTag.readUtfString(input));
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
    public void write(DataOutputStream output) throws IOException{
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
    
    
}
