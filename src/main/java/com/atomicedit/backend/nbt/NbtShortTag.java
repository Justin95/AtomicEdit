
package com.atomicedit.backend.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Justin Bonner
 */
public class NbtShortTag extends NbtTag{
    
    private short data;
    
    public NbtShortTag(DataInputStream input) throws IOException{
        super(NbtTypes.TAG_SHORT, NbtTag.readUtfString(input));
        this.data = input.readShort();
    }
    
    public NbtShortTag(String name, short data){
        super(NbtTypes.TAG_SHORT, name);
        this.data = data;
    }
    
    @Override
    public void write(DataOutputStream output) throws IOException{
        output.writeShort(data);
    }
    
    public short getPayload(){
        return this.data;
    }
    
    
}
