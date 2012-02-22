package esl.cuenet.source.accessors;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.IAccessor;
import esl.cuenet.source.SourceQueryException;
import org.apache.log4j.Logger;

public class GoogleCalendarCollection extends MongoDB implements IAccessor {

    private Logger logger = Logger.getLogger(GoogleCalendarCollection.class);
    private Attribute[] attributes = null;
    private boolean[] setFlags = new boolean[6];

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

    @Override
    public void setAttributeNames(Attribute[] attributes) throws AccesorInitializationException {
        this.attributes = attributes;
    }

    @Override
    public void start() {
        for (int i=0; i<setFlags.length; i++) setFlags[i] = false;
    }

    @Override
    public void associateLong(Attribute attribute, long value) throws AccesorInitializationException {
        //todo: implement method

    }

    @Override
    public void associateString(Attribute attribute, String value) throws AccesorInitializationException {
        //todo: implement method

    }

    @Override
    public void associateDouble(Attribute attribute, double value) throws AccesorInitializationException {
        //todo: implement method

    }

    @Override
    public IResultSet executeQuery() throws SourceQueryException {
        //todo: implement method
        return null;
    }
}

