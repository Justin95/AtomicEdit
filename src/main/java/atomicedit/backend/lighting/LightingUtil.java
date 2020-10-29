
package atomicedit.backend.lighting;

import atomicedit.backend.BlockState;
import atomicedit.backend.ChunkSectionCoord;
import atomicedit.backend.GlobalBlockStateMap;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.ChunkSection;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.utils.GeneralUtils;
import atomicedit.logging.Logger;
import atomicedit.utils.Tuple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * It's far from perfect but I used an algorithm from a comment here.
 * https://gamedev.stackexchange.com/questions/19207/how-can-i-implement-voxel-based-lighting-with-occlusion-in-a-minecraft-style-gam
 * @author Justin Bonner
 */
public class LightingUtil {
    
    public static void doLightingCalculation(Map<ChunkCoord, ChunkController> chunkMap) throws MalformedNbtTagException {
        Logger.info("Starting lighting calculation with " + chunkMap.size() + " chunks.");
        LightingArea lightingArea = createLightingArea(chunkMap);
        LightingAreaIterator iterator = new LightingAreaIterator(lightingArea);
        //block light
        iterator.forEach((x, y, z) -> {
            LightingBehavior lightBehavior = lightingArea.getLightingBehaviorAt(x, y, z);
            lightingArea.setBlockLightAt(x, y, z, (byte)lightBehavior.emitLevel);
            if (!lightBehavior.allowBlockLight) {
                lightingArea.setEdited(x, y, z, true);
            }
        });
        for (int i = 15; i > 0; i--) {
            final int index = i;
            //iterate through each available light level from brightest to darkest
            iterator.forEach((x, y, z) -> {
                if (lightingArea.getBlockLightAt(x, y, z) != index) {
                    return;
                }
                
                int yPlusLight = lightingArea.getBlockLightAt(x, y + 1, z);
                if (!lightingArea.hasBeenEdited(x, y + 1, z) && yPlusLight < index - 1) {
                    lightingArea.setBlockLightAt(x, y + 1, z, (byte)(index - 1));
                    lightingArea.setEdited(x, y + 1, z, true);
                }
                
                int yMinusLight = lightingArea.getBlockLightAt(x, y - 1, z);
                if (!lightingArea.hasBeenEdited(x, y - 1, z) && yMinusLight < index - 1) {
                    lightingArea.setBlockLightAt(x, y - 1, z, (byte)(index - 1));
                    lightingArea.setEdited(x, y - 1, z, true);
                }
                
                int xPlusLight = lightingArea.getBlockLightAt(x + 1, y, z);
                if (!lightingArea.hasBeenEdited(x + 1, y, z) && xPlusLight < index - 1) {
                    lightingArea.setBlockLightAt(x + 1, y, z, (byte)(index - 1));
                    lightingArea.setEdited(x + 1, y, z, true);
                }
                
                int xMinusLight = lightingArea.getBlockLightAt(x - 1, y, z);
                if (!lightingArea.hasBeenEdited(x - 1, y, z) && xMinusLight < index - 1) {
                    lightingArea.setBlockLightAt(x - 1, y, z, (byte)(index - 1));
                    lightingArea.setEdited(x - 1, y, z, true);
                }
                
                int zPlusLight = lightingArea.getBlockLightAt(x, y, z + 1);
                if (!lightingArea.hasBeenEdited(x, y, z + 1) && zPlusLight < index - 1) {
                    lightingArea.setBlockLightAt(x, y, z + 1, (byte)(index - 1));
                    lightingArea.setEdited(x, y, z + 1, true);
                }
                
                int zMinusLight = lightingArea.getBlockLightAt(x, y, z - 1);
                if (!lightingArea.hasBeenEdited(x, y, z - 1) && zMinusLight < index - 1) {
                    lightingArea.setBlockLightAt(x, y, z - 1, (byte)(index - 1));
                    lightingArea.setEdited(x, y, z - 1, true);
                }
            });
        }
        lightingArea.clearEdited();
        Logger.info("Finished block lighting, starting sky lighting.");
        //skylight
        iterator.forEach((x, y, z) -> {
            LightingBehavior lightBehavior = lightingArea.getLightingBehaviorAt(x, y, z);
            int skyLight = y == 255 ? 15 : lightingArea.getSkyLightAt(x, y + 1, z);
            if (!lightBehavior.allowBlockLight) {
                skyLight = 0;
                lightingArea.setEdited(x, y, z, true);
            } else if (skyLight > 0 && (!lightBehavior.allowSkyLight || skyLight < 15)) {
                skyLight--;
            }
            if (skyLight == 15) {
                lightingArea.setEdited(x, y, z, true);
            }
            lightingArea.setSkyLightAt(x, y, z, (byte)skyLight);
        });
        for (int i = 15; i > 0; i--) {
            final int index = i;
            //iterate through each available light level from brightest to darkest
            iterator.forEach((x, y, z) -> {
                if (lightingArea.getSkyLightAt(x, y, z) != index) {
                    return;
                }
                
                int yPlusLight = lightingArea.getSkyLightAt(x, y + 1, z);
                if (!lightingArea.hasBeenEdited(x, y + 1, z) && yPlusLight < index - 1) {
                    lightingArea.setSkyLightAt(x, y + 1, z, (byte)(index - 1));
                    lightingArea.setEdited(x, y + 1, z, true);
                }
                
                int yMinusLight = lightingArea.getSkyLightAt(x, y - 1, z);
                if (!lightingArea.hasBeenEdited(x, y - 1, z) && yMinusLight < index - 1) {
                    lightingArea.setSkyLightAt(x, y - 1, z, (byte)(index - 1));
                    lightingArea.setEdited(x, y - 1, z, true);
                }
                
                int xPlusLight = lightingArea.getSkyLightAt(x + 1, y, z);
                if (!lightingArea.hasBeenEdited(x + 1, y, z) && xPlusLight < index - 1) {
                    lightingArea.setSkyLightAt(x + 1, y, z, (byte)(index - 1));
                    lightingArea.setEdited(x + 1, y, z, true);
                }
                
                int xMinusLight = lightingArea.getSkyLightAt(x - 1, y, z);
                if (!lightingArea.hasBeenEdited(x - 1, y, z) && xMinusLight < index - 1) {
                    lightingArea.setSkyLightAt(x - 1, y, z, (byte)(index - 1));
                    lightingArea.setEdited(x - 1, y, z, true);
                }
                
                int zPlusLight = lightingArea.getSkyLightAt(x, y, z + 1);
                if (!lightingArea.hasBeenEdited(x, y, z + 1) && zPlusLight < index - 1) {
                    lightingArea.setSkyLightAt(x, y, z + 1, (byte)(index - 1));
                    lightingArea.setEdited(x, y, z + 1, true);
                }
                
                int zMinusLight = lightingArea.getSkyLightAt(x, y, z - 1);
                if (!lightingArea.hasBeenEdited(x, y, z - 1) && zMinusLight < index - 1) {
                    lightingArea.setSkyLightAt(x, y, z - 1, (byte)(index - 1));
                    lightingArea.setEdited(x, y, z - 1, true);
                }
            });
        }
        lightingArea.writeResult(chunkMap);
        Logger.info("Finished lighting calculation.");
    }
    
    
    private static LightingArea createLightingArea(Map<ChunkCoord, ChunkController> chunkMap) throws MalformedNbtTagException {
        List<Tuple<ChunkSectionCoord, LightingSection>> lightSections = new ArrayList<>();
        final LightingBehavior[] blockIdToLightBehavior = calcLightBehaviorData();
        for (ChunkCoord chunkCoord : chunkMap.keySet()) {
            ChunkController chunkController = chunkMap.get(chunkCoord);
            boolean needsLightingCalc = chunkController.needsLightingCalc();
            int numSections = chunkController.chunkHeightInSections();
            for (int sectionIndex = numSections - 1; sectionIndex >= 0; sectionIndex--) {
                ChunkSection section = chunkController.getChunkSection(sectionIndex);
                short[] blocks = section.getBlockIds();
                byte[] blockLightData = section.getBlockLightValues();
                byte[] skyLightData = section.getSkyLightValues();
                //clear all existing lighting data in chunks being relit
                if (needsLightingCalc) {
                    Arrays.fill(blockLightData, (byte)0);
                    Arrays.fill(skyLightData, (byte)0);
                }
                LightingBehavior[] lightingBehaviors = new LightingBehavior[ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION];
                for (int index = 0; index < ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION; index++) {
                    lightingBehaviors[index] = blockIdToLightBehavior[blocks[index]];
                }
                LightingSection lightSection = new LightingSection(lightingBehaviors, blockLightData, skyLightData, needsLightingCalc);
                lightSections.add(new Tuple<>(section.coord, lightSection));
            }
        }
        return new LightingArea(lightSections);
    }
    
