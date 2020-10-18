
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
    private NbtTypes type;
    private String name;
    
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
    
    public void setName(String name){
        this.name = name;
    }
    
    protected abstract void write(DataOutputStream writer) throws IOException;
    
    protected static String readUtfString(DataInputStream input) throws IOException{
        return input.readUTF();
    }
    
    public static void writeTag(DataOutputStream writer, NbtTag tag) throws IOException{
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
    
}
