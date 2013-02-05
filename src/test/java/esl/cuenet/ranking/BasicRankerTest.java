package esl.cuenet.ranking;

import esl.cuenet.ranking.network.NeoEntityBase;
import esl.cuenet.ranking.network.PersistentEventEntityNetwork;
import esl.cuenet.ranking.rankers.BasicRanker;
import esl.cuenet.ranking.rankers.EventEntityPropagationFunction;
import esl.cuenet.ranking.rankers.EventEventPropagationFunction;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BasicRankerTest {

    private Logger logger = Logger.getLogger(BasicRankerTest.class);
    private String directory = "/data/graph_db/sources";
    private static GraphDatabaseService graphDb = null;

    @BeforeClass
    public static void setup() {
        SysLoggerUtils.initLogger();
    }

    @AfterClass
    public static void shutdown() {
        if (graphDb != null) graphDb.shutdown();   //shuting down graph -- in case a test case "forgets" to do so.
    }

    @Test
    public void doInitializationTest() {
        logger.info("Initialization Test");

        graphDb = new EmbeddedGraphDatabase(directory);
        EventEntityNetwork network = new PersistentEventEntityNetwork(graphDb);
        EntityBase entityBase = new NeoEntityBase(graphDb);

        Ranker ranker = new BasicRanker(network, entityBase);
        ranker.assign(entityBase.lookup(EntityBase.V_NAME, "Arjun Satish").getId(), 1.0);
        ranker.assign(entityBase.lookup(EntityBase.V_FB_ID, "6028816").getId(), 1.0);
        ranker.assign(entityBase.lookup(EntityBase.V_EMAIL, "gupta@sdsc.edu").getId(), 1.0);

        List<PropagationFunction> functions = new ArrayList<PropagationFunction>(2);
        functions.add(new EventEventPropagationFunction());
        functions.add(new EventEntityPropagationFunction());

        ranker.compute(functions);

        logger.info("Shutting down DB");
        graphDb.shutdown();
        graphDb = null;
    }

}
