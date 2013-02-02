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
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.io.IOException;
import java.util.HashSet;

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
        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );

        Iterable<Node> nodes = graphDb.getAllNodes();
        HashSet<Long> idSet = new HashSet<Long>(100000);
        int i = 0;
        for (Node n: nodes) {
            ++i;
            for (Relationship r: n.getRelationships()) idSet.add(r.getId());
        }

        logger.info(i + " Nodes");
        logger.info(idSet.size() + " Relationships");

        graphDb.shutdown();
    }



}
