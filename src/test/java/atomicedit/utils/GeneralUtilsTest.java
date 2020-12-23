
package atomicedit.utils;

import atomicedit.backend.utils.GeneralUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Justin Bonner
 */
public class GeneralUtilsTest {
    
    @Test
    public void testYZXIndex() {
        int index = GeneralUtils.getIndexYZX(0, 10, 0, 10, 10);
        Assert.assertEquals(1000, index);
        
        index = GeneralUtils.getIndexYZX(17, 10, 18, 18, 19);
        Assert.assertEquals(3761, index);
    }
    
}
