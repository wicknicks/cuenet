package esl.cuenet.algorithms.firstk.structs.eventgraph;

import esl.cuenet.algorithms.BaseAlgorithm;
import esl.cuenet.mapper.parser.ParseException;
import esl.datastructures.graph.*;
import org.apache.log4j.Logger;
import org.junit.Test;
import test.TestBase;

import java.io.FileNotFoundException;
import java.util.List;

public class EventGraphTest extends TestBase {

    private Logger logger = Logger.getLogger(EventGraphTest.class);

    @Test
    public void simpleEventGraphTest() throws FileNotFoundException, ParseException, EventGraphException {
        BaseAlgorithm algorithm = new ConcreteAlgorithmClass();

        EventGraph graph = new EventGraph(algorithm.getModel());
        Event conference = graph.createEvent("conference");
        Event session1 = graph.createEvent("session");
        Event session2 = graph.createEvent("session");
        Event lunch = graph.createEvent("lunch");
        Entity person1 = graph.createPerson();
        Entity person2 = graph.createPerson();

        graph.addSubevent(conference, session1);
        graph.addSubevent(conference, session2);
        graph.addSubevent(conference, lunch);
        graph.addParticipant(conference, person1);
        graph.addParticipant(session2, person2);

        TraversalContext context = new TraversalContext();
        context.setCx("");


        logger.info(" === Traversing with complete graph === ");

        BFSEventGraphTraverser traverser = new BFSEventGraphTraverser(graph);

        traverser.setNodeVisitorCallback(new NodeVisitor() {
            @Override
            public void visit(Node node, TraversalContext traversalContext) {
                logger.info(node.name());
            }
        });

        traverser.setEdgeVisitorCallback(new EdgeVisitor() {
            @Override
            public void visit(Edge edge, TraversalContext traversalContext) {
                logger.info(edge.label());
            }
        });

        traverser.start();

        logger.info(" === Removing person1 === ");

        graph.removeEntity(person1);
        traverser.start();

        logger.info(" === Adding person1 to lunch === ");

        person1 = (Entity) graph.addIndividual(person1.getIndividual(), EventGraph.NodeType.ENTITY);
        graph.addParticipant(lunch, person1);
        traverser.start();

        logger.info(" === Removing conference === ");

        graph.removeEvent(conference);
        traverser.start();

    }

    @Test
    public void findParticipantsSubevents() throws FileNotFoundException, ParseException, EventGraphException {
        BaseAlgorithm algorithm = new ConcreteAlgorithmClass();

        EventGraph graph = new EventGraph(algorithm.getModel());
        Event conference = graph.createEvent("conference");
        Event session1 = graph.createEvent("session");
        Event session2 = graph.createEvent("session");
        Event lunch = graph.createEvent("lunch");
        Entity person1 = graph.createPerson();
        Entity person2 = graph.createPerson();

        graph.addSubevent(conference, session1);
        graph.addSubevent(conference, session2);
        graph.addSubevent(conference, lunch);
        graph.addParticipant(session2, person1);
        graph.addParticipant(session2, person2);

        List<Event> subevents = graph.getSubevents (conference);
        for (Event e: subevents) logger.info("Subevent: " + e.name());

        List<Entity> entities = graph.getParticipants(session2);
        for (Entity e: entities) logger.info("Participant: " + e.name());
    }

    @Test
    public void dropEdgeTest() throws FileNotFoundException, ParseException, EventGraphException {
        BaseAlgorithm algorithm = new ConcreteAlgorithmClass();

        EventGraph graph = new EventGraph(algorithm.getModel());
        Event conference = graph.createEvent("conference");
        Event session1 = graph.createEvent("session");
        Event session2 = graph.createEvent("session");
        Event lunch = graph.createEvent("lunch");
        Entity person1 = graph.createPerson();
        Entity person2 = graph.createPerson();

        graph.addSubevent(conference, session1);
        graph.addSubevent(conference, session2);
        graph.addSubevent(conference, lunch);
        graph.addParticipant(conference, person1);
        graph.addParticipant(session2, person2);

        TraversalContext context = new TraversalContext();
        context.setCx("");

        graph.dropParticipantEdge(conference, person1);
        logger.info(" -- Dropped participant person1 -- ");

        graph.dropSubeventEdge(conference, lunch);
        logger.info(" -- Dropped subevent edge between conference and lunch -- ");


        BFSEventGraphTraverser traverser = new BFSEventGraphTraverser(graph);

        traverser.setNodeVisitorCallback(new NodeVisitor() {
            @Override
            public void visit(Node node, TraversalContext traversalContext) {
                logger.info(node.name());
            }
        });

        traverser.setEdgeVisitorCallback(new EdgeVisitor() {
            @Override
            public void visit(Edge edge, TraversalContext traversalContext) {
                logger.info(edge.label());
            }
        });

        traverser.start();

    }

    private class ConcreteAlgorithmClass extends BaseAlgorithm {

        public ConcreteAlgorithmClass() throws FileNotFoundException, ParseException {
            super();
        }
    }

}
