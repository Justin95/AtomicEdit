
package atomicedit.logging;

import java.io.PrintStream;

/**
 *
 * @author Justin Bonner
 */
public class Logger {
    
    
    
    
    
    
    
    public static void info(String message){
        System.out.println(message);
    }
    
    public static void info(String message, Exception e){
        System.out.println(message);
        printException(System.out, e);
    }
    
    public static void notice(String message){
        System.out.println(message);
    }
    
    public static void notice(String message, Exception e){
        System.out.println(message);
        printException(System.out, e);
    }
    
    public static void warning(String message){
        System.out.println(message);
    }
    
    public static void warning(String message, Exception e){
        System.out.println(message);
        printException(System.out, e);
    }
    
    public static void error(String message){
        System.out.println(message);
    }
    
    public static void error(String message, Exception e){
        System.out.println(message);
        printException(System.out, e);
    }
    
    public static void critical(String message){
        System.out.println(message);
    }
    
    public static void critical(String message, Exception e){
        System.out.println(message);
        printException(System.out, e);
    }
    
    private static void printException(PrintStream location, Exception e){
        e.printStackTrace(location);
    }
    
}
