package esl.cuenet.source.accessors;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import esl.cuenet.model.Constants;
import esl.cuenet.query.IResultIterator;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.ResultIterator;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.IAccessor;
import esl.datastructures.Location;
import esl.datastructures.TimeInterval;
import esl.system.JsonUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class ConferenceSubEventAccessor extends MongoDB implements IAccessor {

    OntModel model = null;
    private Attribute[] attributes;
    private boolean[] setFlags = new boolean[1];
    private String url = null;

    private Property titleProperty = null;
    private OntClass personClass = null;
    private Property occursAt = null;
    private Property occursDuring = null;
    private Property nameProperty = null;

    private SimpleDateFormat sdformatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");

    private Logger logger = Logger.getLogger(ConferenceSubEventAccessor.class);

    public ConferenceSubEventAccessor(OntModel model) {
        super(AccessorConstants.DBNAME);
        this.model = model;
        titleProperty = model.getProperty(Constants.CuenetNamespace + "title");
        personClass = model.getOntClass(Constants.CuenetNamespace + "person");
        occursAt = model.getProperty(Constants.CuenetNamespace + "occurs-at");
        occursDuring = model.getProperty(Constants.CuenetNamespace + "occurs-during");
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
    }

    @Override
    public void associateString(Attribute attribute, String value) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[0])==0) {
            this.url = value;
            setFlags[0] = true;
        }
        else throw new AccesorInitializationException("Incorrect Assignment: String attributes in "
                + ConferenceSubEventAccessor.class.getName());
    }

    public BasicDBList query(String collection) {
        BasicDBObject query = new BasicDBObject();
        if (url != null) query.put("link", url);

        DBReader reader = startReader(collection);
        reader.query(query);

        BasicDBList results = new BasicDBList();
        while(reader.hasNext()) {
            BasicDBObject o = (BasicDBObject) reader.next();
            results.add(o);
        }

        return results;
    }

    public ResultSetImpl convert(BasicDBList results, OntClass subeventClass) throws IOException {
        ResultSetImpl resultSet = new ResultSetImpl("Results from: " + subeventClass.getURI());

        logger.info("Converting " + results.size() + " records.");

        for (Object o: results) {
            BasicDBObject obj = (BasicDBObject) o;

            logger.info(obj);

            String title = null;
            if (obj.containsField("title")) title = obj.getString("title");

            Individual event;
            if (title != null) event = subeventClass.createIndividual(subeventClass.getURI() + "_" + title.replaceAll(" ", "_"));
            else event = subeventClass.createIndividual();

            TimeInterval interval = null;
            if (obj.containsField("interval")) {
                long _start = 0, _end = 0;
                try {
                    _start = sdformatter.parse(JsonUtils.unnest(obj, "interval.start", String.class)).getTime();
                    _end = sdformatter.parse(JsonUtils.unnest(obj, "interval.end", String.class)).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                interval = TimeInterval.createFromInterval(_start, _end, model);
            }

            Location location = null;
            if (JsonUtils.contains(obj, "location.address")) {
                location = Location.createFromAddress(JsonUtils.unnest(obj, "location.address", String.class), model);
            } else if (JsonUtils.contains(obj, "location.geo")) {
                location = Location.createFromGPS(JsonUtils.unnest(obj, "location.geo.lat", Double.class), JsonUtils.unnest(obj, "location.geo.lon", Double.class), model);
            }

            List<Individual> participants = new ArrayList<Individual>();
            if (obj.containsField("participants")) {
                BasicDBList parts = (BasicDBList) obj.get("participants");
                for (Object p: parts) {
                    Individual participant = personClass.createIndividual(personClass.getURI() + "_" + ((String)p).replaceAll(" ", "_"));
                    participant.addProperty(nameProperty, p.toString());
                    participants.add(participant);
                }
            }

            if (location != null) event.addProperty(occursAt, location);
            if (interval != null) event.addProperty(occursDuring, interval);
            if (title != null) event.addProperty(titleProperty, title);

            List<Individual> resultEntry = new ArrayList<Individual>();
            resultEntry.add(event);
            for (Individual p: participants) resultEntry.add(p);

            resultSet.addResult(resultEntry);
        }

        return resultSet;
    }

    private class ResultSetImpl implements IResultSet {
        private String result;
        private ResultIterator resultIterator = new ResultIterator(model);

        public ResultSetImpl (String result) {this.result = result;}

        public void addResult(List<Individual> individuals) {
            this.resultIterator.add(individuals);
        }

        @Override
        public String printResults() {
            return result;
        }

        @Override
        public IResultIterator iterator() {
            return resultIterator;
        }
    }
}
