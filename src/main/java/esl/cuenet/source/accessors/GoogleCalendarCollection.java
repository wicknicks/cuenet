package esl.cuenet.source.accessors;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.RDF;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.Hash;
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
import esl.datastructures.TimeInterval;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class GoogleCalendarCollection extends MongoDB implements IAccessor {

    private Logger logger = Logger.getLogger(GoogleCalendarCollection.class);
    private Attribute[] attributes = null;
    private boolean[] setFlags = new boolean[6];
    private String ownerEmail = null;
    private long startTime = -1;
    private long endTime = -1;
    private int errorMargin = 5;
    private OntModel model = null;

    public GoogleCalendarCollection() {
        super("test");
    }

    public GoogleCalendarCollection(OntModel model) {
        this();
        this.model = model;
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

        return convertResults(result);
    }

    private IResultSet convertResults(BasicDBList result) {
        ResultSetImpl resultSet = new ResultSetImpl("Google Calendar Results");

        OntClass event = null;
        OntClass person = model.getOntClass(Constants.CuenetNamespace + "person");
        DatatypeProperty nameProperty = model.getDatatypeProperty(
                Constants.CuenetNamespace + "name");
        DatatypeProperty emailProperty = model.getDatatypeProperty(
                Constants.CuenetNamespace + "email");
        DatatypeProperty titleProperty = model.getDatatypeProperty(
                Constants.CuenetNamespace + "title");
        ObjectProperty participatesInProperty = model.getObjectProperty(
                Constants.DOLCE_Lite_Namespace + Constants.ParticipantIn);
        ObjectProperty occursDuring = model.getObjectProperty(
                Constants.CuenetNamespace + "occurs-during");

        for (Object o: result) {
            BasicDBObject entry = (BasicDBObject) o;

            if (entry.containsField("title")) {
                event = getOntologyClass(entry.getString("title"));
            } else {
                event = model.getOntClass("http://www.w3.org/1999/02/22-rdf-syntax-ns#event");
            }

            Individual ev = event.createIndividual();
            Individual owner = person.createIndividual();

            if (entry.containsField("name")) owner.addLiteral(nameProperty, entry.getString("name"));
            if (entry.containsField("email")) owner.addLiteral(emailProperty, entry.getString("email"));
            if (entry.containsField("title")) owner.addLiteral(titleProperty, entry.getString("title"));
            if (entry.containsField("start-time") && entry.containsField("end-time")) {
                TimeInterval interval = TimeInterval.createFromInterval(entry.getLong("start-time"),
                        entry.getLong("start-time"), (EnhGraph) model);
                ev.addProperty(occursDuring, interval);
            }

            owner.addProperty(participatesInProperty, ev);

            List<Individual> resultEntry = new ArrayList<Individual>();
            resultEntry.add(ev);
            resultEntry.add(owner);
            resultSet.addResult(resultEntry);

        }

        return resultSet;
    }

    private OntClass getOntologyClass(String title) {
        if (Pattern.compile(Pattern.quote("meet"), Pattern.CASE_INSENSITIVE).matcher(title).find())
            return model.getOntClass("http://www.semanticweb.org/arjun/cuenet-main.owl#meeting");
        else if (Pattern.compile(Pattern.quote("talk"), Pattern.CASE_INSENSITIVE).matcher(title).find())
            return model.getOntClass("http://www.semanticweb.org/arjun/cuenet-main.owl#talk");
        return model.getOntClass(Constants.DOLCE_Lite_Namespace + "event");
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

