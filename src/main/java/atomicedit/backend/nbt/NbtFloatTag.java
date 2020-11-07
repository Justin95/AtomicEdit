
package atomicedit.backend.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Justin Bonner
 */
public class NbtFloatTag extends NbtTag{
    
    private float data;
    
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
    
    public void setPayload(float value) {
        this.data = value;
    }
    
    @Override
    public NbtFloatTag copy() {
        return new NbtFloatTag(name, data);
    }
    
    @Override
    public String toString(int indent){
        return String.format("%"+indent+"s", "") + this.getName() + ":" + data;
    }
    
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NbtFloatTag)) {
            return false;
        }
        if (this == other) {
            return true;
        }
        NbtFloatTag otherTag = (NbtFloatTag) other;
        return this.data == otherTag.data && this.name.equals(otherTag.name);
    }
    
}
