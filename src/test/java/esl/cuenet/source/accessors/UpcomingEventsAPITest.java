package esl.cuenet.source.accessors;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;
import java.util.Calendar;

public class UpcomingEventsAPITest {

    private Logger logger = Logger.getLogger(UpcomingEventsAPITest.class);

    @Test
    public void runTest() throws IOException {
        UpcomingEventsAPI eventsAPI = new UpcomingEventsAPI();
        BasicDBList list = eventsAPI.searchUpcoming(33.642795, -117.845196, "2012-01-01", "2012-01-10");

        logger.info(list.toString());

        for (Object o : list) {
            BasicDBObject object = (BasicDBObject) o;
            logger.info(o.toString());
        }

        BasicDBList events = list;
        BasicDBList filteredEvents = new BasicDBList();

        String nameSubstring = "Steve";
        String descriptionSubstring = "Costa Mesa";
        
        for (Object e: events) {
            BasicDBObject event = (BasicDBObject) e;
            if ( !event.getString("name").contains(nameSubstring) ) continue;
            if ( !event.getString("description").contains(descriptionSubstring) ) continue;
            filteredEvents.add(event);
        }

        logger.info("Filtered Results: " + (filteredEvents.size()) + " " + filteredEvents);

    }

    @Test
    public void test() {
        long l = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(l);
        System.out.println(c.get(Calendar.YEAR));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(l);
        logger.info(String.format("%d-%02d-%02d", calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
    }

}
