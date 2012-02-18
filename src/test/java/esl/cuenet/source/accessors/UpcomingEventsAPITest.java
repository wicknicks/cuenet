package esl.cuenet.source.accessors;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;

public class UpcomingEventsAPITest {

    private Logger logger = Logger.getLogger(UpcomingEventsAPITest.class);

    @Test
    public void runTest() throws IOException {
        UpcomingEventsAPI eventsAPI = new UpcomingEventsAPI();
        BasicDBList list = eventsAPI.searchUpcoming(33.642795, -117.845196, "2012-01-01", "2012-01-10");
        for (Object o: list) {
            BasicDBObject object = (BasicDBObject) o;
            logger.info(o.toString());
        }
        
    }

}