    /**
     * Create an array of lighting behaviors in which the index of the array corrisponds to the
     * global id of a block state (the id in the blockIds of a chunk section). 
     * @return 
     */
    private static LightingBehavior[] calcLightBehaviorData() {
        List<BlockState> blockStates = GlobalBlockStateMap.getBlockTypes();
        LightingBehavior[] lightingBehavior = new LightingBehavior[blockStates.size()];
        for (int i = 0; i < blockStates.size(); i++) {
            lightingBehavior[i] = blockStates.get(i).getLightingBehavior();
        }
        return lightingBehavior;
    }
    
    private static class LightingArea {
        
        private List<Tuple<ChunkSectionCoord, LightingSection>> sections;
        private Tuple<ChunkSectionCoord, LightingSection> lastLookup;
        
        LightingArea(List<Tuple<ChunkSectionCoord, LightingSection>> sections) {
            this.sections = sections;
            this.lastLookup = sections.get(0);
        }
        
        //absolute world coordinates for x,y,z
        LightingBehavior getLightingBehaviorAt(int x, int y, int z) {
            int localX = x & 0xF;
            int localY = y & 0xF;
            int localZ = z & 0xF;
            return getSectionFromWorldCoord(x, y, z).getLightingBehaviorAt(localX, localY, localZ);
        }
        
