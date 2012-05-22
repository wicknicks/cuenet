package test.utils;

import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeParser {

    private Logger logger = Logger.getLogger(DateTimeParser.class);

    public DateTimeParser() {
        SysLoggerUtils.initLogger();
    }

    @Test
    public void parseDateTime() throws java.text.ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        Date f = sdf.parse("2009:08:27 18:00:00");  //1251421200000
        logger.info(f.getTime());
        f = sdf.parse("2009:08:27 23:59:59");  //1251442799000
        logger.info(f.getTime());
    }


}
