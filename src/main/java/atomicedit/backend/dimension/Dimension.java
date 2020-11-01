
package atomicedit.backend.dimension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public class Dimension {
    
    private static final List<Dimension> DEFAULT_DIMENSIONS = Collections.unmodifiableList(Arrays.asList(new Dimension[]{
        new Dimension(
            "Overworld",
            "." //same dir as world folder
        ),
        new Dimension(
            "Nether",
            "DIM-1"
        ),
        new Dimension(
            "The End",
            "DIM1"
        )
    }));
    
    public static final Dimension DEFAULT_DIMENSION = DEFAULT_DIMENSIONS.get(0); //overworld
    
    //TODO add dimension lighting properties, ie use skylight or not
    private final String name;
    private final String pathToDimFolder;
    
    private Dimension(String name, String pathToDimFolder) {
        this.name = name;
        this.pathToDimFolder = pathToDimFolder;
    }
    
    public String getName() {
        return this.name;
    }
    
    /**
     * Get the relative path from the world root directory to the dimension directory.
     * @return 
     */
    public String getSubPathToDimFolder() {
        return this.pathToDimFolder;
    }
    
    public static List<Dimension> getDimensions() {
        return DEFAULT_DIMENSIONS; //TODO add custom datapack dimension support
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
