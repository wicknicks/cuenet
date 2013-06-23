package test.utils;

import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import javax.mail.internet.MailDateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeParser {

    private Logger logger = Logger.getLogger(DateTimeParser.class);

    public DateTimeParser() {
        SysLoggerUtils.initLogger();
    }

    @Test
    public void parseDateTime() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        Date f = sdf.parse("2009:08:27 18:00:00");  //1251421200000
        logger.info(f.getTime());
        f = sdf.parse("2009:08:27 23:59:59");  //1251442799000
        logger.info(f.getTime());
    }

    @Test
    public void parseMailDate() throws ParseException {
        MailDateFormat format = new MailDateFormat();
        Date dt = format.parse("Fri, 20 Apr 2012 02:51:25 +0100");
        logger.info(" Date: " + dt);

        Date dt2 = new Date(dt.getTime());
        logger.info(" Date: " + dt2);

        dt2 = new Date(0L);
        logger.info(" Date: " + dt2);
    }

    @Test
    public void parseTest3() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date f = sdf.parse("2012-07-27T18:29:39+0000");
        logger.info(f + " " + f.getTime());
    }

}
