package esl.cuenet.source.accessors;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.mongodb.BasicDBObject;
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
import esl.system.JsonUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConferenceTweetAccessor extends MongoDB implements IAccessor {

    OntModel model = null;
    private Attribute[] attributes;
    private boolean[] setFlags = new boolean[2];
    private String url = null;
    private String name = null;
    private SimpleDateFormat sdformatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
    private Property titleProperty = null;
    private OntClass personClass = null;
    private Property nameProperty = null;

    private Logger logger = Logger.getLogger(ConferenceTweetAccessor.class);


    public ConferenceTweetAccessor(OntModel model) {
        super(AccessorConstants.DBNAME);
        this.model = model;
        titleProperty = model.getProperty(Constants.CuenetNamespace + "title");
        personClass = model.getOntClass(Constants.CuenetNamespace + "person");
        nameProperty = model.getProperty(Constants.CuenetNamespace + "name");
    }

    @Override
    public void setAttributeNames(Attribute[] attributes) throws AccesorInitializationException {
        this.attributes = attributes;

    }

    @Override
    public void start() {
        for (int i=0; i<setFlags.length; i++) setFlags[i] = false;
        url = null;
        name = null;
    }

    @Override
    public void associateString(Attribute attribute, String value) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[0])==0) {
            this.url = value;
            setFlags[0] = true;
        } else if (attribute.compareTo(attributes[1]) == 0) {
            this.name = value;
            setFlags[1] = true;
        }
        else throw new AccesorInitializationException("Incorrect Assignment: String attributes in "
                + ConferenceTweetAccessor.class.getName());
    }

    @Override
    public void associateTimeInterval(Attribute attribute, TimeInterval timeInterval) throws AccesorInitializationException {
        throw new AccesorInitializationException("TimeInterval value being initialized for wrong attribute "
                + ConferenceTweetAccessor.class.getName());
    }

    @Override
    public void associateLocation(Attribute attribute, Location location) throws AccesorInitializationException {
        throw new AccesorInitializationException("Location value being initialized for wrong attribute "
                + ConferenceTweetAccessor.class.getName());
    }

    @Override
    public void associateLong(Attribute attribute, long value) throws AccesorInitializationException {
        throw new AccesorInitializationException("Long value being initialized for wrong attribute "
                + KeynoteAccessor.class.getName());
    }

    @Override
    public void associateDouble(Attribute attribute, double value) throws AccesorInitializationException {
        throw new AccesorInitializationException("Double value being initialized for wrong attribute "
                + ConferenceTweetAccessor.class.getName());
    }

    @Override
    public IResultSet executeQuery() throws SourceQueryException {
        ResultSetImpl resultSet = new ResultSetImpl("Tweets accessed for: " + url, model);
        if (url == null) return resultSet;

        BasicDBObject query = new BasicDBObject();
        query.put("ev_url", url);
        HashSet<String> usernames = new HashSet<String>(5);

        DBReader reader = startReader("tweets");
        reader.query(query);

        while(reader.hasNext()) {
            BasicDBObject o = (BasicDBObject) reader.next();
            if (JsonUtils.contains(o, "user.name")) {
                String name = JsonUtils.unnest(o, "user.name", String.class);
                if (usernames.contains(name)) continue;
                logger.info("Result: " + JsonUtils.unnest(o, "user"));
                resultSet.addResult(convert(o));
                usernames.add(name);
            }
        }

        return resultSet;
    }

    private List<Individual> convert(BasicDBObject object) {
        List<Individual> inds = new ArrayList<Individual>();

        Individual conference = null;
        if (object.containsField("type") && (object.getString("type").equals("conference"))) {
            try {
                conference = getConference(url);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Individual user = null;
        if (JsonUtils.contains(object, "user.name")) {
            String name = JsonUtils.unnest(object, "user.name", String.class);
            user = personClass.createIndividual(personClass.getURI() + "_" + name.replaceAll(" ", "_"));
            user.addProperty(nameProperty, name);
        }

        if (conference != null) inds.add(conference);
        if (user != null) inds.add(user);

        return inds;
    }

    private Individual getConference(String url) throws IOException, ParseException {
        OntClass conferenceClass = model.getOntClass(Constants.CuenetNamespace + "conference");
        BasicDBObject query = new BasicDBObject("link", url);
        DBReader reader = startReader("conferences");

        reader.query(query);

        BasicDBObject co;
        if (reader.hasNext()) {
            co = (BasicDBObject) reader.next();
        }
        else return null;

        Individual conference;
        Property occursAt = model.getProperty(Constants.CuenetNamespace + "occurs-at");
        Property occursDuring = model.getProperty(Constants.CuenetNamespace + "occurs-during");

        String title = null;
        if (co.containsField("title")) title = co.getString("title");

        if (title != null) conference = conferenceClass.createIndividual(conferenceClass.getURI() + "_" + title.replaceAll(" ", "_"));
        else conference = conferenceClass.createIndividual();

        TimeInterval interval = null;
        if (co.containsField("interval")) {
            long _start = sdformatter.parse(JsonUtils.unnest(co, "interval.start", String.class)).getTime();
            long _end = sdformatter.parse(JsonUtils.unnest(co, "interval.end", String.class)).getTime();
            interval = TimeInterval.createFromInterval(_start, _end, model);
        }

        Location location = null;
        if (JsonUtils.contains(co, "location.address")) {
            location = Location.createFromAddress(JsonUtils.unnest(co, "location.address", String.class), model);
        } else if (JsonUtils.contains(co, "location.geo")) {
            location = Location.createFromGPS(JsonUtils.unnest(co, "location.geo.lat", Double.class), JsonUtils.unnest(co, "location.geo.lon", Double.class), model);
        }

        if (location != null) conference.addProperty(occursAt, location);
        if (interval != null) conference.addProperty(occursDuring, interval);
        if (title != null) conference.addProperty(titleProperty, title);

        return conference;
    }
}
