
package atomicedit.backend.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Justin Bonner
 */
public class NbtByteTag extends NbtTag{
    
    private byte data;
    
    public NbtByteTag(DataInputStream input, boolean readName) throws IOException{
        super(NbtTypes.TAG_BYTE, readName ? NbtTag.readUtfString(input) : "");
        this.data = input.readByte();
    }
    
    public NbtByteTag(String name, byte data){
        super(NbtTypes.TAG_BYTE, name);
        this.data = data;
    }
    
    @Override
    protected void write(DataOutputStream output) throws IOException{
        output.writeByte(data);
    }
    
    public byte getPayload(){
        return this.data;
    }
    
    @Override
    public String toString(int indent){
        return String.format("%"+indent+"s", "") + this.getName() + ":" + data;
    }
    
}
