
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
        this.data = new ArrayList<>(dataLength);
        for(int i = 0; i < dataLength; i++){
            data.add(i, NbtTypes.getTypeFromId(tagId).instantiate(input, false));
        }
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
    public void write(DataOutputStream output) throws IOException{
        output.write(tagId);
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
        if(getListType() == NbtTypes.TAG_BYTE_ARRAY){
            return (List<NbtByteArrayTag>)(List<?>) data; //we can use a (List<?>) cast because we expressly check types here and in the constructor
        }
        throw new MalformedNbtTagException();
    }
    
    public List<NbtByteTag> getByteTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_BYTE){
            return (List<NbtByteTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException();
    }
    
    public List<NbtCompoundTag> getCompoundTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_COMPOUND){
            return (List<NbtCompoundTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException();
    }
    
    public List<NbtDoubleTag> getDoubleTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_DOUBLE){
            return (List<NbtDoubleTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException();
    }
    
    public List<NbtFloatTag> getFloatTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_FLOAT){
            return (List<NbtFloatTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException();
    }
    
    public List<NbtIntArrayTag> getIntArrayTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_INT_ARRAY){
            return (List<NbtIntArrayTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException();
    }
    
    public List<NbtIntTag> getIntTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_INT){
            return (List<NbtIntTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException();
    }
    
    public List<NbtListTag> getListTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_LIST){
            return (List<NbtListTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException();
    }
    
    public List<NbtLongTag> getLongTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_LONG){
            return (List<NbtLongTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException();
    }
    
    public List<NbtLongArrayTag> getLongArrayTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_LONG_ARRAY){
            return (List<NbtLongArrayTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException();
    }
    
    public List<NbtShortTag> getShortTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_SHORT){
            return (List<NbtShortTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException();
    }
    
    public List<NbtStringTag> getStringTags() throws MalformedNbtTagException{
        if(getListType() == NbtTypes.TAG_STRING){
            return (List<NbtStringTag>)(List<?>) data;
        }
        throw new MalformedNbtTagException();
    }
    
    @Override
    public String toString(){
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(this.getName());
        strBuilder.append(":[\n");
        data.forEach((NbtTag tag) -> {
            strBuilder.append(tag.toString());
            strBuilder.append("\n");
        });
        strBuilder.append("]");
        return strBuilder.toString();
    }
    
}
