package esl.cuenet.source.accessors;

import com.mongodb.BasicDBObject;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;

public class YahooPlaceFinderTest {

    private Logger logger = Logger.getLogger(YahooPlaceFinderTest.class);

    @Test
    public void runTest() throws IOException {

        YahooPlaceFinder placeFinder = new YahooPlaceFinder();
        BasicDBObject o = placeFinder.queryPlaceFinder(30.2669,-97.7428);
        if (o != null) logger.info(o.toString());

    }

}
