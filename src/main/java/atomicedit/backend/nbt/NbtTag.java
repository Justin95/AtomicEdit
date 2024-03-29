
package atomicedit.backend.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Justin Bonner
 */
public abstract class NbtTag {
    //https://minecraft.gamepedia.com/NBT_format
    private final NbtTypes type;
    protected final String name;
    
    public NbtTag(NbtTypes type, String name){
        this.type = type;
        this.name = name;
    }
    
    public NbtTypes getType(){
        return this.type;
    }
    
    public String getName(){
        return this.name;
    }
    
    protected abstract void write(DataOutputStream writer) throws IOException;
    
    protected static String readUtfString(DataInputStream input) throws IOException{
        return input.readUTF();
    }
    
    public static NbtTag readNbt(DataInputStream input) throws IOException, MalformedNbtTagException{
        byte tagId = input.readByte();
        return NbtTypes.getTypeFromId(tagId).instantiate(input, true);
    }
    
    public static void writeTag(DataOutputStream writer, NbtTag tag) throws IOException {
        //Logger.info("Writing nbt tag:\n" + tag.toString()); //this is a great log message for debugging
        writer.writeByte(tag.getType().ordinal());
        writer.writeUTF(tag.name);
        tag.write(writer);
    }
    
    @Override
    public String toString(){
        return toString(2);
    }
    
    protected abstract String toString(int indent);
    
    public abstract NbtTag copy();
    
}
