package esl.cuenet.source.accessors;

import com.mongodb.BasicDBObject;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;

public class GoogleCalendarCollectionTest {

    private Logger logger = Logger.getLogger(YahooPlaceFinderTest.class);

    @Test
    public void runTest() throws IOException {

        GoogleCalendarCollection calendarCollection = new GoogleCalendarCollection();
        BasicDBObject o = calendarCollection.search("arjun", 1296171000000L);
        if (o != null) logger.info(o.toString());

    }

}
