package esl.cuenet.ranking;

import esl.cuenet.ranking.network.NeoEntityBase;
import esl.cuenet.ranking.network.PersistentEventEntityNetwork;
import esl.cuenet.ranking.rankers.*;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;

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
                                                   //       ....  will happen when uncaught exceptions are thrown.
    }

    @Test
    public void doInitializationTest() {
        logger.info("Basic Ranker Test");

        graphDb = new EmbeddedGraphDatabase(directory);
        EventEntityNetwork network = new PersistentEventEntityNetwork(graphDb);
        EntityBase entityBase = new NeoEntityBase(graphDb);

        Ranker ranker = new BasicRanker(network, entityBase);
        //ranker.assign(entityBase.lookup(EntityBase.V_NAME, "Arjun Satish").getId(), 1.0);

        //Work
//        ranker.assign(entityBase.lookup(EntityBase.V_FB_ID, "6028816").getId(), 1.0);
//        ranker.assign(entityBase.lookup(EntityBase.V_EMAIL, "gupta@sdsc.edu").getId(), 1.0);

        //Friends
        ranker.assign(entityBase.lookup(EntityBase.V_NAME, "Nicolas Mangano").getId(), 1.0);
        ranker.assign(entityBase.lookup(EntityBase.V_EMAIL, "alexander.behm@gmail.com").getId(), 1.0);

        PropagationFunction[] functions = new PropagationFunction[]{
                new SubEventPropagationFunction(),
                new EntityEventPropagationFunction(),
                new EventEntityPropagationFunction(),
                new TemporalPropagationFunction()
        };

        while( !ranker.canTerminate() ) {
            ranker.compute(functions);
            //logger.info(((EventEntityPropagationFunction) functions[2]).count());
        }

        ranker.results();

        logger.info("Shutting down DB");
        graphDb.shutdown();
        graphDb = null;
    }

}
