
package atomicedit.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Iterate over every combination of a list that consists of one element from each given T[].
 * None of the arrays may be empty.
 * @author Justin Bonner
 */
public class OneFromEachIterator<T> implements Iterator<List<T>> {
    
    private final List<T[]> source;
    private final int[] indexes;
    private boolean indexesReady;
    
    public OneFromEachIterator(List<T[]> source) {
        this.source = source;
        for (T[] t : source) {
            if (t.length == 0) {
                throw new IllegalArgumentException("Cannot use an empty array.");
            }
        }
        indexes = new int[source.size()];
        indexesReady = true;
    }
    
    @Override
    public boolean hasNext() {
        if (!indexesReady) {
            for (int i = 0 ; i < indexes.length; i++) {
                if (indexes[i] < source.get(i).length - 1) {
                    indexes[i]++;
                    break;
                } else {
                    if (i >= source.size() - 1) {
                        //done condition
                        return false;
                    }
                    indexes[i] = 0;
                }
            }
            indexesReady = true;
        }
        return true;
    }
    
    @Override
    public List<T> next() {
        if (!hasNext()) {
            return null;
        }
        List<T> combination = new ArrayList<>(source.size());
        for(int i = 0; i < source.size(); i++) {
            combination.set(i, source.get(i)[indexes[i]]);
        }
        return combination;
    }
    
}
