
package atomicedit.logging;

import atomicedit.settings.AtomicEditConstants;
import atomicedit.settings.LoggingSettingValues;
import atomicedit.settings.LoggingSettings;
import atomicedit.settings.LoggingSettingsCreator;
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
    
    private static PrintWriter logWriter;
    private static LogLevel minLevel;
    
    public static void initialize(){
        LoggingSettingValues logSettings = LoggingSettingsCreator.createSettings();
        minLevel = LogLevel.of(logSettings.getSettingValueAsString(LoggingSettings.LOGGING_LEVEL));
        if (logSettings.getSettingValueAsBoolean(LoggingSettings.WRITE_LOGS_TO_FILE)) {
            if(!new File(AtomicEditConstants.ATOMIC_EDIT_INSTALL_PATH).exists()){
                new File(AtomicEditConstants.ATOMIC_EDIT_INSTALL_PATH).mkdir();
            }
            String logFilePath = logSettings.getSettingValueAsString(LoggingSettings.LOG_FILE_PATH);
            File logFile = new File(logFilePath);
            logFile.delete();
            try{
                logFile.createNewFile();
                logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
            }catch(IOException e){
                System.err.println("Could not set up log system.");
                e.printStackTrace(System.err);
            }
        }
        info("Initialized Logging System.");
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
        if (e != null) {
            e.printStackTrace(System.out);
        }
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
