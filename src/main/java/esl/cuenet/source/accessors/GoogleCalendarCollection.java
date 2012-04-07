package esl.cuenet.source.accessors;

import com.mongodb.BasicDBList;
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
    private String ownerEmail = null;
    private long startTime = -1;
    private long endTime = -1;
    private int errorMargin = 5;

    public GoogleCalendarCollection() {
        super("test");
    }

    public IResultSet query() {
        DBReader cursor = startReader("google_calendar");
        BasicDBObject query = new BasicDBObject();
        if (setFlags[1]) {
            query.put("email", ownerEmail);
        }
        if (setFlags[2]) {
            query.put("start-time", new BasicDBObject("$lt", startTime+(errorMargin *60*1000)));
            query.put("end-time", new BasicDBObject("$gt", endTime-(errorMargin *60*1000)));
        }
        cursor.query(query);

        BasicDBList result = new BasicDBList();
        while (cursor.hasNext()) result.add(cursor.next());

        if (result.size() > 3)
            return new ResultSetImpl("Found " + result.size() + " entires in google_calendar.");
        else return new ResultSetImpl(result.toString());
    }

    public BasicDBObject search(String username, long timestamp) {
        String queryTemplate = String.format("{\"username\": \"%s\", \"start-time\": {\"$lt\": %d}, " +
                "\"end-time\": {\"$gt\": %d}}", username,
                timestamp+(errorMargin *60*1000), timestamp-(errorMargin * 60 * 1000));

        DBReader cursor = startReader("google_calendar");
        BasicDBObject query = (BasicDBObject)JSON.parse(queryTemplate);

        cursor.query(query);
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

        /* re-init for next query */
        startTime = -1;
        endTime = -1;
    }

    @Override
    public void associateLong(Attribute attribute, long value) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[2])==0) {       /* name */
            setFlags[2] = true;
            if (this.startTime != -1) endTime = value;
            else startTime = value;
        }
        else
            throw new AccesorInitializationException("Long value being initialized for wrong attribute "
                    + FacebookUserAccessor.class.getName());
    }

    @Override
    public void associateString(Attribute attribute, String value) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[1])==0) {       /* name */
            setFlags[1] = true;
            this.ownerEmail = value;
        }
        else
            throw new AccesorInitializationException("String value being initialized for wrong attribute "
                    + FacebookUserAccessor.class.getName());
    }

    @Override
    public void associateDouble(Attribute attribute, double value) throws AccesorInitializationException {
        throw new AccesorInitializationException("Double value being initialized for wrong attribute "
                + FacebookUserAccessor.class.getName());
    }

    @Override
    public IResultSet executeQuery() throws SourceQueryException {
        return query();
    }

    private class ResultSetImpl implements IResultSet {
        private String result;
        public ResultSetImpl (String result) {this.result = result;}
        @Override
        public String printResults() {
            return result;
        }
    }

}

