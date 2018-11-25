
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
    
    public NbtDoubleTag(DataInputStream input) throws IOException{
        super(NbtTypes.TAG_DOUBLE, NbtTag.readUtfString(input));
        this.data = input.readDouble();
    }
    
    public NbtDoubleTag(String name, double data){
        super(NbtTypes.TAG_DOUBLE, name);
        this.data = data;
    }
    
    @Override
    public void write(DataOutputStream output) throws IOException{
        output.writeDouble(data);
    }
    
    public double getPayload(){
        return this.data;
    }
    
}
