
package atomicedit.backend;

import atomicedit.backend.lighting.LightingBehavior;
import atomicedit.logging.Logger;
import atomicedit.utils.ArrayUtils;
import atomicedit.utils.OneFromEachIterator;
import atomicedit.utils.Tuple;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author Justin Bonner
 */
public class BlockState {
    
    public final String name;
    public final BlockStateProperty[] blockStateProperties;
    public final LightingBehavior lightingBehavior;
    private final String stringDesc;
    /**
     * Only have one immutable BlockState object per block state
     */
    private static HashMap<String, ArrayList<BlockState>> blockLibrary = new HashMap<>();
    public static final BlockState AIR = getBlockState("minecraft:air", null); //need a block to fill empty chunk sections with
    
    private BlockState(String name, BlockStateProperty[] blockStateProperties, LightingBehavior lightingBehavior){
        if(name == null){
            throw new IllegalArgumentException("Block name cannot be null");
        }
        if(blockStateProperties != null && blockStateProperties.length == 0){
            blockStateProperties = null;
        }
        this.name = name;
        this.blockStateProperties = blockStateProperties;
        this.stringDesc = name + (blockStateProperties != null ? "#" + Arrays.toString(blockStateProperties) : "");
        this.lightingBehavior = lightingBehavior;
    }
    
    public static BlockState getBlockState(String name, BlockStateProperty[] blockStateProperties){
        if(blockStateProperties != null && blockStateProperties.length == 0){
            blockStateProperties = null;
        }
        BlockState newType = new BlockState(name, blockStateProperties, LightingBehavior.DEFAULT_FULL_BLOCK);
        if(blockLibrary.containsKey(name)){
            ArrayList<BlockState> potentialTypes = blockLibrary.get(name);
            for(BlockState type : potentialTypes){
                if(newType.equals(type)){
                    return type; //allow newType to be garbage collected
                }
            }
            potentialTypes.add(newType);
            GlobalBlockStateMap.addBlockType(newType);
            return newType;
        }
        ArrayList<BlockState> newList = new ArrayList<>();
        newList.add(newType);
        blockLibrary.put(name, newList);
        GlobalBlockStateMap.addBlockType(newType);
        return newType;
    }
    
    private static void createBlockState(String name, BlockStateProperty[] blockStateProperties, LightingBehavior lightingBehavior) {
        if(blockStateProperties != null && blockStateProperties.length == 0){
            blockStateProperties = null;
        }
        BlockState newType = new BlockState(name, blockStateProperties, lightingBehavior);
        if(blockLibrary.containsKey(name)){
            ArrayList<BlockState> potentialTypes = blockLibrary.get(name);
            for(BlockState type : potentialTypes){
                if(newType.equals(type)){
                    return; //already exists
                }
            }
            potentialTypes.add(newType);
            GlobalBlockStateMap.addBlockType(newType);
            return;
        }
        ArrayList<BlockState> newList = new ArrayList<>();
        newList.add(newType);
        blockLibrary.put(name, newList);
        GlobalBlockStateMap.addBlockType(newType);
    }
    
    public static String getLoadedBlockTypesDebugString(){
        StringBuilder strBuilder = new StringBuilder();
        for(String str : blockLibrary.keySet()){
            strBuilder.append(blockLibrary.get(str));
            strBuilder.append("\n");
        }
        return strBuilder.toString();
    }
    
    @Override
    public String toString(){
        return stringDesc;
    }
    
    private boolean equals(BlockState other){
        //do not compare lighting behavior
        return other.name.equals(this.name) && (Arrays.deepEquals(this.blockStateProperties, other.blockStateProperties));
    }
    
    public boolean hasProperty(String propertyName){
        for(int i = 0; i < blockStateProperties.length; i++){
            if(blockStateProperties[i].NAME.equals(propertyName)){
                return true;
            }
        }
        return false;
    }
    
    public Object getProperty(String propertyName){
        for(int i = 0; i < blockStateProperties.length; i++){
            if(blockStateProperties[i].NAME.equals(propertyName)){
                return blockStateProperties[i].VALUE;
            }
        }
        return null;
    }
    
