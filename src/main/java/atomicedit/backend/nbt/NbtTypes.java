
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
        (DataInputStream input) -> {throw new UnsupportedOperationException();} //never instantiate end tags
    ),
    TAG_BYTE(
        (DataInputStream input) -> new NbtByteTag(input)
    ),
    TAG_SHORT(
        (DataInputStream input) -> new NbtShortTag(input)
    ),
    TAG_INT(
        (DataInputStream input) -> new NbtIntTag(input)
    ),
    TAG_LONG(
        (DataInputStream input) -> new NbtLongTag(input)
    ),
    TAG_FLOAT(
        (DataInputStream input) -> new NbtFloatTag(input)
    ),
    TAG_DOUBLE(
        (DataInputStream input) -> new NbtDoubleTag(input)
    ),
    TAG_BYTE_ARRAY(
        (DataInputStream input) -> new NbtByteArrayTag(input)
    ),
    TAG_STRING(
        (DataInputStream input) -> new NbtStringTag(input)
    ),
    TAG_LIST(
        (DataInputStream input) -> new NbtListTag(input)
    ),
    TAG_COMPOUND(
        (DataInputStream input) -> new NbtCompoundTag(input)
    ),
    TAG_INT_ARRAY(
        (DataInputStream input) -> new NbtIntArrayTag(input)
    ),
    TAG_LONG_ARRAY(
        (DataInputStream input) -> new NbtLongArrayTag(input)
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
    
    public NbtTag instantiate(DataInputStream inputStream){
        try{
            return instantiater.instantiate(inputStream);
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
        public NbtTag instantiate(DataInputStream inputStream) throws IOException;
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
