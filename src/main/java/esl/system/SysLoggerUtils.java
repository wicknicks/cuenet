package esl.system;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class SysLoggerUtils {

    private static boolean isLoggerInitialized = false;

    public static void initLogger() {
        if ( !isLoggerInitialized ) {
            BasicConfigurator.configure();
            Logger.getRootLogger().setLevel(Level.INFO);
            isLoggerInitialized = true;
        }
    }

}
