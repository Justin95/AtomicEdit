
package atomicedit.backend.nbt;

import atomicedit.logging.Logger;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public enum NbtTypes {
    /*
     * Nbt Types must have the index of their Tag Id, https://minecraft.gamepedia.com/NBT_format
     */
    TAG_END(
        (DataInputStream input, boolean readName) -> {throw new UnsupportedOperationException();} //never instantiate end tags
    ),
    TAG_BYTE(
        (DataInputStream input, boolean readName) -> new NbtByteTag(input, readName)
    ),
    TAG_SHORT(
        (DataInputStream input, boolean readName) -> new NbtShortTag(input, readName)
    ),
    TAG_INT(
        (DataInputStream input, boolean readName) -> new NbtIntTag(input, readName)
    ),
    TAG_LONG(
        (DataInputStream input, boolean readName) -> new NbtLongTag(input, readName)
    ),
    TAG_FLOAT(
        (DataInputStream input, boolean readName) -> new NbtFloatTag(input, readName)
    ),
    TAG_DOUBLE(
        (DataInputStream input, boolean readName) -> new NbtDoubleTag(input, readName)
    ),
    TAG_BYTE_ARRAY(
        (DataInputStream input, boolean readName) -> new NbtByteArrayTag(input, readName)
    ),
    TAG_STRING(
        (DataInputStream input, boolean readName) -> new NbtStringTag(input, readName)
    ),
    TAG_LIST(
        (DataInputStream input, boolean readName) -> new NbtListTag(input, readName)
    ),
    TAG_COMPOUND(
        (DataInputStream input, boolean readName) -> new NbtCompoundTag(input, readName)
    ),
    TAG_INT_ARRAY(
        (DataInputStream input, boolean readName) -> new NbtIntArrayTag(input, readName)
    ),
    TAG_LONG_ARRAY(
        (DataInputStream input, boolean readName) -> new NbtLongArrayTag(input, readName)
    )
    ;
    
    private Instantiater instantiater;
    
    NbtTypes(Instantiater instantiater){
        this.instantiater = instantiater;
    }
    
    public static NbtTypes getTypeFromId(int id){
        if(id < 0 || id > NbtTypes.values().length){
            Logger.warning("Invalid NbtTag id");
            return null;
        }
        return NbtTypes.values()[id];
    }
    
    public NbtTag instantiate(DataInputStream inputStream, boolean readName){
        try{
            return instantiater.instantiate(inputStream, readName);
        }catch(IOException e){
            Logger.error("IOException making nbt tag: " + e);
            return null;
        }
    }
    
    public static NbtTag typeCheck(NbtTag tag, NbtTypes type) throws MalformedNbtTagException{
        if(tag == null){
            Logger.warning("Tried to nbt type check null");
            throw new NullPointerException();
        }
        if(tag.getType() == type){
            return tag;
        }else{
            Logger.error("Nbt tag failed type check: " + tag.getName());
            throw new MalformedNbtTagException();
        }
    }
    
    public interface Instantiater{
        public NbtTag instantiate(DataInputStream inputStream, boolean readName) throws IOException;
    }
    
    public static NbtCompoundTag getAsCompoundTag(NbtTag tag) throws MalformedNbtTagException{
        if(tag.getType() == TAG_COMPOUND){
            return (NbtCompoundTag) tag;
        }
        Logger.notice("Tried to cast tag: " + tag.getName() + " as Compound Tag but it was a " + tag.getType().name());
        throw new MalformedNbtTagException();
    }
    
    public static NbtListTag getAsListTag(NbtTag tag) throws MalformedNbtTagException{
        if(tag.getType() == TAG_LIST){
            return (NbtListTag) tag;
        }
        Logger.notice("Tried to cast tag: " + tag.getName() + " as List Tag but it was a " + tag.getType().name());
        throw new MalformedNbtTagException();
    }
    
    //quick payload getters
    
    public static byte getByte(NbtTag tag) throws MalformedNbtTagException{
        typeCheck(tag, TAG_BYTE);
        return ((NbtByteTag) tag).getPayload();
    }
    
    public static short getShort(NbtTag tag) throws MalformedNbtTagException{
        typeCheck(tag, TAG_SHORT);
        return ((NbtShortTag) tag).getPayload();
    }
    
    public static int getInt(NbtTag tag) throws MalformedNbtTagException{
        typeCheck(tag, TAG_INT);
        return ((NbtIntTag) tag).getPayload();
    }
    
    public static long getLong(NbtTag tag) throws MalformedNbtTagException{
        typeCheck(tag, TAG_LONG);
        return ((NbtLongTag) tag).getPayload();
    }
    
    public static float getFloat(NbtTag tag) throws MalformedNbtTagException{
        typeCheck(tag, TAG_FLOAT);
        return ((NbtFloatTag) tag).getPayload();
    }
    
    public static double getDouble(NbtTag tag) throws MalformedNbtTagException{
        typeCheck(tag, TAG_DOUBLE);
        return ((NbtDoubleTag) tag).getPayload();
    }
    
    public static String getString(NbtTag tag) throws MalformedNbtTagException{
        typeCheck(tag, TAG_STRING);
        return ((NbtStringTag) tag).getPayload();
    }
    
    public static byte[] getByteArray(NbtTag tag) throws MalformedNbtTagException{
        typeCheck(tag, TAG_BYTE_ARRAY);
        return ((NbtByteArrayTag) tag).getPayload();
    }
    
    public static int[] getIntArray(NbtTag tag) throws MalformedNbtTagException{
        typeCheck(tag, TAG_INT_ARRAY);
        return ((NbtIntArrayTag) tag).getPayload();
    }
    
    public static long[] getLongArray(NbtTag tag) throws MalformedNbtTagException{
        typeCheck(tag, TAG_LONG_ARRAY);
        return ((NbtLongArrayTag) tag).getPayload();
    }
    
    public static List<NbtTag> getNbtList(NbtTag tag) throws MalformedNbtTagException{
        typeCheck(tag, TAG_LIST);
        return ((NbtListTag) tag).getPayload();
    }
    
    public static NbtTag getNbtTag(NbtTag tag, String name) throws MalformedNbtTagException{
        typeCheck(tag, TAG_COMPOUND);
        return ((NbtCompoundTag) tag).getTag(name);
    }
    
}
