
package atomicedit.backend.lighting;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public class LightingBehavior {
    
    private static final List<LightingBehavior> BEHAVIOR_CACHE = new ArrayList<>(16 * 2 * 2);//one behavior per possible permuation
    public static final LightingBehavior DEFAULT;
    
    public final int emitLevel;
    public final boolean allowBlockLight;
    public final boolean allowSkyLight;
    
    private LightingBehavior(int emitLevel, boolean allowBlockLight, boolean allowSkyLight) {
        this.emitLevel = emitLevel;
        this.allowBlockLight = allowBlockLight;
        this.allowSkyLight = allowSkyLight;
    }
    
    static {
        for (int i = 0; i < 16; i++) {
            BEHAVIOR_CACHE.add(new LightingBehavior(i, false, false));
            BEHAVIOR_CACHE.add(new LightingBehavior(i, false, true));
            BEHAVIOR_CACHE.add(new LightingBehavior(i, true, false));
            BEHAVIOR_CACHE.add(new LightingBehavior(i, true, true));
        }
        DEFAULT = getInstance(0, false, false); //OPAQUE
    }
    
    public static LightingBehavior getInstance(int emitLevel, boolean allowBlockLight, boolean allowSkyLight) {
        for (LightingBehavior lb : BEHAVIOR_CACHE) {
            if (lb.emitLevel == emitLevel && lb.allowBlockLight == allowBlockLight && lb.allowSkyLight == allowSkyLight) {
                return lb;
            }
        }
        throw new RuntimeException("Could not find Block Lighting Behavior {" + emitLevel + ", " + allowBlockLight + ", " + allowSkyLight + "}");
    }
    
}
