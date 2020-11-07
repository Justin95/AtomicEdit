
package atomicedit.backend.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Justin Bonner
 */
public class NbtStringTag extends NbtTag{
    
    private String data;
    private int dataSize;
    
    public NbtStringTag(DataInputStream input, boolean readName) throws IOException{
        super(NbtTypes.TAG_STRING, readName ? NbtTag.readUtfString(input) : "");
        this.data = NbtTag.readUtfString(input);
        this.dataSize = data.length();
    }
    
    public NbtStringTag(String name, String data){
        super(NbtTypes.TAG_STRING, name);
        this.data = data;
        this.dataSize = data.length();
    }
    
    @Override
    protected void write(DataOutputStream output) throws IOException{
        output.writeUTF(data);
    }
    
    public String getPayload(){
        return this.data;
    }
    
    public void setPayload(String value) {
        this.data = value;
        this.dataSize = value.length();
    }
    
    public int getPayloadSize(){
        return this.dataSize;
    }
    
    @Override
    public NbtStringTag copy() {
        return new NbtStringTag(name, data);
    }
    
    @Override
    public String toString(int indent){
        return String.format("%"+indent+"s", "") + this.getName() + ":\"" + data + "\"";
    }
    
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NbtStringTag)) {
            return false;
        }
        if (this == other) {
            return true;
        }
        NbtStringTag otherTag = (NbtStringTag) other;
        return this.data.equals(otherTag.data) && this.name.equals(otherTag.name);
    }
    
}
