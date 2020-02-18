
package atomicedit.backend;

/**
 *
 * @author Justin Bonner
 */
public class GcThread extends Thread {
    
    private static final int GC_INTERVAL_MS = 2000;
    
    private boolean keepRunning = true;
    
    public GcThread() {
        this.setName("Atomic Edit Garbage Collector Thread");
    }
    
    @Override
    public void run() {
        while (keepRunning) {
            System.gc();
            try {
                Thread.sleep(GC_INTERVAL_MS);
            } catch (InterruptedException e) {

            }
        }
    }
    
    public void shutdown() {
        this.keepRunning = false;
    }
    
}
