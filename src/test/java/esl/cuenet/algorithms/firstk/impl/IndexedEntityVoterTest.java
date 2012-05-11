package esl.cuenet.algorithms.firstk.impl;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.mongodb.BasicDBObject;
import esl.cuenet.algorithms.firstk.Vote;
import esl.cuenet.algorithms.firstk.exceptions.EventGraphException;
import esl.cuenet.algorithms.firstk.structs.eventgraph.Entity;
import esl.cuenet.algorithms.firstk.structs.eventgraph.EventGraph;
import esl.cuenet.mapper.parser.ParseException;
import esl.cuenet.model.Constants;
import esl.cuenet.query.QueryEngine;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import test.TestBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IndexedEntityVoterTest extends MongoDB {

    private Logger logger = Logger.getLogger(IndexedEntityVoterTest.class);
    private TestBase.TestAlgorithm testAlgorithm = null;

    public IndexedEntityVoterTest() throws IOException, ParseException {
        super("test");
        SysLoggerUtils.initLogger();
        testAlgorithm = new TestBase.TestAlgorithm();
    }

    @Test
    public void doTest() throws IOException, ParseException, EventGraphException {

        /* for profiling */
//        logger.info("Started!");
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        long start = System.currentTimeMillis();
        logger.info("Starting Second Test: " + start);

        OntModel model = testAlgorithm.getModel();
        List<Individual> eventAttendees = new ArrayList<Individual>(500);
        OntClass personClass = model.getOntClass(Constants.CuenetNamespace + "person");
        Property nameProperty = model.getProperty(Constants.CuenetNamespace + "name");
        Property emailProperty = model.getProperty(Constants.CuenetNamespace + "email");

        DBReader cursor = startReader("conf_attendees");
        BasicDBObject queryObject = new BasicDBObject("url", "http://vldb2009.org/");
        cursor.query(queryObject);
        while(cursor.hasNext()) {
            Individual attendee = personClass.createIndividual();
            BasicDBObject obj = (BasicDBObject) cursor.next();
            String name = obj.getString("name");
            attendee.addLiteral(nameProperty, name);
            eventAttendees.add(attendee);
        }

        logger.info("Event Attendees: " + eventAttendees.size());

        HashIndexedEntityVoter indexedVoter = new HashIndexedEntityVoter(new QueryEngine(testAlgorithm.getModel(),
                testAlgorithm.getSourceMapper()), testAlgorithm.getModel());

        EventGraph graph = new EventGraph(model);
        Entity entity = graph.createPerson();
        entity.getIndividual().addProperty(nameProperty, "Arjun Satish");
        entity.getIndividual().addProperty(emailProperty, "arjun.satish@gmail.com");
        indexedVoter.addToVerifiedPile(entity.getIndividual());

        Vote[] votes = indexedVoter.vote(graph, eventAttendees);
        for (Vote vote : votes) logger.info(vote.entityID + "  " + vote.score);

        Entity e1 = graph.createPerson();
        e1.getIndividual().addProperty(nameProperty, "Atish Das Sarma");
        e1.getIndividual().addProperty(emailProperty, "atish.dassarma@gmail.com");
        indexedVoter.addToVerifiedPile(e1.getIndividual());

        e1 = graph.createPerson();
        e1.getIndividual().addProperty(nameProperty, "Danupon Nanongkai");
        e1.getIndividual().addProperty(emailProperty, "danupon@gmail.com");
        indexedVoter.addToVerifiedPile(e1.getIndividual());

        e1 = new Entity(personClass.createIndividual());
        e1.getIndividual().addProperty(nameProperty, "Chen Li");
        indexedVoter.addToVerifiedPile(e1.getIndividual());

        e1 = graph.createPerson();
        e1.getIndividual().addProperty(nameProperty, "Ramesh Jain");
        indexedVoter.addToVerifiedPile(e1.getIndividual());

        e1 = graph.createPerson();
        e1.getIndividual().addProperty(nameProperty, "Galen Reeves");
        indexedVoter.addToVerifiedPile(e1.getIndividual());

        e1 = new Entity(personClass.createIndividual());
        e1.getIndividual().addProperty(nameProperty, "Nicola Onose");
        indexedVoter.addToVerifiedPile(e1.getIndividual());

        votes = indexedVoter.vote(graph, eventAttendees);
        for (Vote vote : votes) logger.info(vote.entityID + "  " + vote.score);

        long end = System.currentTimeMillis();
        logger.info("Terminated in " + (end-start) + " ms.");
    }


    @Test
    public void testOnSecondPhoto() throws EventGraphException, IOException, ParseException {

        long start = System.currentTimeMillis();
        logger.info("Starting Second Test: " + start);

        OntModel model = testAlgorithm.getModel();
        List<Individual> eventAttendees = new ArrayList<Individual>(500);
        OntClass personClass = model.getOntClass(Constants.CuenetNamespace + "person");
        Property nameProperty = model.getProperty(Constants.CuenetNamespace + "name");
        Property emailProperty = model.getProperty(Constants.CuenetNamespace + "email");

        DBReader cursor = startReader("conf_attendees");
        BasicDBObject queryObject = new BasicDBObject("url", "http://vldb2009.org/");
        cursor.query(queryObject);
        while(cursor.hasNext()) {
            Individual attendee = personClass.createIndividual();
            BasicDBObject obj = (BasicDBObject) cursor.next();
            String name = obj.getString("name");
            attendee.addLiteral(nameProperty, name);
            eventAttendees.add(attendee);
        }

        logger.info("Event Attendees: " + eventAttendees.size());

        HashIndexedEntityVoter indexedVoter = new HashIndexedEntityVoter(new QueryEngine(testAlgorithm.getModel(),
                testAlgorithm.getSourceMapper()), testAlgorithm.getModel());

        EventGraph graph = new EventGraph(model);
        Entity entity = graph.createPerson();
        entity.getIndividual().addProperty(nameProperty, "Arjun Satish");
        entity.getIndividual().addProperty(emailProperty, "arjun.satish@gmail.com");
        indexedVoter.addToVerifiedPile(entity.getIndividual());

        Vote[] votes = indexedVoter.vote(graph, eventAttendees);
        for (Vote vote : votes) logger.info(vote.entityID + "  " + vote.score);

        long end = System.currentTimeMillis();
        logger.info("Terminated in " + (end-start) + " ms.");
    }
}
