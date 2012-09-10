package esl.cuenet.source.accessors;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import esl.cuenet.model.Constants;
import esl.cuenet.query.IResultIterator;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.ResultIterator;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.IAccessor;
import esl.cuenet.source.SourceQueryException;
import esl.datastructures.Location;
import esl.datastructures.TimeInterval;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConferenceAccessor extends MongoDB implements IAccessor {

    private OntModel model = null;
    private Attribute[] attributes = null;
    private boolean[] setFlags = new boolean[3];
    private TimeInterval interval = null;
    private Location location = null;
    private String ltitle, stitle, url;
    private Logger logger = Logger.getLogger(ConferenceAccessor.class);

    public ConferenceAccessor(OntModel model) {
        super(AccessorConstants.DBNAME);
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
                    + ConferenceAccessor.class.getName());
        }
    }

    @Override
    public void associateLocation(Attribute attribute, Location location) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[1])==0) {
            this.location = location;
            setFlags[1] = true;
        } else {
            throw new AccesorInitializationException("Location value being initialized for wrong attribute "
                    + ConferenceAccessor.class.getName());
        }
    }

    @Override
    public void associateLong(Attribute attribute, long value) throws AccesorInitializationException {
        throw new AccesorInitializationException("Long value being initialized for wrong attribute "
                + ConferenceAccessor.class.getName());
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
        else throw new AccesorInitializationException("String value being initialized for wrong attribute "
                + ConferenceAccessor.class.getName());
    }

    @Override
    public void associateDouble(Attribute attribute, double value) throws AccesorInitializationException {
        throw new AccesorInitializationException("Long value being initialized for wrong attribute "
                + ConferenceAccessor.class.getName());
    }

    private IResultSet query() {
        BasicDBObject query = new BasicDBObject();
        if (interval != null) {
            long start = interval.getStart();
            long end = interval.getEnd();
            BasicDBObject tio = (BasicDBObject) JSON.parse(String.format("{\"start-date\" : { \"$lt\" : %d} , \"end-date\" : { \"$gt\" : %d}}", start, end));
            query.putAll(tio.toMap());
        }

        if (this.ltitle != null) query.put("title", this.ltitle);
        if (this.stitle != null) query.put("short", this.stitle);
        if (this.url != null) query.put("url", this.url);

        DBReader reader = startReader("conferences");
        reader.query(query);

        OntClass conferenceClass = model.getOntClass(Constants.CuenetNamespace + "conference");
        Property titleProperty = model.getProperty(Constants.CuenetNamespace + "title");
        Property urlProperty = model.getProperty(Constants.CuenetNamespace + "url");
        Property nameProperty = model.getProperty(Constants.CuenetNamespace + "name");

        ResultSetImpl queryResultSet = new ResultSetImpl("Conference Results", model);

        while(reader.hasNext()) {
            BasicDBObject conf = (BasicDBObject) reader.next();
            Location confLocation = null;
            if (conf.containsField("location")) {
                confLocation = convertToLocation(conf.getString("location"));
                if (confLocation != null && location != null) {
                    if (!confLocation.liesWithinSameCity(location)) continue;
                }
            }

            long _sdate = conf.getLong("start-date");
            long _edate = conf.getLong("end-date");
            String _title = conf.getString("title");
            String _short = conf.getString("short");
            String _url = conf.getString("url");

            if (_short == null) _short = "";

            Individual confIndividual = conferenceClass.createIndividual(Constants.CuenetNamespace + "conference_" + _short.replace(" ", "_"));
            TimeInterval confTimeInterval = TimeInterval.createFromInterval(_sdate, _edate, model);

            if (_title != null) confIndividual.addLiteral(titleProperty, _title);
            confIndividual.addLiteral(nameProperty, _short);
            if (_url != null) confIndividual.addLiteral(urlProperty, _url);

            List<Individual> resultSet = new ArrayList<Individual>();
            resultSet.add(confIndividual);
            if (confTimeInterval != null) resultSet.add(confTimeInterval);
            if (confLocation != null) resultSet.add(confLocation);

            queryResultSet.addResult(resultSet);
            logger.info(conf);
        }

        return queryResultSet;
    }

    private Location convertToLocation(String location) {
        try {
            return Location.createFromAddress(location, model);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void search (TimeInterval intv, Location location) {
        this.interval = intv;
        this.location = location;
        if (intv != null) setFlags[0] = true;
        if (location != null) setFlags[1] = true;
        query();
    }

    @Override
    public IResultSet executeQuery() throws SourceQueryException {
        return query();
    }

}
