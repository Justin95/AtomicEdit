
package atomicedit.backend.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Justin Bonner
 */
public class NbtDoubleTag extends NbtTag{
    
    private double data;
    
    public NbtDoubleTag(DataInputStream input, boolean readName) throws IOException{
        super(NbtTypes.TAG_DOUBLE, readName ? NbtTag.readUtfString(input) : "");
        this.data = input.readDouble();
    }
    
    public NbtDoubleTag(String name, double data){
        super(NbtTypes.TAG_DOUBLE, name);
        this.data = data;
    }
    
    @Override
    protected void write(DataOutputStream output) throws IOException{
        output.writeDouble(data);
    }
    
    public double getPayload(){
        return this.data;
    }
    
    public void setPayload(double value) {
        this.data = value;
    }
    
    @Override
    public NbtDoubleTag copy() {
        return new NbtDoubleTag(name, data);
    }
    
    @Override
    public String toString(int indent){
        return String.format("%"+indent+"s", "") + this.getName() + ":" + data;
    }
    
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NbtDoubleTag)) {
            return false;
        }
        if (this == other) {
            return true;
        }
        NbtDoubleTag otherTag = (NbtDoubleTag) other;
        return this.data == otherTag.data && this.name.equals(otherTag.name);
    }
    
}
