package esl.cuenet.source.accessors;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConferenceAttendeeAccessor extends MongoDB implements IAccessor {

    private OntModel model = null;
    private Attribute[] attributes = null;
    private boolean[] setFlags = new boolean[6];
    private String ltitle, stitle, url;
    private List<String> personNames = new ArrayList<String>();
    private TimeInterval interval = null;
    private Location location = null;


    private Logger logger = Logger.getLogger(ConferenceAttendeeAccessor.class);

    public ConferenceAttendeeAccessor(OntModel model) {
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
        url = null;
        personNames.clear();
    }

    @Override
    public void associateTimeInterval(Attribute attribute, TimeInterval timeInterval) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[2])==0) {
            this.interval = timeInterval;
            setFlags[2] = true;
        } else {
            throw new AccesorInitializationException("TimeInterval value being initialized for wrong attribute "
                    + ConferenceAccessor.class.getName());
        }
    }

    @Override
    public void associateLocation(Attribute attribute, Location location) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[3])==0) {
            this.location = location;
            setFlags[3] = true;
        } else {
            throw new AccesorInitializationException("Location value being initialized for wrong attribute "
                    + ConferenceAccessor.class.getName());
        }
    }
    @Override
    public void associateLong(Attribute attribute, long value) throws AccesorInitializationException {
        throw new AccesorInitializationException("Incorrect Assignment: No long attributes in "
                + ConferenceAttendeeAccessor.class.getName());
    }

    @Override
    public void associateString(Attribute attribute, String value) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[0])==0) {
            this.url = value;
            setFlags[0] = true;
        }
        else if (attribute.compareTo(attributes[1])==0) {
            this.personNames.add(value);
            setFlags[1] = true;
        }
        else if (attribute.compareTo(attributes[4])==0) {
            setFlags[4] = true;
            this.ltitle = value;
        }
        else if (attribute.compareTo(attributes[5])==0) {
            setFlags[5] = true;
            this.stitle = value;
        }
        else if (attribute.compareTo(attributes[4])==0) {
            setFlags[4] = true;
            this.url = value;
        }
        else throw new AccesorInitializationException("Incorrect Assignment: String attributes in "
                + ConferenceAttendeeAccessor.class.getName());
    }

    @Override
    public void associateDouble(Attribute attribute, double value) throws AccesorInitializationException {
        throw new AccesorInitializationException("Incorrect Assignment: No double attributes in "
                + ConferenceAttendeeAccessor.class.getName());
    }

    @Override
    public IResultSet executeQuery() throws SourceQueryException {
        return query();
    }

    private IResultSet query() {
        BasicDBObject query = new BasicDBObject();

        if (url != null) query.put("url", url);
        BasicDBList namePredicates = new BasicDBList();
        for (String name: personNames) {
            namePredicates.add(new BasicDBObject("name", name));
        }
        if (namePredicates.size() > 0) query.put("$or", namePredicates);

        DBReader reader = startReader("conf_attendees");
        reader.query(query);

        OntClass personClass = model.getOntClass(Constants.CuenetNamespace + "person");
        Property nameProperty = model.getProperty(Constants.CuenetNamespace + "name");
        HashMap<String, List<String>> urlNameMap = new HashMap<String, List<String>>();

        while(reader.hasNext()) {
            BasicDBObject attendeeObject = (BasicDBObject) reader.next();
            String url = attendeeObject.getString("url");
            String name = attendeeObject.getString("name");
            if (name == null) continue;

            if (!urlNameMap.containsKey(url)) urlNameMap.put(url, new ArrayList<String>());
            urlNameMap.get(url).add(name);

        }

        ResultSetImpl resultSet = new ResultSetImpl("Conference Attendee Results", model);

        for (Map.Entry<String, List<String>> entry: urlNameMap.entrySet()) {
            Individual confIndividual = getConference(entry.getKey());

            if (confIndividual == null) continue;
            if (entry.getValue().size() == 0) continue;

            List<Individual> result = new ArrayList<Individual>();
            result.add(confIndividual);

            for (String attendeeName: entry.getValue()) {
                Individual personIndividual = personClass.createIndividual(personClass.getURI()+attendeeName.replaceAll(" ", "_"));
                //Individual personIndividual = model.createIndividual(personClass);
                personIndividual.addProperty(nameProperty, attendeeName);
                result.add(personIndividual);
                logger.info(entry.getKey() + " attended-by " + attendeeName);
            }

            resultSet.addResult(result);
        }

        return resultSet;
    }

    private Individual getConference(String url) {
        OntClass conferenceClass = model.getOntClass(Constants.CuenetNamespace + "conference");
        Property titleProperty = model.getProperty(Constants.CuenetNamespace + "title");
        Property urlProperty = model.getProperty(Constants.CuenetNamespace + "url");
        Property nameProperty = model.getProperty(Constants.CuenetNamespace + "name");
        Property occursAt = model.getProperty(Constants.CuenetNamespace + "occurs-at");
        Property occursDuring = model.getProperty(Constants.CuenetNamespace + "occurs-during");

        BasicDBObject query = new BasicDBObject("url", url);
        DBReader reader = startReader("conferences");
        reader.query(query);
        DBObject result = null;
        while(reader.hasNext()) result = reader.next();

        if (result == null) throw new RuntimeException("Conference not found: " + url);

        BasicDBObject conf = (BasicDBObject) result;

        Location confLocation = null;
        if (conf.containsField("location")) confLocation = convertToLocation(conf.getString("location"));

        if (stitle != null && stitle.compareTo(conf.getString("short"))!=0) return null;
        if (ltitle != null && ltitle.compareTo(conf.getString("title"))!=0) return null;


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

        confIndividual.addProperty(occursAt, confLocation);
        confIndividual.addProperty(occursDuring, confTimeInterval);

        return confIndividual;
    }

    private Location convertToLocation(String location) {
        try {
            return Location.createFromAddress(location, model);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void search(String url, String name) {
        if (url != null) this.url = url;
        if (name != null) this.personNames.add(name);
        query();
    }
}
