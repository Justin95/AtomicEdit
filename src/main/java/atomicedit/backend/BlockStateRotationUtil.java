
package atomicedit.backend;

import atomicedit.backend.BlockStateProperty.BlockStateDataType;

/**
 * Utility code for rotating block states.
 * @author Justin Bonner
 */
public class BlockStateRotationUtil {
    
    
    
    static BlockState guessRotatedBlockState(BlockState blockState, int rightRots) {
        rightRots %= 4;
        if (blockState.blockStateProperties == null) {
            return blockState; //no properties means no rotation related properties
        }
        BlockStateProperty[] rotProps = new BlockStateProperty[blockState.blockStateProperties.length];
        for (int i = 0 ; i < blockState.blockStateProperties.length; i++) {
            rotProps[i] = RotationCases.getRotatedProperty(blockState.blockStateProperties[i], rightRots);
        }
        return BlockState.getBlockState(blockState.name, rotProps);
    }
    
    private static final String[] FACING_DIRS = {"north", "east", "south", "west"};
    
    private static enum RotationCases {
        AXIS(
            "axis",
            (oldProp, rightRots) -> {
                if (oldProp.valueType != BlockStateDataType.STRING) {
                    return oldProp;
                }
                if ("x".equals(oldProp.VALUE)) {
                    return rightRots == 1 || rightRots == 3 ? BlockStateProperty.getInstance("axis", "z") : BlockStateProperty.getInstance("axis", "x");
                }
                if ("z".equals(oldProp.VALUE)) {
                    return rightRots == 1 || rightRots == 3 ? BlockStateProperty.getInstance("axis", "x") : BlockStateProperty.getInstance("axis", "z");
                }
                return oldProp;
            }
        ),
        FACING(
            "facing",
            (oldProp, rightRots) -> {
                if (oldProp.valueType != BlockStateDataType.STRING) {
                    return oldProp;
                }
                if ("north".equals(oldProp.VALUE)) {
                    return BlockStateProperty.getInstance("facing", FACING_DIRS[(rightRots + 0) % 4]);
                }
                if ("east".equals(oldProp.VALUE)) {
                    return BlockStateProperty.getInstance("facing", FACING_DIRS[(rightRots + 1) % 4]);
                }
                if ("south".equals(oldProp.VALUE)) {
                    return BlockStateProperty.getInstance("facing", FACING_DIRS[(rightRots + 2) % 4]);
                }
                if ("west".equals(oldProp.VALUE)) {
                    return BlockStateProperty.getInstance("facing", FACING_DIRS[(rightRots + 3) % 4]);
                }
                return oldProp;
            }
        ),
        NORTH(
            "north",
            (oldProp, rightRots) -> {
                if (oldProp.valueType != BlockStateDataType.STRING) {
                    return oldProp;
                }
                return BlockStateProperty.getInstance(FACING_DIRS[(rightRots + 0) % 4], (String)oldProp.VALUE);
            }
        ),
        EAST(
            "east",
            (oldProp, rightRots) -> {
                if (oldProp.valueType != BlockStateDataType.STRING) {
                    return oldProp;
                }
                return BlockStateProperty.getInstance(FACING_DIRS[(rightRots + 1) % 4], (String)oldProp.VALUE);
            }
        ),
        SOUTH(
            "south",
            (oldProp, rightRots) -> {
                if (oldProp.valueType != BlockStateDataType.STRING) {
                    return oldProp;
                }
                return BlockStateProperty.getInstance(FACING_DIRS[(rightRots + 2) % 4], (String)oldProp.VALUE);
            }
        ),
        WEST(
            "west",
            (oldProp, rightRots) -> {
                if (oldProp.valueType != BlockStateDataType.STRING) {
                    return oldProp;
                }
                return BlockStateProperty.getInstance(FACING_DIRS[(rightRots + 3) % 4], (String)oldProp.VALUE);
            }
        ),
        ROTATION(
            "rotation",
            (oldProp, rightRots) -> {
                if (oldProp.valueType != BlockStateDataType.STRING) {
                    return oldProp;
                }
                if (!((String)oldProp.VALUE).matches("\\d+")) {
                    return oldProp;
                }
                int initRot = Integer.parseInt((String)oldProp.VALUE);
                if (initRot < 0 || initRot > 15) {
                    return oldProp;
                }
                int newRot = (initRot + (rightRots * 4)) % 16;
                return BlockStateProperty.getInstance("rotation", String.valueOf(newRot));
            }
        ),
        ;
        private final String propName;
        private final PropertyRotator rotator;
        
        RotationCases(String propName, PropertyRotator rotator) {
            this.propName = propName;
            this.rotator = rotator;
        }
        
        static BlockStateProperty getRotatedProperty(BlockStateProperty prop, int rightRots) {
            for (RotationCases rotCase : values()) {
                if (rotCase.propName.equals(prop.NAME)) {
                    return rotCase.rotator.getRotatedProperty(prop, rightRots);
                }
            }
            return prop;
        }
        
    }
    
    private interface PropertyRotator {
        BlockStateProperty getRotatedProperty(BlockStateProperty oldProp, int rightRots);
    }
    
}