        byte getBlockLightAt(int x, int y, int z) {
            if (y > 255 || y < 0) {
                return 0;
            }
            int localX = x & 0xF;
            int localY = y & 0xF;
            int localZ = z & 0xF;
            return getSectionFromWorldCoord(x, y, z).getBlockLightAt(localX, localY, localZ);
        }
        
        void setBlockLightAt(int x, int y, int z, byte value) {
            int localX = x & 0xF;
            int localY = y & 0xF;
            int localZ = z & 0xF;
            getSectionFromWorldCoord(x, y, z).setBlockLightAt(localX, localY, localZ, value);
        }
        
        byte getSkyLightAt(int x, int y, int z) {
            if (y > 255 || y < 0) {
                return 0;
            }
            int localX = x & 0xF;
            int localY = y & 0xF;
            int localZ = z & 0xF;
            return getSectionFromWorldCoord(x, y, z).getSkyLightAt(localX, localY, localZ);
        }
        
        void setSkyLightAt(int x, int y, int z, byte value) {
            int localX = x & 0xF;
            int localY = y & 0xF;
            int localZ = z & 0xF;
            getSectionFromWorldCoord(x, y, z).setSkyLightAt(localX, localY, localZ, value);
        }
        
        boolean hasBeenEdited(int x, int y, int z) {
            if (y > 255 || y < 0) {
                return true; //dont edit those values
            }
            int localX = x & 0xF;
            int localY = y & 0xF;
            int localZ = z & 0xF;
            return getSectionFromWorldCoord(x, y, z).hasBeenEdited(localX, localY, localZ);
        }
        
        void setEdited(int x, int y, int z, boolean value) {
            int localX = x & 0xF;
            int localY = y & 0xF;
            int localZ = z & 0xF;
            getSectionFromWorldCoord(x, y, z).setEdited(localX, localY, localZ, value);
        }
        
        void clearEdited() {
            for (Tuple<ChunkSectionCoord, LightingSection> tuple : this.sections) {
                tuple.right.clearEdited();
            }
        }
        
        private LightingSection getSectionFromWorldCoord(int x, int y, int z) {
            int sectionX = (int) Math.floor((float)x / ChunkSection.SIDE_LENGTH);
            int sectionY = (int) Math.floor((float)y / ChunkSection.SIDE_LENGTH);
            int sectionZ = (int) Math.floor((float)z / ChunkSection.SIDE_LENGTH);
            if (lastLookup.left.x == sectionX && lastLookup.left.y == sectionY && lastLookup.left.z == sectionZ) {
                return lastLookup.right;
            }
            for (Tuple<ChunkSectionCoord, LightingSection> tuple : sections) {
                if (tuple.left.x == sectionX && tuple.left.y == sectionY && tuple.left.z == sectionZ) {
                    lastLookup = tuple;
                    return tuple.right;
                }
            }
            Logger.error("Tried to look up an index out of bounds in lighting calc (" + x + ", " + y + ", " + z + "). Valid Sections: " + sections);
            throw new RuntimeException("Tried to look out of bounds in lighting calc.");
        }
        
        LightingSection getSectionFromSectionCoord(ChunkSectionCoord sectionCoord) {
            for (Tuple<ChunkSectionCoord, LightingSection> tuple : sections) {
                if (tuple.left.equals(sectionCoord)) {
                    return tuple.right;
                }
            }
            Logger.error("Tried to look up an index out of bounds in lighting calc " + sectionCoord + ". Valid Sections: " + sections);
            throw new RuntimeException("Tried to look out of bounds in lighting calc.");
        }
        
