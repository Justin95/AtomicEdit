
package atomicedit.backend.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Justin Bonner
 */
public class NbtFloatTag extends NbtTag{
    
    private final float data;
    
    public NbtFloatTag(DataInputStream input, boolean readName) throws IOException{
        super(NbtTypes.TAG_FLOAT, readName ? NbtTag.readUtfString(input) : "");
        this.data = input.readFloat();
    }
    
    public NbtFloatTag(String name, float data){
        super(NbtTypes.TAG_FLOAT, name);
        this.data = data;
    }
    
    @Override
    protected void write(DataOutputStream output) throws IOException{
        output.writeFloat(data);
    }
    
    public float getPayload(){
        return this.data;
    }
    
    @Override
    public NbtFloatTag copy() {
        return new NbtFloatTag(name, data);
    }
    
    @Override
    public String toString(int indent){
        return String.format("%"+indent+"s", "") + this.getName() + ":" + data;
    }
    
}
