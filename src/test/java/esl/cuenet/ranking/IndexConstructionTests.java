package esl.cuenet.ranking;

import esl.cuenet.model.Constants;
import esl.cuenet.ranking.network.OntProperties;
import esl.cuenet.ranking.network.PersistentEventEntityNetwork;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.util.HashSet;
import java.util.Map;

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

    @Test
    public void indexEventsAndEntities() {

        /* Index Enitities */
        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );
        logger.info("Loaded Graph");

        String query = "START n=node(*) WHERE has(n." + EntityBase.TYPE + ") AND n." + EntityBase.TYPE + "= '" + EntityBase.ENTITY + "' RETURN n";

        ExecutionEngine engine = new ExecutionEngine( graphDb );
        ExecutionResult results = engine.execute(query);

        int ix = 0;
        Index<Node> nodeIndex = graphDb.index().forNodes(EntityBase.ENTITY_INDEX);
        Transaction tx = graphDb.beginTx();

        try {
            nodeIndex.delete();
            tx.success();
            tx.finish();

            nodeIndex = graphDb.index().forNodes(EntityBase.ENTITY_INDEX);
            tx = graphDb.beginTx();
            for (Map<String, Object> result: results)
                for ( Map.Entry<String, Object> column : result.entrySet() ) {
                    Node n = (Node) column.getValue();
                    nodeIndex.add(n, EntityBase.TYPE, EntityBase.ENTITY);
                    ix++;
                }
            tx.success();
            logger.info("Indexed " + ix + " entities.");
        } catch (Exception e) {
            tx.failure();
            e.printStackTrace();
        } finally {
            tx.finish();
            graphDb.shutdown();
        }

        graphDb = new EmbeddedGraphDatabase( directory );
        logger.info("ReLoaded Graph");

        nodeIndex = graphDb.index().forNodes(EntityBase.ENTITY_INDEX);
        IndexHits<Node> hits = nodeIndex.get(EntityBase.TYPE, EntityBase.ENTITY);
        logger.info("Entities in graph " + hits.size());

        graphDb.shutdown();

        /* Index Events */
        graphDb = new EmbeddedGraphDatabase( directory );
        logger.info("Loaded Graph for Indexing Events");
        query = "START n=node(*) WHERE has(n." + OntProperties.TYPE + ") AND n." + OntProperties.TYPE + "= '" + OntProperties.INSTANCE + "' RETURN n";
        engine = new ExecutionEngine( graphDb );
        results = engine.execute(query);

        ix = 0;
        nodeIndex = graphDb.index().forNodes(EventEntityNetwork.EVENT_INDEX);
        tx = graphDb.beginTx();

        try {
            nodeIndex.delete();
            tx.success();
            tx.finish();

            nodeIndex = graphDb.index().forNodes(EventEntityNetwork.EVENT_INDEX);
            tx = graphDb.beginTx();
            for (Map<String, Object> result: results)
                for ( Map.Entry<String, Object> column : result.entrySet() ) {
                    Node n = (Node) column.getValue();
                    if ( !n.hasProperty(OntProperties.ONT_URI) ) {
                        logger.info("WEIRD " + n);
                        continue;
                    }
                    String uri = (String) n.getProperty(OntProperties.ONT_URI);
                    if (uri.contains(Constants.CuenetNamespace + Constants.EmailExchangeEvent) ||
                            uri.contains(Constants.CuenetNamespace + Constants.PhotoCaptureEvent)) {
                        nodeIndex.add(n, EntityBase.TYPE, EventEntityNetwork.EVENT);
                        ix++;
                    }
                }
            tx.success();
            logger.info("Indexed " + ix + " events.");
        } catch (Exception e) {
            tx.failure();
            e.printStackTrace();
        } finally {
            tx.finish();
            graphDb.shutdown();
        }

        graphDb = new EmbeddedGraphDatabase( directory );
        logger.info("ReLoaded Graph");
        nodeIndex = graphDb.index().forNodes(EventEntityNetwork.EVENT_INDEX);
        hits = nodeIndex.get(EntityBase.TYPE, EventEntityNetwork.EVENT);
        logger.info("Entities in graph " + hits.size());

        graphDb.shutdown();
    }
}
