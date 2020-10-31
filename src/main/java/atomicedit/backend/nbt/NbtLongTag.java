
package atomicedit.backend.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Justin Bonner
 */
public class NbtLongTag extends NbtTag{
    
    private long data;
    
    public NbtLongTag(DataInputStream input, boolean readName) throws IOException{
        super(NbtTypes.TAG_LONG, readName ? NbtTag.readUtfString(input) : "");
        this.data = input.readLong();
    }
    
    public NbtLongTag(String name, long data){
        super(NbtTypes.TAG_LONG, name);
        this.data = data;
    }
    
    @Override
    protected void write(DataOutputStream output) throws IOException{
        output.writeLong(data);
    }
    
    public long getPayload(){
        return this.data;
    }
    
    @Override
    public NbtLongTag copy() {
        return new NbtLongTag(name, data);
    }
    
    @Override
    public String toString(int indent){
        return String.format("%"+indent+"s", "") + this.getName() + ":" + data;
    }
    
}
