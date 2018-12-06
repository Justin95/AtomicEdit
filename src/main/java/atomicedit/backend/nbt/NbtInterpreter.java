
package atomicedit.backend.nbt;

import atomicedit.logging.Logger;
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
            return nbt;
        }catch(IOException e){
            Logger.error("Encountered IOException while reading NBT data", e);
        }catch(MalformedNbtTagException e){
            Logger.error("Tried to read malformed NBT data", e);
        }catch(Exception e){
            Logger.error("Unexpected exception while reading NBT data", e);
        }
        return null;
    }
    
    static NbtTag readNbt(DataInputStream input) throws IOException, MalformedNbtTagException{
        byte tagId = input.readByte();
        return NbtTypes.getTypeFromId(tagId).instantiate(input, true);
    }
    
}
