package esl.cuenet.ranking;

import esl.cuenet.ranking.network.PersistentEventEntityNetwork;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class IndexConstructionTests {

    private String directory = "/data/graph_db/sources";
    private Logger logger = Logger.getLogger(IndexConstructionTests.class);

    @BeforeClass
    public static void setup() {
        SysLoggerUtils.initLogger();
    }

    @Test
    public void constructSpatioTemporalIxTest() {
        logger.info("Initiating constructSpatioTemporalIxTest");

        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );
        logger.info("Loaded Graph");

        EventEntityNetwork network = new PersistentEventEntityNetwork( graphDb );

        try {
            network.stIndex(SpatioTemporalIndex.OCCURS_DURING_IX).construct();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            graphDb.shutdown();
        }
    }

    @Test
    public void queryTemporalIndex() {
        logger.info("Initiating queryTemporalIndex");

        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );
        logger.info("Loaded Graph");

        EventEntityNetwork network = new PersistentEventEntityNetwork( graphDb );

        try {
            SpatioTemporalIndex txIndex = network.stIndex(SpatioTemporalIndex.OCCURS_DURING_IX);
            txIndex.overlaps(1327202830000L, 1327202840000L);
            txIndex.previous(1327202838000L);
            txIndex.next(1327202838000L);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            graphDb.shutdown();
        }
    }
}
