
package com.atomicedit.backend;

/**
 *
 * @author Justin Bonner
 */
public class BlockStateProperty {
    
    public final String NAME;
    public final Object VALUE;
    public final BlockStateDataType valueType;
    
    public BlockStateProperty(String name, String value){
        this.NAME = name;
        this.VALUE = value;
        this.valueType = BlockStateDataType.STRING;
    }
    
    public BlockStateProperty(String name, int value){
        this.NAME = name;
        this.VALUE = value;
        this.valueType = BlockStateDataType.INTEGER;
    }
    
    public BlockStateProperty(String name, boolean value){
        this.NAME = name;
        this.VALUE = value;
        this.valueType = BlockStateDataType.BOOLEAN;
    }
    
    public enum BlockStateDataType {
        BOOLEAN,
        INTEGER,
        STRING
    }
    
}
