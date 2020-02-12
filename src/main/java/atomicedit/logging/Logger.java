
package atomicedit.logging;

import atomicedit.settings.AtomicEditSettings;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author Justin Bonner
 */
public class Logger {
    
    private static final String LOG_FILE_PATH = AtomicEditSettings.ATOMIC_EDIT_INSTALL_PATH + "/AtomicEditLastLog.log";
    private static PrintWriter logWriter;
    
    public static void initialize(){
        if(!new File(AtomicEditSettings.ATOMIC_EDIT_INSTALL_PATH).exists()){
            new File(AtomicEditSettings.ATOMIC_EDIT_INSTALL_PATH).mkdir();
        }
        File logFile = new File(LOG_FILE_PATH);
        logFile.delete();
        try{
            logFile.createNewFile();
            logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
        }catch(IOException e){
            System.err.println("Could not set up log system.");
            e.printStackTrace(System.err);
        }
    }
    
    public static void cleanUp(){
        info("Shutting down logging system.");
        logWriter.close();
    }
    
    private static void log(String message) {
        logWriter.println(message);
        System.out.println(message);
    }
    
    private static void log(String message, Exception e) {
        logWriter.println(message);
        printException(logWriter, e);
        System.out.println(message);
        e.printStackTrace(System.out);
    }
    
    public static void info(String message){
        log(message);
    }
    
    public static void info(String message, Exception e){
        log(message, e);
    }
    
    public static void notice(String message){
        log(message);
    }
    
    public static void notice(String message, Exception e){
        log(message, e);
    }
    
    public static void warning(String message){
        log(message);
    }
    
    public static void warning(String message, Exception e){
        log(message, e);
    }
    
    public static void error(String message){
        log(message);
    }
    
    public static void error(String message, Exception e){
        log(message, e);
    }
    
    public static void critical(String message){
        log(message);
    }
    
    public static void critical(String message, Exception e){
        log(message, e);
    }
    
    private static void printException(PrintWriter location, Exception e){
        if(e == null){
            return;
        }
        e.printStackTrace(location);
    }
    
}
