
package atomicedit.backend.dimension;

import atomicedit.utils.FileUtils;
import java.io.File;
import java.util.ArrayList;
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
    
    public static List<Dimension> getDefaultDimensions() {
        return DEFAULT_DIMENSIONS;
    }
    
    public static List<Dimension> getDimensions(String saveFilePath) {
        List<Dimension> dimensions = new ArrayList<>();
        dimensions.addAll(DEFAULT_DIMENSIONS);
        dimensions.addAll(getCustomDimensions(saveFilePath));
        return Collections.unmodifiableList(dimensions);
    }
    
    private static List<Dimension> getCustomDimensions(String saveFilePath) {
        File dimFolder = new File(FileUtils.concatPaths(saveFilePath, "dimensions"));
        if (!(dimFolder.exists() && dimFolder.isDirectory())) {
            return Collections.EMPTY_LIST;
        }
        List<Dimension> customDimensions = new ArrayList<>();
        for (File datapackDir : dimFolder.listFiles((file) -> file.isDirectory())) {
            for (File custDimFolder : datapackDir.listFiles((file) -> file.isDirectory())) {
                String dimName = datapackDir.getName() + "/" + custDimFolder.getName();
                customDimensions.add(new Dimension(dimName, "dimensions/" + dimName));
            }
        }
        customDimensions.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
        return customDimensions;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
