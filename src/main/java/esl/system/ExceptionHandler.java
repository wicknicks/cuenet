package esl.system;

import org.apache.log4j.Logger;

public class ExceptionHandler {

    private int mode = -1;
    private Logger logger = Logger.getLogger(ExceptionHandler.class);
    public static int DEBUG = 0;
    public static int PRODUCTION = 1;

    public ExceptionHandler(int mode) {
        this.mode = mode;
        if (!(this.mode == DEBUG || this.mode == PRODUCTION)) {
            logger.info("Bad init");
            System.exit(1);
        }
    }

    public void handle(Exception ex) {
        if (mode == PRODUCTION)
            logger.error(ex.getClass().getCanonicalName() + " " + ex.getMessage(), ex);
        else if (mode == DEBUG)
            ex.printStackTrace();
    }

}
