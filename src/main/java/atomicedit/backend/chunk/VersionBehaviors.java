
package atomicedit.backend.chunk;

import atomicedit.backend.nbt.NbtByteArrayTag;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.utils.GeneralUtils;
import atomicedit.backend.nbt.NbtCompoundTag;
import atomicedit.backend.nbt.NbtIntArrayTag;
import atomicedit.logging.Logger;

/**
 * This class holds the code to parse chunk data as required by different data versions.
 * @author Justin Bonner
 */
class VersionBehaviors {
    
    
    static long[] packBlockIds_1_13(int[] localBlockIds, int indexSize) {
        return GeneralUtils.writeIntArrayToPackedLongArray(localBlockIds, indexSize);
    }
    
    static int readBlockIdFromPackedIds_1_13(int elementSize, int offset, long[] source) {
        return GeneralUtils.readIntFromPackedLongArray(elementSize, offset, source);
    }
    
    static long[] packBlockIds_1_16(int[] localBlockIds, int indexSize) {
        return GeneralUtils.writeIntArrayToLongArray(localBlockIds, indexSize);
    }
    
    
    static int readBlockIdFromPackedIds_1_16(int elementSize, int offset, long[] source) {
        return GeneralUtils.readIntFromLongArray(elementSize, offset, source);
    }
    
    static int[] parseBiomes_1_13(NbtCompoundTag chunkTag) throws MalformedNbtTagException {
        NbtByteArrayTag biomesTag = chunkTag.getCompoundTag("Level").getByteArrayTag("Biomes");
        byte[] byteBiomes = biomesTag.getPayload();
        int[] biomes = new int[byteBiomes.length];
        for (int i = 0; i < byteBiomes.length; i++) {
            biomes[i] = byteBiomes[i];
        }
        return biomes;
    }
    
    static void writeBiomes_1_13(NbtCompoundTag chunkTag, int[] biomes) throws MalformedNbtTagException {
        byte[] byteBiomes = new byte[biomes.length];
        boolean didWarn = false;
        for (int i = 0; i < byteBiomes.length; i++) {
            if (biomes[i] > Byte.MAX_VALUE && !didWarn) {
                Logger.warning("Trying to put a biome (" + biomes[i] + ") into a chunk version that does not support it.");
                didWarn = true;//don't need a million of the same error message
            }
            byteBiomes[i] = (byte)biomes[i];
        }
        chunkTag.getCompoundTag("Level").putTag(new NbtByteArrayTag("Biomes", byteBiomes));
    }
    
    static int[] parseBiomes_1_16(NbtCompoundTag chunkTag) throws MalformedNbtTagException {
        if (!chunkTag.getCompoundTag("Level").contains("Biomes")) {
            return null;
        }
        return chunkTag.getCompoundTag("Level").getIntArrayTag("Biomes").getPayload();
    }
    
    static void writeBiomes_1_16(NbtCompoundTag chunkTag, int[] biomes) throws MalformedNbtTagException {
        if (biomes == null) {
            return;
        }
        chunkTag.getCompoundTag("Level").putTag(new NbtIntArrayTag("Biomes", biomes));
    }
    
}
