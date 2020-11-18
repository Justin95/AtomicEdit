
package atomicedit.backend.nbt;

import atomicedit.logging.Logger;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
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
        "End Tag",
        (DataInputStream input, boolean readName) -> {throw new UnsupportedOperationException();}, //never instantiate end tags
        (String name) -> {throw new UnsupportedOperationException();} //never instantiate end tags
    ),
    TAG_BYTE(
        "Byte Tag",
        (DataInputStream input, boolean readName) -> new NbtByteTag(input, readName),
        (String name) -> new NbtByteTag(name, (byte)0)
    ),
    TAG_SHORT(
        "Short Tag",
        (DataInputStream input, boolean readName) -> new NbtShortTag(input, readName),
        (String name) -> new NbtShortTag(name, (short)0)
    ),
    TAG_INT(
        "Integer Tag",
        (DataInputStream input, boolean readName) -> new NbtIntTag(input, readName),
        (String name) -> new NbtIntTag(name, 0)
    ),
    TAG_LONG(
        "Long Tag",
        (DataInputStream input, boolean readName) -> new NbtLongTag(input, readName),
        (String name) -> new NbtLongTag(name, 0)
    ),
    TAG_FLOAT(
        "Float Tag",
        (DataInputStream input, boolean readName) -> new NbtFloatTag(input, readName),
        (String name) -> new NbtFloatTag(name, 0)
    ),
    TAG_DOUBLE(
        "Double Tag",
        (DataInputStream input, boolean readName) -> new NbtDoubleTag(input, readName),
        (String name) -> new NbtDoubleTag(name, 0)
    ),
    TAG_BYTE_ARRAY(
        "Byte Array Tag",
        (DataInputStream input, boolean readName) -> new NbtByteArrayTag(input, readName),
        (String name) -> new NbtByteArrayTag(name, new byte[0])
    ),
    TAG_STRING(
        "String Tag",
        (DataInputStream input, boolean readName) -> new NbtStringTag(input, readName),
        (String name) -> new NbtStringTag(name, "")
    ),
    TAG_LIST(
        "List Tag",
        (DataInputStream input, boolean readName) -> new NbtListTag(input, readName),
        (String name) -> new NbtListTag(name, new ArrayList<>(), TAG_END)
    ),
    TAG_COMPOUND(
        "Compound Tag",
        (DataInputStream input, boolean readName) -> new NbtCompoundTag(input, readName),
        (String name) -> new NbtCompoundTag(name, new ArrayList<>())
    ),
    TAG_INT_ARRAY(
        "Integer Array Tag",
        (DataInputStream input, boolean readName) -> new NbtIntArrayTag(input, readName),
        (String name) -> new NbtIntArrayTag(name, new int[0])
    ),
    TAG_LONG_ARRAY(
        "Long Array Tag",
        (DataInputStream input, boolean readName) -> new NbtLongArrayTag(input, readName),
        (String name) -> new NbtLongArrayTag(name, new long[0])
    )
    ;
    
    public final String name;
    private final Instantiater instantiater;
    private final EmptyInstantiater emptyInstantiater;
    
    NbtTypes(String name, Instantiater instantiater, EmptyInstantiater emptyInstantiater){
        this.name = name;
        this.instantiater = instantiater;
        this.emptyInstantiater = emptyInstantiater;
    }
    
    public static NbtTypes getTypeFromId(int id){
        if(id < 0 || id >= NbtTypes.values().length){
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
    
    public NbtTag instantiateEmpty(String name) {
        return this.emptyInstantiater.createEmpty(name);
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
    
    public interface EmptyInstantiater {
        NbtTag createEmpty(String name);
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
    
    @Override
    public String toString() {
        return this.name;
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
