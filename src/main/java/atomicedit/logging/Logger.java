
package atomicedit.logging;

import atomicedit.AtomicEdit;
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
    private static LogLevel minLevel = LogLevel.INFO; //use INFO until we can set the desired level from the settings
    
    public static void initialize(){
        minLevel = LogLevel.of(AtomicEdit.getSettings().getSettingValueAsString(AtomicEditSettings.LOGGING_LEVEL));
        if (AtomicEdit.getSettings().getSettingValueAsBoolean(AtomicEditSettings.WRITE_LOGS_TO_FILE)) {
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
    }
    
    public static void cleanUp(){
        info("Shutting down logging system.");
        if (logWriter != null) {
            logWriter.close();
        }
    }
    
    private static void log(String message, LogLevel level) {
        if (level.isLessImportantThan(minLevel)) {
            return;
        }
        if (logWriter != null) {
            logWriter.println(message);
        }
        System.out.println(message);
    }
    
    private static void log(String message, Exception e, LogLevel level) {
        if (level.isLessImportantThan(minLevel)) {
            return;
        }
        if (logWriter != null) {
            logWriter.println(message);
            printException(logWriter, e);
        }
        System.out.println(message);
        e.printStackTrace(System.out);
    }
    
    public static void debug(String message) {
        log(message, LogLevel.DEBUG);
    }
    
    public static void info(String message){
        log(message, LogLevel.INFO);
    }
    
    public static void info(String message, Exception e){
        log(message, e, LogLevel.INFO);
    }
    
    public static void notice(String message){
        log(message, LogLevel.NOTICE);
    }
    
    public static void notice(String message, Exception e){
        log(message, e, LogLevel.NOTICE);
    }
    
    public static void warning(String message){
        log(message, LogLevel.WARNING);
    }
    
    public static void warning(String message, Exception e){
        log(message, e, LogLevel.WARNING);
    }
    
    public static void error(String message){
        log(message, LogLevel.ERROR);
    }
    
    public static void error(String message, Exception e){
        log(message, e, LogLevel.ERROR);
    }
    
    public static void critical(String message){
        log(message, LogLevel.CRITICAL);
    }
    
    public static void critical(String message, Exception e){
        log(message, e, LogLevel.CRITICAL);
    }
    
    private static void printException(PrintWriter location, Exception e){
        if(e == null){
            return;
        }
        e.printStackTrace(location);
    }
    
}
