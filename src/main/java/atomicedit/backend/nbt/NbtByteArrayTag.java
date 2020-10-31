
package atomicedit.backend.nbt;

import atomicedit.logging.Logger;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author Justin Bonner
 */
public class NbtByteArrayTag extends NbtTag{
    
    private final byte[] data;
    private final int dataSize;
    
    public NbtByteArrayTag(DataInputStream input, boolean readName) throws IOException{
        super(NbtTypes.TAG_BYTE_ARRAY, readName ? NbtTag.readUtfString(input) : "");
        this.dataSize = input.readInt();
        this.data = new byte[dataSize];
        int result = input.read(data);
        if(result != dataSize){
            Logger.warning("Could not read full length of NBT byte array");
            throw new IOException();
        }
    }
    
    public NbtByteArrayTag(String name, byte[] data){
        super(NbtTypes.TAG_BYTE_ARRAY, name);
        this.data = data;
        this.dataSize = data.length;
    }
    
    @Override
    protected void write(DataOutputStream output) throws IOException{
        output.writeInt(dataSize);
        output.write(data);
    }
    
    public byte[] getPayload(){
        return this.data;
    }
    
    public int getPayloadSize(){
        return this.dataSize;
    }
    
    @Override
    public NbtByteArrayTag copy() {
        return new NbtByteArrayTag(name, data);
    }
    
    @Override
    public String toString(int indent){
        return String.format("%"+indent+"s", "") + this.getName() + ":" + Arrays.toString(data);
    }
    
}
