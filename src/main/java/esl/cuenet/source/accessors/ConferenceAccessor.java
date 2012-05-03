package esl.cuenet.source.accessors;

import com.hp.hpl.jena.ontology.OntModel;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.IAccessor;
import esl.cuenet.source.SourceQueryException;
import esl.datastructures.Location;
import esl.datastructures.TimeInterval;
import org.apache.log4j.Logger;

public class ConferenceAccessor extends MongoDB implements IAccessor {

    private OntModel model = null;
    private Attribute[] attributes = null;
    private boolean[] setFlags = new boolean[3];
    private TimeInterval interval = null;
    private Location location = null;
    private String ltitle, stitle, url;
    private Logger logger = Logger.getLogger(ConferenceAccessor.class);

    public ConferenceAccessor(OntModel model) {
        super("test");
        this.model = model;
    }


    @Override
    public void setAttributeNames(Attribute[] attributes) throws AccesorInitializationException {
        this.attributes = attributes;
    }

    @Override
    public void start() {
        for (int i=0; i<setFlags.length; i++) setFlags[i] = false;
        interval = null;
        location = null;
        ltitle = null;
        stitle = null;
        url = null;
    }

    @Override
    public void associateTimeInterval(Attribute attribute, TimeInterval timeInterval) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[0])==0) {
            this.interval = timeInterval;
            setFlags[0] = true;
        } else {
            throw new AccesorInitializationException("TimeInterval value being initialized for wrong attribute "
                    + FacebookUserAccessor.class.getName());
        }
    }

    @Override
    public void associateLocation(Attribute attribute, Location location) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[1])==0) {
            this.location = location;
            setFlags[1] = true;
        } else {
            throw new AccesorInitializationException("TimeInterval value being initialized for wrong attribute "
                    + FacebookUserAccessor.class.getName());
        }
    }

    @Override
    public void associateLong(Attribute attribute, long value) throws AccesorInitializationException {
        throw new AccesorInitializationException("Long value being initialized for wrong attribute "
                + FacebookUserAccessor.class.getName());
    }

    @Override
    public void associateString(Attribute attribute, String value) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[2])==0) {
            setFlags[2] = true;
            this.ltitle = value;
        }
        else if (attribute.compareTo(attributes[3])==0) {
            setFlags[3] = true;
            this.stitle = value;
        }
        else if (attribute.compareTo(attributes[4])==0) {
            setFlags[4] = true;
            this.url = value;
        }

    }

    @Override
    public void associateDouble(Attribute attribute, double value) throws AccesorInitializationException {
        throw new AccesorInitializationException("Long value being initialized for wrong attribute "
                + FacebookUserAccessor.class.getName());
    }

    private IResultSet query() {
        BasicDBObject query = new BasicDBObject();
        if (interval != null) {
            long start = interval.getStart();
            long end = interval.getEnd();
            BasicDBObject tio = (BasicDBObject) JSON.parse(String.format("{\"start-date\" : { \"$lt\" : %d} , \"end-date\" : { \"$gt\" : %d}}", start, end));
            query.putAll(tio.toMap());
        }

        DBReader reader = startReader("conferences");
        reader.query(query);

        while(reader.hasNext()) {
            BasicDBObject conf = (BasicDBObject) reader.next();
            logger.info(conf);
        }

        return null;
    }

    public void search (TimeInterval intv) {
        interval = intv;
        setFlags[0] = true;
        query();
    }

    @Override
    public IResultSet executeQuery() throws SourceQueryException {
        return query();
    }


}
