package esl.cuenet.ranking;

import esl.cuenet.ranking.network.NeoEntityBase;
import esl.cuenet.ranking.network.NeoOntoInstanceImporter;
import esl.cuenet.ranking.network.PersistentEventEntityNetwork;
import esl.cuenet.ranking.sources.EmailSource;
import esl.cuenet.ranking.sources.FacebookPhotoSource;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.io.IOException;
import java.util.Map;

public class ConstructEEN {

    private Logger logger = Logger.getLogger(SourceImportTest.class);
    private String directory = "/data/graph_db/sources";

    @BeforeClass
    public static void setUp() {
        SysLoggerUtils.initLogger();
    }

    @Test
    public void sourceInstantiationTest() throws IOException {
        logger.info("Starting Test");

        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );
        EventEntityNetwork network = new PersistentEventEntityNetwork( graphDb );

        NeoEntityBase entityBase = new NeoEntityBase(graphDb);

        Transaction tx = graphDb.beginTx();
        try {
            entityBase.construct();
            tx.success();
        } catch (Exception e) {
            e.printStackTrace();
            tx.failure();
        } finally {
            tx.finish();
        }

        NeoOntoInstanceImporter importer = new NeoOntoInstanceImporter(network, new SourceInstantiator[]{
                new EmailSource(), new FacebookPhotoSource()
        });

        try {
            importer.populate(entityBase);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            graphDb.shutdown();
        }
    }

    @Test
    public void countNodesAndEdgesInGraph() {
        logger.info("Counting Nodes and Edges in: " + directory);

        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );

        String query = "START n=node(*) RETURN COUNT(n)";
        ExecutionEngine engine = new ExecutionEngine( graphDb );
        ExecutionResult results = engine.execute(query);

        for (Map<String, Object> result: results) {
            for ( Map.Entry<String, Object> column : result.entrySet() ) {
                logger.info(column.getKey() + " " + column.getValue());
            }
        }

        query = "START r=rel(*) RETURN COUNT(r)";
        engine = new ExecutionEngine( graphDb );
        results = engine.execute(query);

        for (Map<String, Object> result: results) {
            for ( Map.Entry<String, Object> column : result.entrySet() ) {
                logger.info(column.getKey() + " " + column.getValue());
            }
        }

        graphDb.shutdown();
    }



}
