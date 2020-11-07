
package atomicedit.backend.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Justin Bonner
 */
public class NbtShortTag extends NbtTag{
    
    private short data;
    
    public NbtShortTag(DataInputStream input, boolean readName) throws IOException{
        super(NbtTypes.TAG_SHORT, readName ? NbtTag.readUtfString(input) : "");
        this.data = input.readShort();
    }
    
    public NbtShortTag(String name, short data){
        super(NbtTypes.TAG_SHORT, name);
        this.data = data;
    }
    
    @Override
    protected void write(DataOutputStream output) throws IOException{
        output.writeShort(data);
    }
    
    public short getPayload(){
        return this.data;
    }
    
    public void setPayload(short value) {
        this.data = value;
    }
    
    @Override
    public NbtShortTag copy() {
        return new NbtShortTag(name, data);
    }
    
    @Override
    public String toString(int indent){
        return String.format("%"+indent+"s", "") + this.getName() + ":" + data;
    }
    
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NbtShortTag)) {
            return false;
        }
        if (this == other) {
            return true;
        }
        NbtShortTag otherTag = (NbtShortTag) other;
        return this.data == otherTag.data && this.name.equals(otherTag.name);
    }
    
}
