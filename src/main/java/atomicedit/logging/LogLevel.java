
package atomicedit.logging;

/**
 *
 * @author Justin Bonner
 */
public enum LogLevel {
    DEBUG,
    INFO,
    NOTICE,
    WARNING,
    ERROR,
    CRITICAL
    ;
    
    public boolean isLessImportantThan(LogLevel other) {
        return this.ordinal() < other.ordinal();
    }
    
    public static LogLevel of(String levelName) {
        return LogLevel.valueOf(levelName.toUpperCase());
    }
    
}