    public static void debugPrintAllBlockStates() {
        JsonArray blockStatesJson = new JsonArray();
        blockLibrary.entrySet().stream().sorted(
            Comparator.comparing((entry) -> entry.getKey())
        ).forEach((entry) -> {
            JsonObject stateJson = new JsonObject();
            stateJson.addProperty("name", entry.getKey());
            Map<String, Set<Object>> propNameToPossibleValues = new HashMap<>();
            for (BlockState blockState : entry.getValue()) {
                if (blockState.blockStateProperties == null) {
                    continue;
                }
                for (BlockStateProperty property : blockState.blockStateProperties) {
                    if (!propNameToPossibleValues.containsKey(property.NAME)) {
                        propNameToPossibleValues.put(property.NAME, new HashSet<>());
                    }
                    propNameToPossibleValues.get(property.NAME).add(property.VALUE);
                }
            }
            if (!propNameToPossibleValues.isEmpty()) {
                JsonArray propsJson = new JsonArray();
                for (Entry<String, Set<Object>> propAndValues : propNameToPossibleValues.entrySet()) {
                    JsonObject propJson = new JsonObject();
                    propJson.addProperty("name", propAndValues.getKey());
                    JsonArray valuesJson = new JsonArray();
                    for (Object val : propAndValues.getValue()) {
                        if (val instanceof String) {
                            propJson.addProperty("type", "string");
                            valuesJson.add((String)val);
                        } else if (val instanceof Integer) {
                            propJson.addProperty("type", "int");
                            valuesJson.add((Integer)val);
                        } else if (val instanceof Boolean) {
                            propJson.addProperty("type", "boolean");
                            valuesJson.add((Boolean)val);
                        }
                    }
                    propJson.add("values", valuesJson);
                    propsJson.add(propJson);
                }
                stateJson.add("properties", propsJson);
            }
            blockStatesJson.add(stateJson);
        });
        Logger.info("Num json elements: " + blockStatesJson.size());
        Logger.info(new GsonBuilder().setPrettyPrinting().create().toJson(blockStatesJson));
    }
    