        void writeResult(Map<ChunkCoord, ChunkController> chunkMap) throws MalformedNbtTagException {
            for (Tuple<ChunkSectionCoord, LightingSection> tuple : sections) {
                ChunkCoord chunkCoord = ChunkCoord.getInstance(tuple.left.x, tuple.left.z);
                ChunkController chunk = chunkMap.get(chunkCoord);
                chunk.setBlockLighting(tuple.left.y, tuple.right.blockLight);
                chunk.setSkyLighting(tuple.left.y, tuple.right.skyLight);
            }
        }
        
    }
    
    private static class LightingSection {
        
        public final boolean needsLightingCalc;
        private final LightingBehavior[] lightingBehavior;
        private final byte[] blockLight;
        private final byte[] skyLight;
        private final boolean[] edited;
        
        LightingSection(LightingBehavior[] lightingBehavior, byte[] blockLight, byte[] skyLight, boolean needsLightingCalc) {
            this.lightingBehavior = lightingBehavior;
            this.blockLight = blockLight;
            this.skyLight = skyLight;
            this.needsLightingCalc = needsLightingCalc;
            this.edited = new boolean[ChunkSection.NUM_BLOCKS_IN_CHUNK_SECTION];
        }
        
        //relative coordinates for x,y,z
        LightingBehavior getLightingBehaviorAt(int x, int y, int z) {
            int index = GeneralUtils.getIndexYZX(x, y, z, ChunkSection.SIDE_LENGTH);
            return this.lightingBehavior[index];
        }
        
        byte getBlockLightAt(int x, int y, int z) {
            return getLightAt(blockLight, x, y, z);
        }
        
        void setBlockLightAt(int x, int y, int z, byte value) {
            setLightAt(blockLight, x, y, z, value);
        }
        
        byte getSkyLightAt(int x, int y, int z) {
            return getLightAt(skyLight, x, y, z);
        }
        
        void setSkyLightAt(int x, int y, int z, byte value) {
            setLightAt(skyLight, x, y, z, value);
        }
        
        boolean hasBeenEdited(int x, int y, int z) {
            int index = GeneralUtils.getIndexYZX(x, y, z, ChunkSection.SIDE_LENGTH);
            return this.edited[index];
        }
        
        void setEdited(int x, int y, int z, boolean value) {
            int index = GeneralUtils.getIndexYZX(x, y, z, ChunkSection.SIDE_LENGTH);
            this.edited[index] = value;
        }
        
        void clearEdited() {
            Arrays.fill(edited, false);
        }
        
        private static byte getLightAt(byte[] lightArray, int x, int y, int z) {
            int index = GeneralUtils.getIndexYZX(x, y, z, ChunkSection.SIDE_LENGTH);
            int arrayIndex = index / 2; //4 bits per light, 2 lights per byte
            int offset = (index % 2) * 4;
            return (byte)((lightArray[arrayIndex] >> offset) & (byte)0b1111);
        }

        private static void setLightAt(byte[] lightArray, int x, int y, int z, byte value) {
            int index = GeneralUtils.getIndexYZX(x, y, z, ChunkSection.SIDE_LENGTH);
            int arrayIndex = index / 2; //4 bits per light, 2 lights per byte
            if (index % 2 == 0) {
                lightArray[arrayIndex] = (byte)((lightArray[arrayIndex] & 0b11110000) | (value & 0b00001111));
            } else {
                lightArray[arrayIndex] = (byte)((lightArray[arrayIndex] & 0b00001111) | ((value << 4) & 0b11110000));
            }
        }
        
    }
    
    private static class LightingAreaIterator {
        
        private final LightingArea area;
        
        LightingAreaIterator(LightingArea lightingArea) {
            this.area = lightingArea;
        }
        
        public void forEach(ForEachxyz callback) {
            List<ChunkSectionCoord> sectionCoords = area.sections.stream().map((tuple) -> tuple.left).sorted((a, b) -> b.y - a.y).collect(Collectors.toList());
            for (ChunkSectionCoord sectionCoord : sectionCoords) {
                LightingSection lightingSection = area.getSectionFromSectionCoord(sectionCoord);
                if (!lightingSection.needsLightingCalc) {
                    continue;
                }
                for (int y = ChunkSection.SIDE_LENGTH - 1; y >= 0; y--) {
                    for (int z = 0; z < ChunkSection.SIDE_LENGTH; z++) {
                        for (int x = 0; x < ChunkSection.SIDE_LENGTH; x++) {
                            int worldX = (sectionCoord.x * ChunkSection.SIDE_LENGTH) + x;
                            int worldY = (sectionCoord.y * ChunkSection.SIDE_LENGTH) + y;
                            int worldZ = (sectionCoord.z * ChunkSection.SIDE_LENGTH) + z;
                            callback.doIt(worldX, worldY, worldZ);
                        }
                    }
                }
            }
        }
        
        private static interface ForEachxyz {
            void doIt(int x, int y, int z);
        }
        
    }
    
}
