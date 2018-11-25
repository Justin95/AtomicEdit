
package atomicedit.backend.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Justin Bonner
 */
public class NbtCompoundTag extends NbtTag{
    
    private ArrayList<NbtTag> data;
    private int dataSize;
    
    public NbtCompoundTag(DataInputStream input) throws IOException{
        super(NbtTypes.TAG_COMPOUND, NbtTag.readUtfString(input));
        this.data = new ArrayList<>();
        dataSize = 0;
        byte tagId;
        while((tagId = input.readByte()) != 0){
            data.add(NbtTypes.getTypeFromId(tagId).instantiate(input));
            dataSize++;
        }
    }
    
    public NbtCompoundTag(String name, ArrayList<NbtTag> data){
        super(NbtTypes.TAG_COMPOUND, name);
        this.data = data;
        this.dataSize = data.size();
    }
    
    @Override
    public void write(DataOutputStream output) throws IOException{
        for(NbtTag tag : data){
            output.write(tag.getType().ordinal());
            output.writeUTF(tag.getName());
            tag.write(output);
        }
        output.write(0); //write null terminator / TAG_END
    }
    
    public ArrayList<NbtTag> getPayload(){
        return this.data;
    }
    
    public NbtTag getTag(String name) throws MalformedNbtTagException{
        for(NbtTag tag : data){
            if(tag.getName().equals(name)){
                return tag;
            }
        }
        throw new MalformedNbtTagException("Compound tag does not contain: " + name);
    }
    
    public boolean contains(String name){
        for(NbtTag tag : data){
            if(tag.getName().equals(name)){
                return true;
            }
        }
        return false;
    }
    
    public int getPayloadSize(){
        return this.dataSize;
    }
    
    //time savers
    
    public NbtByteArrayTag getByteArrayTag(String name) throws MalformedNbtTagException{
        NbtTag tag = getTag(name);
        NbtTypes.typeCheck(tag, NbtTypes.TAG_BYTE_ARRAY);
        return (NbtByteArrayTag) tag;
    }
    
    public NbtByteTag getByteTag(String name) throws MalformedNbtTagException{
        NbtTag tag = getTag(name);
        NbtTypes.typeCheck(tag, NbtTypes.TAG_BYTE);
        return (NbtByteTag) tag;
    }
    
    public NbtCompoundTag getCompoundTag(String name) throws MalformedNbtTagException{
        NbtTag tag = getTag(name);
        NbtTypes.typeCheck(tag, NbtTypes.TAG_COMPOUND);
        return (NbtCompoundTag) tag;
    }
    
    public NbtDoubleTag getDoubleTag(String name) throws MalformedNbtTagException{
        NbtTag tag = getTag(name);
        NbtTypes.typeCheck(tag, NbtTypes.TAG_DOUBLE);
        return (NbtDoubleTag) tag;
    }
    
    public NbtFloatTag getFloatTag(String name) throws MalformedNbtTagException{
        NbtTag tag = getTag(name);
        NbtTypes.typeCheck(tag, NbtTypes.TAG_FLOAT);
        return (NbtFloatTag) tag;
    }
    
    public NbtIntArrayTag getIntArrayTag(String name) throws MalformedNbtTagException{
        NbtTag tag = getTag(name);
        NbtTypes.typeCheck(tag, NbtTypes.TAG_INT_ARRAY);
        return (NbtIntArrayTag) tag;
    }
    
    public NbtIntTag getIntTag(String name) throws MalformedNbtTagException{
        NbtTag tag = getTag(name);
        NbtTypes.typeCheck(tag, NbtTypes.TAG_INT);
        return (NbtIntTag) tag;
    }
    
    public NbtListTag getListTag(String name) throws MalformedNbtTagException{
        NbtTag tag = getTag(name);
        NbtTypes.typeCheck(tag, NbtTypes.TAG_LIST);
        return (NbtListTag) tag;
    }
    
    public NbtLongTag getLongTag(String name) throws MalformedNbtTagException{
        NbtTag tag = getTag(name);
        NbtTypes.typeCheck(tag, NbtTypes.TAG_LONG);
        return (NbtLongTag) tag;
    }
    
    public NbtLongArrayTag getLongArrayTag(String name) throws MalformedNbtTagException{
        NbtTag tag = getTag(name);
        NbtTypes.typeCheck(tag, NbtTypes.TAG_LONG_ARRAY);
        return (NbtLongArrayTag) tag;
    }
    
    public NbtShortTag getShortTag(String name) throws MalformedNbtTagException{
        NbtTag tag = getTag(name);
        NbtTypes.typeCheck(tag, NbtTypes.TAG_SHORT);
        return (NbtShortTag) tag;
    }
    
    public NbtStringTag getStringTag(String name) throws MalformedNbtTagException{
        NbtTag tag = getTag(name);
        NbtTypes.typeCheck(tag, NbtTypes.TAG_STRING);
        return (NbtStringTag) tag;
    }
    
}
