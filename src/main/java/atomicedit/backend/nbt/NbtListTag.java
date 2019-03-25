
package atomicedit.backend.nbt;

import atomicedit.logging.Logger;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public class NbtListTag extends NbtTag{
    
    private List<NbtTag> data;
    private byte tagId;
    
    public NbtListTag(DataInputStream input, boolean readName) throws IOException{
        super(NbtTypes.TAG_LIST, readName ? NbtTag.readUtfString(input) : "");
        this.tagId = input.readByte();
        int dataLength = input.readInt();
        List<NbtTag> temp = new ArrayList<>(dataLength);
        for(int i = 0; i < dataLength; i++){
            temp.add(i, NbtTypes.getTypeFromId(tagId).instantiate(input, false));
        }
        this.data = temp; //Collections.unmodifiableList(temp); //probably dont make this immutable
    }
    
    public NbtListTag(String name, List<NbtTag> data){
        super(NbtTypes.TAG_LIST, name);
        if(data.isEmpty()){
            this.data = data;
            this.tagId = 1; //TODO figure out of this should be 0 or if that gets read as a TAG_END
        }else{
            NbtTypes type = data.get(0).getType();
            for(NbtTag tag : data){ //need to be certain all the nbt tags are the same type
                if(tag.getType() != type){ //enums can be compared with ==
                    Logger.error("Trying to create malformed Nbt List Tag");
                    throw new IllegalArgumentException("Nbt tags in Nbt List Tag must all be of the same type");
                }
            }
            this.data = data;
            this.tagId = (byte)type.ordinal();
        }
    }
    
    @Override
    protected void write(DataOutputStream output) throws IOException{
        output.writeByte(tagId);
        output.writeInt(data.size());
        for(NbtTag tag : data){
            tag.write(output);
        }
    }
    
    public List<NbtTag> getPayload(){
        return this.data;
    }
    
    public NbtTypes getListType(){
        return NbtTypes.getTypeFromId(tagId);
    }
    
    public int getPayloadSize(){
        return this.data.size();
    }
    
    //time savers
    
    public List<NbtByteArrayTag> getByteArrayTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_BYTE_ARRAY || (getListType() == NbtTypes.TAG_END && data.isEmpty())){
            return (List<NbtByteArrayTag>)(List<?>) data; //we can use a (List<?>) cast because we expressly check types here and in the constructor
        }
        throw new MalformedNbtTagException("Expected a list of Byte Array NBT tags got: ", this);
    }
    
    public List<NbtByteTag> getByteTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_BYTE || (getListType() == NbtTypes.TAG_END && data.isEmpty())){
            return (List<NbtByteTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException("Expected a list of Byte NBT tags got: ", this);
    }
    
    public List<NbtCompoundTag> getCompoundTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_COMPOUND || (getListType() == NbtTypes.TAG_END && data.isEmpty())){
            return (List<NbtCompoundTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException("Expected a list of Compound NBT tags got type "+getListType()+": ", this);
    }
    
    public List<NbtDoubleTag> getDoubleTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_DOUBLE || (getListType() == NbtTypes.TAG_END && data.isEmpty())){
            return (List<NbtDoubleTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException("Expected a list of Double NBT tags got: ", this);
    }
    
    public List<NbtFloatTag> getFloatTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_FLOAT || (getListType() == NbtTypes.TAG_END && data.isEmpty())){
            return (List<NbtFloatTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException("Expected a list of Float NBT tags got: ", this);
    }
    
    public List<NbtIntArrayTag> getIntArrayTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_INT_ARRAY || (getListType() == NbtTypes.TAG_END && data.isEmpty())){
            return (List<NbtIntArrayTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException("Expected a list of Integer Array NBT tags got: ", this);
    }
    
    public List<NbtIntTag> getIntTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_INT || (getListType() == NbtTypes.TAG_END && data.isEmpty())){
            return (List<NbtIntTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException("Expected a list of Integer NBT tags got: ", this);
    }
    
    public List<NbtListTag> getListTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_LIST || (getListType() == NbtTypes.TAG_END && data.isEmpty())){
            return (List<NbtListTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException("Expected a list of List NBT tags got: ", this);
    }
    
    public List<NbtLongTag> getLongTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_LONG || (getListType() == NbtTypes.TAG_END && data.isEmpty())){
            return (List<NbtLongTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException("Expected a list of Long NBT tags got: ", this);
    }
    
    public List<NbtLongArrayTag> getLongArrayTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_LONG_ARRAY || (getListType() == NbtTypes.TAG_END && data.isEmpty())){
            return (List<NbtLongArrayTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException("Expected a list of Long Array NBT tags got: ", this);
    }
    
    public List<NbtShortTag> getShortTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_SHORT || (getListType() == NbtTypes.TAG_END && data.isEmpty())){
            return (List<NbtShortTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException("Expected a list of Short NBT tags got: ", this);
    }
    
    public List<NbtStringTag> getStringTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_STRING || (getListType() == NbtTypes.TAG_END && data.isEmpty())){
            return (List<NbtStringTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException("Expected a list of String NBT tags got: ", this);
    }
    
    @Override
    public String toString(int indent){
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(String.format("%"+indent+"s", ""));
        strBuilder.append(this.getName());
        strBuilder.append(":[");
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
        strBuilder.append("]");
        return strBuilder.toString();
    }
    
}
