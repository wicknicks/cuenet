package esl.system;

import org.apache.log4j.BasicConfigurator;

public class Logger {

    private static boolean isLoggerInitialized = false;

    public static void initLogger() {
        if ( !isLoggerInitialized ) {
            BasicConfigurator.configure();
            isLoggerInitialized = true;
        }
    }

}
