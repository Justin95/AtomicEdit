
package com.atomicedit.backend.nbt;

import com.atomicedit.logging.Logger;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 *
 * @author Justin Bonner
 */
public class NbtInterpreter {
    
    
    
    
    public static NbtTag interpretNbt(byte[] nbtData){
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(nbtData));
        try{
            NbtTag nbt = readNbt(input);
            Logger.info("Sucessfully read NBT tag");
            return nbt;
        }catch(IOException e){
            Logger.error("Encountered IOException while reading NBT data");
        }catch(MalformedNbtTagException e){
            Logger.error("Tried to read malformed NBT data");
        }catch(Exception e){
            Logger.error("Unexpected exception while reading NBT data: " + e);
        }
        return null;
    }
    
    static NbtTag readNbt(DataInputStream input) throws IOException, MalformedNbtTagException{
        byte tagId = input.readByte();
        return NbtTypes.getTypeFromId(tagId).instantiate(input);
    }
    
}
