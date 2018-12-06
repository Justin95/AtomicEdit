
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
    public void write(DataOutputStream output) throws IOException{
        output.writeUTF(data);
    }
    
    public String getPayload(){
        return this.data;
    }
    
    public int getPayloadSize(){
        return this.dataSize;
    }
    
    @Override
    public String toString(){
        return this.getName() + ":\"" + data + "\"";
    }
    
}
