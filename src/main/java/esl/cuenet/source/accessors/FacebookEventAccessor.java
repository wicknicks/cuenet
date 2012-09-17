package esl.cuenet.source.accessors;

import com.hp.hpl.jena.ontology.OntModel;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.IAccessor;
import esl.cuenet.source.SourceQueryException;
import esl.datastructures.Location;
import esl.datastructures.TimeInterval;
import org.apache.log4j.Logger;

public class FacebookEventAccessor extends MongoDB implements IAccessor {
    private Logger logger = Logger.getLogger(GoogleCalendarCollection.class);
    private Attribute[] attributes = null;
    private boolean[] setFlags = new boolean[1];
    private String eventAuthorName = null;
    private long startTime = -1;
    private long endTime = -1;
    private int errorMargin = 7200; // 2 hours
    private OntModel model = null;
    private TimeInterval timeInterval = null;

    public FacebookEventAccessor(OntModel model) {
        super(AccessorConstants.DBNAME);
        this.model = model;
    }

    public IResultSet query() {
        DBReader cursor = startReader("fb_events");

        if (timeInterval != null) {
            startTime = timeInterval.getStart()/1000;
            endTime = timeInterval.getEnd()/1000;
        }

        cursor.query(new BasicDBObject());
        BasicDBList result = new BasicDBList();

        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            if ( !object.containsField("start_time") ) continue;

            long rStartTime = Long.parseLong(((BasicDBObject)object).getString("start_time"));
            long rEndTime = Long.parseLong(((BasicDBObject)object).getString("end_time"));

            if (rStartTime < startTime && (rEndTime+errorMargin) > endTime ) {
                logger.info("Gott itttttttttt! " + object.get("name"));
                result.add(object);
            }

            //else logger.info("IGNORING: " + ((BasicDBObject) object).getString("name") + " " + (rStartTime - startTime)  + " " + (rEndTime - endTime));
        }

        logger.info("Found " + result.size() + " facebook entries.");
        return null;
    }

    @Override
    public void setAttributeNames(Attribute[] attributes) throws AccesorInitializationException {
        this.attributes = attributes;
    }

    @Override
    public void start() {
        for (int i=0; i<setFlags.length; i++) setFlags[i] = false;

        /* re-init for next query */
        timeInterval = null;
        startTime = -1;
        endTime = -1;
    }

    @Override
    public void associateTimeInterval(Attribute attribute, TimeInterval timeInterval) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[2])==0) {
            this.timeInterval = timeInterval;
        }
        else
            throw new AccesorInitializationException("TimeInterval value being initialized for wrong attribute "
                    + FacebookUserAccessor.class.getName());
    }

    @Override
    public void associateLocation(Attribute attribute, Location timeInterval) throws AccesorInitializationException {
        throw new AccesorInitializationException("Incorrect Assignment: No location attributes in "
                + YahooPlaceFinderAPI.class.getName());
    }

    @Override
    public void associateLong(Attribute attribute, long value) throws AccesorInitializationException {
        throw new AccesorInitializationException("Long value being initialized for wrong attribute "
                + FacebookUserAccessor.class.getName());
    }

    @Override
    public void associateString(Attribute attribute, String value) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[1]) == 0)
            eventAuthorName = value;
        else throw new AccesorInitializationException("String value being initialized for wrong attribute "
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

}
