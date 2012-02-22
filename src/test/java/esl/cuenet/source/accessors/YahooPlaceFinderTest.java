package esl.cuenet.source.accessors;

import com.mongodb.BasicDBObject;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;

public class YahooPlaceFinderTest {

    private Logger logger = Logger.getLogger(YahooPlaceFinderTest.class);

    @Test
    public void runTest() throws IOException {

        YahooPlaceFinderAPI placeFinder = new YahooPlaceFinderAPI();
        BasicDBObject o = placeFinder.findAddress(30.2669, -97.7428);
        if (o != null) logger.info(o.toString());

        o = placeFinder.findLatLon("Eiffel Tower, Paris, France");
        if (o != null) logger.info(o.toString());

    }

}
