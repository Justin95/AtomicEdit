
package atomicedit.backend.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Justin Bonner
 */
public class NbtIntTag extends NbtTag{
    
    private int data;
    
    public NbtIntTag(DataInputStream input, boolean readName) throws IOException{
        super(NbtTypes.TAG_INT, readName ? NbtTag.readUtfString(input) : "");
        this.data = input.readInt();
    }
    
    public NbtIntTag(String name, int data){
        super(NbtTypes.TAG_INT, name);
        this.data = data;
    }
    
    @Override
    protected void write(DataOutputStream output) throws IOException{
        output.writeInt(data);
    }
    
    public int getPayload(){
        return this.data;
    }
    
    @Override
    public NbtIntTag copy() {
        return new NbtIntTag(name, data);
    }
    
    @Override
    public String toString(int indent){
        return String.format("%"+indent+"s", "") + this.getName() + ":" + data;
    }
    
}
