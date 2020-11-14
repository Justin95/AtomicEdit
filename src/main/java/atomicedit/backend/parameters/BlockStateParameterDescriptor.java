
package atomicedit.backend.parameters;

import atomicedit.backend.BlockState;

/**
 *
 * @author Justin Bonner
 */
public class BlockStateParameterDescriptor extends ParameterDescriptor<BlockState> {
    
    public BlockStateParameterDescriptor(String name, BlockState defaultValue) {
        super(name, ParameterType.BLOCK_SELECTOR, defaultValue);
    }
    
}