    /**
     * Create instances for block states configured in known_block_states.json
     * This allows them to be created with correct lighting properties.
     */
    public static void loadKnownBlockStates() {
        final String KNOWN_BLOCK_STATES_FILE_PATH = "/data/known_block_states.json";
        JsonArray blockStatesJson = new JsonParser().parse(readFile(KNOWN_BLOCK_STATES_FILE_PATH)).getAsJsonArray();
        for (JsonElement blockStateJson : blockStatesJson) {
            if (!blockStateJson.isJsonObject()) {
                throw new RuntimeException("Improperly formatted known_block_states json.");
            }
            JsonObject stateJson = blockStateJson.getAsJsonObject();
            //name, properties, lighting
            String name = stateJson.get("name").getAsString();
            BlockStateProperty[][] allowedProperties = null;
            if (stateJson.has("properties")) {
                JsonArray propJsonArray = stateJson.getAsJsonArray("properties");
                int numCombinations = 0;
                for (JsonElement jsonElement : propJsonArray) {
                    int numValues = jsonElement.getAsJsonObject().getAsJsonArray("values").size();
                    if (numCombinations == 0) {
                        numCombinations = numValues;
                    } else {
                        numCombinations *= numValues;
                    }
                }
                allowedProperties = new BlockStateProperty[numCombinations][propJsonArray.size()]; //allowedProperties[i] is a valid property array
                ArrayList<BlockStateProperty[]> rawProperties = new ArrayList<>(propJsonArray.size());
                for (int i = 0; i < propJsonArray.size(); i++) {
                    JsonObject propertyDesc = propJsonArray.get(i).getAsJsonObject();
                    String propName = propertyDesc.get("name").getAsString();
                    String propType = propertyDesc.get("type").getAsString();
                    JsonArray values = propertyDesc.getAsJsonArray("values");
                    if (values.size() == 0) {
                        throw new RuntimeException("Block State Property cannot have no valid values.");
                    }
                    rawProperties.add(new BlockStateProperty[values.size()]);
                    for (int j = 0; j < values.size(); j++) {
                        BlockStateProperty property;
                        switch(propType) {
                            case "string":
                                String strValue = values.get(j).getAsString();
                                property = BlockStateProperty.getInstance(propName, strValue);
                                break;
                            case "int":
                                int intValue = values.get(j).getAsInt();
                                property = BlockStateProperty.getInstance(propName, intValue);
                                break;
                            case "boolean":
                                boolean boolValue = values.get(j).getAsBoolean();
                                property = BlockStateProperty.getInstance(propName, boolValue);
                                break;
                            default:
                                throw new RuntimeException("known_block_states.json in wrong format.");
                        }
                        rawProperties.get(i)[j] = property;
                    }
                }
                if (!rawProperties.isEmpty()) {
                    Iterator<List<BlockStateProperty>> iterator = new OneFromEachIterator(rawProperties);
                    int i = 0;
                    while(iterator.hasNext()) {
                        List<BlockStateProperty> properties = iterator.next();
                        for (int j = 0; j < properties.size(); j++) {
                            allowedProperties[i][j] = properties.get(j);
                        }
                        i++;
                    }
                }
            }
            //each tuple is a list of required block state properties for the lighting behavior to be valid for a blockstate
            List<Tuple<List<BlockStateProperty>, LightingBehavior>> propsToLighting = new ArrayList<>();
            if (stateJson.has("lighting_behavior")) {
                JsonElement lightBehaviorRoot = stateJson.get("lighting_behavior");
                if (lightBehaviorRoot.isJsonObject()) {
                    JsonObject lightJson = stateJson.getAsJsonObject("lighting_behavior");
                    int emitLevel = lightJson.get("emit_light_level").getAsInt();
                    boolean allowBlockLight = lightJson.get("allow_block_light").getAsBoolean();
                    boolean allowSkyLight = lightJson.get("allow_sky_light").getAsBoolean();
                    LightingBehavior lightBehavior = LightingBehavior.getInstance(emitLevel, allowBlockLight, allowSkyLight);
                    //no required block state properties for this lighting behavior
                    propsToLighting.add(new Tuple<>(new ArrayList<>(), lightBehavior));
                } else if (lightBehaviorRoot.isJsonArray()) {
                    JsonArray lightBehaviorArray = lightBehaviorRoot.getAsJsonArray();
                    for (int i = 0; i < lightBehaviorArray.size(); i++) {
                        JsonObject lightDescObj = lightBehaviorArray.get(i).getAsJsonObject();
                        JsonObject whenObj = lightDescObj.getAsJsonObject("when");
                        List<BlockStateProperty> reqProps = new ArrayList<>();
                        for (Entry<String, JsonElement> propEntry : whenObj.entrySet()) {
                            BlockStateProperty prop;
                            if (propEntry.getValue().getAsJsonPrimitive().isString()) {
                                prop = BlockStateProperty.getInstance(propEntry.getKey(), propEntry.getValue().getAsString());
                            } else if (propEntry.getValue().getAsJsonPrimitive().isNumber()) {
                                prop = BlockStateProperty.getInstance(propEntry.getKey(), propEntry.getValue().getAsInt());
                            } else if (propEntry.getValue().getAsJsonPrimitive().isBoolean()) {
                                prop = BlockStateProperty.getInstance(propEntry.getKey(), propEntry.getValue().getAsBoolean());
                            } else {
                                throw new RuntimeException("known_block_states.json in wrong format.");
                            }
                            //if this block has no properties or does not have this property
                            if (allowedProperties == null || !ArrayUtils.contains(allowedProperties, prop)) {
                                throw new RuntimeException("Lighting behavior is set for non existant property: " + prop);
                            }
                            reqProps.add(prop);
                        }
                        
                        JsonObject lightJson = lightDescObj.getAsJsonObject("lighting_behavior");
                        int emitLevel = lightJson.get("emit_light_level").getAsInt();
                        boolean allowBlockLight = lightJson.get("allow_block_light").getAsBoolean();
                        boolean allowSkyLight = lightJson.get("allow_sky_light").getAsBoolean();
                        LightingBehavior lightBehavior = LightingBehavior.getInstance(emitLevel, allowBlockLight, allowSkyLight);
                        //only use this lighting behavior if all required properties are present in the blockstate
                        propsToLighting.add(new Tuple<>(reqProps, lightBehavior));
                    }
                } else {
                    throw new RuntimeException("Incorrectly formatted known_block_states.json");
                }
            }
            
            if (allowedProperties == null) {
                LightingBehavior lightingBehavior = propsToLighting.isEmpty() ? LightingBehavior.DEFAULT_FULL_BLOCK : propsToLighting.get(0).right;
                BlockState.createBlockState(name, null, lightingBehavior);
            } else {
                for (int i = 0; i < allowedProperties.length; i++) {
                    LightingBehavior lightingBehavior = LightingBehavior.DEFAULT_FULL_BLOCK;
                    if (!propsToLighting.isEmpty()) {
                        FindLightingLoop:
                        for (Tuple<List<BlockStateProperty>, LightingBehavior> possLight : propsToLighting) {
                            if (ArrayUtils.containsAll(allowedProperties[i], possLight.left)) {
                                lightingBehavior = possLight.right;
                                break FindLightingLoop;
                            }
                        }
                    }
                    BlockState.createBlockState(name, allowedProperties[i], lightingBehavior);
                }
            }
        }
    }
    
    
    private static String readFile(String filepath){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(BlockState.class.getResourceAsStream(filepath)));
            StringBuilder contents = new StringBuilder();
            Scanner fileScanner;
            fileScanner = new Scanner(reader);
            while(fileScanner.hasNextLine()){
                contents.append(fileScanner.nextLine());
                contents.append("\n");
            }
            fileScanner.close();
            return contents.toString();
        }catch(Exception e){
            Logger.error("Exception in reading file: " + e);
            throw new RuntimeException(e);
        }
    }
    
    /*
    
    
    /**
     * Create instances for block states configured in known_block_states.json
     * This allows them to be created with correct lighting properties.
     /
    public static void loadKnownBlockStates() {
        final String KNOWN_BLOCK_STATES_FILE_PATH = "/data/known_block_states.json";
        JsonArray blockStatesJson = new JsonParser().parse(readFile(KNOWN_BLOCK_STATES_FILE_PATH)).getAsJsonArray();
        for (JsonElement blockStateJson : blockStatesJson) {
            if (!blockStateJson.isJsonObject()) {
                throw new RuntimeException("Improperly formatted known_block_states json.");
            }
            JsonObject stateJson = blockStateJson.getAsJsonObject();
            //name, properties, lighting
            String name = stateJson.get("name").getAsString();
            BlockStateProperty[][] allowedProperties = null;
            if (stateJson.has("properties")) {
                JsonArray propJsonArray = stateJson.getAsJsonArray("properties");
                int numCombinations = 0;
                for (JsonElement jsonElement : propJsonArray) {
                    int numValues = jsonElement.getAsJsonObject().getAsJsonArray("values").size();
                    if (numCombinations == 0) {
                        numCombinations = numValues;
                    } else {
                        numCombinations *= numValues;
                    }
                }
                allowedProperties = new BlockStateProperty[numCombinations][propJsonArray.size()]; //allowedProperties[i] is a valid property array
                ArrayList<BlockStateProperty[]> rawProperties = new ArrayList<>(propJsonArray.size());
                for (int i = 0; i < propJsonArray.size(); i++) {
                    JsonObject propertyDesc = propJsonArray.get(i).getAsJsonObject();
                    String propName = propertyDesc.get("name").getAsString();
                    String propType = propertyDesc.get("type").getAsString();
                    JsonArray values = propertyDesc.getAsJsonArray("values");
                    rawProperties.set(i, new BlockStateProperty[values.size()]);
                    for (int j = 0; j < values.size(); j++) {
                        BlockStateProperty property;
                        switch(propType) {
                            case "string":
                                String strValue = values.get(j).getAsString();
                                property = new BlockStateProperty(name, strValue);
                                break;
                            case "int":
                                int intValue = values.get(j).getAsInt();
                                property = new BlockStateProperty(name, intValue);
                                break;
                            case "boolean":
                                boolean boolValue = values.get(j).getAsBoolean();
                                property = new BlockStateProperty(name, boolValue);
                                break;
                            default:
                                throw new RuntimeException("known_block_states.json in wrong format.");
                        }
                        rawProperties.get(i)[j] = property;
                    }
                }
                if (!rawProperties.isEmpty()) {
                    int[] indexes = new int[rawProperties.size()];
                    for (int i = 0; i < indexes.si; i++) {
                        
                    }
                }
                
                /*
                for (int i = 0; i < properties.length; i++) {
                    JsonObject propJson = propJsonArray.get(i).getAsJsonObject();
                    String propName = propJson.get("name").getAsString();
                    String propType = propJson.get("type").getAsString();
                    BlockStateProperty property;
                    switch(propType) {
                        case "string":
                            String strValue = propJson.get("value").getAsString();
                            property = new BlockStateProperty(propName, strValue);
                            break;
                        case "int":
                            int intValue = propJson.get("value").getAsInt();
                            property = new BlockStateProperty(propName, intValue);
                            break;
                        case "boolean":
                            boolean boolValue = propJson.get("value").getAsBoolean();
                            property = new BlockStateProperty(propName, boolValue);
                            break;
                        default:
                            throw new RuntimeException("Improperly formatted known_block_states json. Bad Property Type: " + propType);
                    }
                    properties[i] = property;
                }
                /
            }
            
            LightingBehavior lightBehavior;
            if (stateJson.has("light_behavior")) {
                JsonObject lightJson = stateJson.getAsJsonObject("light_behavior");
                int emitLevel = lightJson.get("emit_light_level").getAsInt();
                boolean allowBlockLight = lightJson.get("allow_block_light").getAsBoolean();
                boolean allowSkyLight = lightJson.get("allow_sky_light").getAsBoolean();
                lightBehavior = LightingBehavior.getInstance(emitLevel, allowBlockLight, allowSkyLight);
            } else {
                lightBehavior = LightingBehavior.DEFAULT;
            }
            BlockState.createBlockState(name, properties, lightBehavior);
        }
    }
    
    public static void loadKnownBlockStates() {
        final String KNOWN_BLOCK_STATES_FILE_PATH = "/data/known_block_states.json";
        JsonArray blockStatesJson = new JsonParser().parse(readFile(KNOWN_BLOCK_STATES_FILE_PATH)).getAsJsonArray();
        for (JsonElement blockStateJson : blockStatesJson) {
            if (!blockStateJson.isJsonObject()) {
                throw new RuntimeException("Improperly formatted known_block_states json.");
            }
            JsonObject stateJson = blockStateJson.getAsJsonObject();
            //name, properties, lighting
            String name = stateJson.get("name").getAsString();
            BlockStateProperty[] properties = null;
            if (stateJson.has("properties")) {
                JsonArray propsJson = stateJson.getAsJsonArray("properties");
                properties = new BlockStateProperty[propsJson.size()];
                for (int i = 0; i < propsJson.size(); i++) {
                    JsonObject propJson = propsJson.get(i).getAsJsonObject();
                    String propName = propJson.get("name").getAsString();
                    String propType = propJson.get("type").getAsString();
                    BlockStateProperty property;
                    switch(propType) {
                        case "string":
                            property = BlockStateProperty.getInstance(propName, propJson.get("value").getAsString());
                            break;
                        case "int":
                            property = BlockStateProperty.getInstance(propName, propJson.get("value").getAsInt());
                            break;
                        case "boolean":
                            property = BlockStateProperty.getInstance(propName, propJson.get("value").getAsBoolean());
                            break;
                        default:
                            throw new RuntimeException("Invalid block states json. Bad property type: " + propType);
                    }
                    properties[i] = property;
                }
            }
            LightingBehavior lightBehavior;
            if (stateJson.has("light_behavior")) {
                JsonObject lightJson = stateJson.getAsJsonObject("light_behavior");
                int emitLevel = lightJson.get("emit_light_level").getAsInt();
                boolean allowBlockLight = lightJson.get("allow_block_light").getAsBoolean();
                boolean allowSkyLight = lightJson.get("allow_sky_light").getAsBoolean();
                lightBehavior = LightingBehavior.getInstance(emitLevel, allowBlockLight, allowSkyLight);
            } else {
                lightBehavior = LightingBehavior.DEFAULT;
            }
            Logger.info("Created Blockstate: " + name + " " + Arrays.toString(properties));
            BlockState.createBlockState(name, properties, lightBehavior);
        }
    }
    
    public static void debugPrintAllBlockStates() {
        JsonObject root = new JsonObject();
        JsonArray propertyTableJson = new JsonArray();
        List<BlockStateProperty> allProperties = BlockStateProperty.getAllLoadedProperties();
        int i = 0;
        for (BlockStateProperty property : allProperties) {
            JsonObject propObj = new JsonObject();
            propObj.addProperty("index", i++); //not read, only for json readability
            propObj.addProperty("name", property.NAME);
            switch(property.valueType) {
                case STRING:
                    propObj.addProperty("type", "string");
                    propObj.addProperty("value", (String)property.VALUE);
                    break;
                case INTEGER:
                    propObj.addProperty("type", "int");
                    propObj.addProperty("value", (Integer)property.VALUE);
                    break;
                case BOOLEAN:
                    propObj.addProperty("type", "boolean");
                    propObj.addProperty("value", (Boolean)property.VALUE);
                    break;
                default:
                    throw new RuntimeException("Invalid property type: " + property.valueType);
            }
            propertyTableJson.add(propObj);
        }
        root.add("properties", propertyTableJson);
        Logger.info("Num properties: " + propertyTableJson.size());
        
        JsonArray blockStatesJson = new JsonArray();
        blockLibrary.entrySet().stream().sorted(
            Comparator.comparing((entry) -> entry.getKey())
        ).forEach((entry) -> {
            for (BlockState blockState : entry.getValue()) {
                JsonObject stateJson = new JsonObject();
                stateJson.addProperty("name", entry.getKey());
                if (blockState.blockStateProperties != null) {
                    JsonArray propsJson = new JsonArray();
                    for (BlockStateProperty property : blockState.blockStateProperties) {
                        int propIndex = allProperties.indexOf(property);
                        propsJson.add(propIndex);
                    }
                    stateJson.add("properties", propsJson);
                }
                blockStatesJson.add(stateJson);
            }
        });
        root.add("block_states", blockStatesJson);
        Logger.info("Num block states: " + blockStatesJson.size());
        Logger.info(new GsonBuilder().setPrettyPrinting().create().toJson(root));
    }
    
    */
    
}
