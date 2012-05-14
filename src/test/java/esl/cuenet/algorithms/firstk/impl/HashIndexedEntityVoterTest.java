package esl.cuenet.algorithms.firstk.impl;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.mongodb.BasicDBObject;
import esl.cuenet.algorithms.firstk.Vote;
import esl.cuenet.algorithms.firstk.exceptions.EventGraphException;
import esl.cuenet.algorithms.firstk.structs.eventgraph.Entity;
import esl.cuenet.algorithms.firstk.structs.eventgraph.Event;
import esl.cuenet.algorithms.firstk.structs.eventgraph.EventGraph;
import esl.cuenet.algorithms.firstk.structs.eventgraph.EventGraphNode;
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

public class HashIndexedEntityVoterTest extends MongoDB {

    private Logger logger = Logger.getLogger(IndexedEntityVoterTest.class);
    private TestBase.TestAlgorithm testAlgorithm = null;

    public HashIndexedEntityVoterTest() throws IOException, ParseException {
        super("test");
        SysLoggerUtils.initLogger();
        testAlgorithm = new TestBase.TestAlgorithm();
    }

    @Test
    public void run1() throws IOException, ParseException, EventGraphException {
        OntModel model = testAlgorithm.getModel();
        List<Individual> eventAttendees = new ArrayList<Individual>(500);
        OntClass personClass = model.getOntClass(Constants.CuenetNamespace + "person");
        Property nameProperty = model.getProperty(Constants.CuenetNamespace + "name");
        Property emailProperty = model.getProperty(Constants.CuenetNamespace + "email");

        MongoDB.DBReader cursor = startReader("conf_attendees");
        BasicDBObject queryObject = new BasicDBObject("url", "http://vldb2009.org/");
        cursor.query(queryObject);
        while(cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursor.next();
            String name = obj.getString("name");
            Individual attendee = personClass.createIndividual(Constants.CuenetNamespace + "person_"+name.replaceAll(" ", "_"));
            attendee.addLiteral(nameProperty, name);
            eventAttendees.add(attendee);
        }

        logger.info("Event Attendees: " + eventAttendees.size());

        HashIndexedEntityVoter indexedVoter = new HashIndexedEntityVoter(new QueryEngine(testAlgorithm.getModel(),
                testAlgorithm.getSourceMapper()), testAlgorithm.getModel());

        EventGraph graph = new EventGraph(model);

        Event conference = graph.createEvent("conference");
        for (Individual ind: eventAttendees) {
            EventGraphNode node = graph.addIndividual(ind, EventGraph.NodeType.ENTITY);
            graph.addParticipant(conference, (Entity) node);
        }

        Entity entity = graph.createPerson();
        entity.getIndividual().addProperty(nameProperty, "Arjun Satish");
        entity.getIndividual().addProperty(emailProperty, "arjun.satish@gmail.com");
        indexedVoter.addToVerifiedList(entity);

        List<Entity> discoverableEntity = new ArrayList<Entity>();
        discoverableEntity.add(entity);

        long start = System.currentTimeMillis();
        logger.info("Starting First Test: " + start);

        Vote[] votes = indexedVoter.vote(graph, discoverableEntity);
        for (Vote vote : votes) logger.info(vote.entityID + "  " + vote.score);
        if (votes.length == 0)  logger.info("voter returned 0 possible entities!");

        long end = System.currentTimeMillis();
        logger.info("Terminated in " + (end-start) + " ms.");

        Entity e1 = graph.createPerson();
        e1.getIndividual().addProperty(nameProperty, "Atish Das Sarma");
        e1.getIndividual().addProperty(emailProperty, "atish.dassarma@gmail.com");
        indexedVoter.addToVerifiedList(e1);
        discoverableEntity.add(e1);

        e1 = graph.createPerson();
        e1.getIndividual().addProperty(nameProperty, "Danupon Nanongkai");
        e1.getIndividual().addProperty(emailProperty, "danupon@gmail.com");
        indexedVoter.addToVerifiedList(e1);
        discoverableEntity.add(e1);

        e1 = new Entity(personClass.createIndividual());
        e1.getIndividual().addProperty(nameProperty, "Chen Li");
        indexedVoter.addToVerifiedList(e1);

        e1 = graph.createPerson();
        e1.getIndividual().addProperty(nameProperty, "Ramesh Jain");
        indexedVoter.addToVerifiedList(e1);

        e1 = graph.createPerson();
        e1.getIndividual().addProperty(nameProperty, "Galen Reeves");
        indexedVoter.addToVerifiedList(e1);

        e1 = new Entity(personClass.createIndividual());
        e1.getIndividual().addProperty(nameProperty, "Nicola Onose");
        indexedVoter.addToVerifiedList(e1);

        start = System.currentTimeMillis();
        logger.info("Starting Second Pass: " + start);

        votes = indexedVoter.vote(graph, discoverableEntity);
        for (Vote vote : votes) logger.info(vote.entityID + "  " + vote.score);

        end = System.currentTimeMillis();
        logger.info("Terminated in " + (end-start) + " ms.");

    }

}
