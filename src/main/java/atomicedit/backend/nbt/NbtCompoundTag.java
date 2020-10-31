
package atomicedit.backend.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Justin Bonner
 */
public class NbtCompoundTag extends NbtTag {
    
    private final ArrayList<NbtTag> data;
    private int dataSize;
    
    public NbtCompoundTag(DataInputStream input, boolean readName) throws IOException{
        super(NbtTypes.TAG_COMPOUND, readName ? NbtTag.readUtfString(input) : "");
        this.data = new ArrayList<>();
        dataSize = 0;
        byte tagId;
        while((tagId = input.readByte()) != 0){
            data.add(NbtTypes.getTypeFromId(tagId).instantiate(input, true));
            dataSize++;
        }
    }
    
    public NbtCompoundTag(String name, ArrayList<NbtTag> data){
        super(NbtTypes.TAG_COMPOUND, name);
        this.data = data;
        this.dataSize = data.size();
    }
    
    public NbtCompoundTag(String name, NbtTag... nbtTags){
        super(NbtTypes.TAG_COMPOUND, name);
        ArrayList<NbtTag> tagData = new ArrayList<>();
        for(NbtTag nbtTag : nbtTags){
            tagData.add(nbtTag);
        }
        this.data = tagData;
        this.dataSize = tagData.size();
    }
    
    @Override
    protected void write(DataOutputStream output) throws IOException{
        for(NbtTag tag : data){
            output.writeByte(tag.getType().ordinal());
            output.writeUTF(tag.getName());
            tag.write(output);
        }
        output.writeByte(0); //write null terminator / TAG_END
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
    
    /**
     * Add a tag to this compound tag. If this compound tag already contains
     * a tag with the same name as the tagToAdd then replace that tag.
     * @param tagToAdd 
     */
    public void putTag(NbtTag tagToAdd){
        NbtTag matchedTag = null;
        for(NbtTag tag : data){
            if(tag.getName().equals(tagToAdd.getName())){
                matchedTag = tag;
                break;
            }
        }
        if(matchedTag != null){
            data.remove(matchedTag);
            this.dataSize--;
        }
        data.add(tagToAdd);
        this.dataSize++;
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
    
    @Override
    public NbtCompoundTag copy() {
        ArrayList<NbtTag> dataCopy = new ArrayList<>(this.dataSize);
        for (NbtTag tag : data) {
            dataCopy.add(tag.copy());
        }
        return new NbtCompoundTag(name, dataCopy);
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
    
    @Override
    public String toString(int indent){
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(String.format("%"+indent+"s", ""));
        strBuilder.append(this.getName());
        strBuilder.append(":{");
        if(!data.isEmpty()){
            strBuilder.append("\n");
        }
        data.forEach((NbtTag tag) -> {
            strBuilder.append(tag.toString(indent + 4));
            strBuilder.append("\n");
        });
        if(!data.isEmpty()){
            strBuilder.append(String.format("%"+indent+"s", ""));
        }
        strBuilder.append("}");
        return strBuilder.toString();
    }
    
}
