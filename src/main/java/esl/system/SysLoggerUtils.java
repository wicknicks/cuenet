package esl.system;

import org.apache.log4j.BasicConfigurator;

public class SysLoggerUtils {

    private static boolean isLoggerInitialized = false;

    public static void initLogger() {
        if ( !isLoggerInitialized ) {
            BasicConfigurator.configure();
            isLoggerInitialized = true;
        }
    }

}
