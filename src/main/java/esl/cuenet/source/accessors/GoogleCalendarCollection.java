package esl.cuenet.source.accessors;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import org.apache.log4j.Logger;

public class GoogleCalendarCollection extends MongoDB {

    private Logger logger = Logger.getLogger(GoogleCalendarCollection.class);

    public GoogleCalendarCollection() {
        super("test");
    }

    public BasicDBObject search(String username, long timestamp) {
        int errorMargin = 5;
        String query = String.format("{\"username\": \"%s\", \"start-time\": {\"$lt\": %d}, " +
                "\"end-time\": {\"$gt\": %d}}", username,
                timestamp+(errorMargin *60*1000), timestamp-(errorMargin *60*1000));

        DBReader cursor = startReader("google_calendar");
        cursor.query((BasicDBObject)JSON.parse(query));
        logger.info("Found " + cursor.count() + " records");

        BasicDBObject tRecord = null;
        while (cursor.hasNext()) tRecord = (BasicDBObject) cursor.next();

        return tRecord;
    }

}

