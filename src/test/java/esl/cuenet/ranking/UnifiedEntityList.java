package esl.cuenet.ranking;

import com.mongodb.BasicDBObject;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.ranking.network.NeoEntityBase;
import esl.cuenet.source.accessors.Utils;
import esl.system.SysLoggerUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.Traversal;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UnifiedEntityList {

    private static Logger logger = Logger.getLogger(UnifiedEntityList.class);

    static String directory = "/data/graph_db/email";

    @BeforeClass
    public static void setup() {
        SysLoggerUtils.initLogger();

        logger.info("Deleting Files in " + directory);

        File[] files = (new File(directory)).listFiles();
        if (files == null) files = new File[]{};
        for (File file: files) FileUtils.deleteQuietly(file);
    }

    @Test
    public void random() {
        File storeTempDir = null;

        try {
            storeTempDir = new File(FileUtils.getTempDirectoryPath() + File.separator + System.currentTimeMillis());
            logger.info("Creating temp graph db at: " + storeTempDir.getAbsolutePath());
            FileUtils.forceMkdir(storeTempDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        GraphDatabaseService g1 = new EmbeddedGraphDatabase(storeTempDir.getAbsolutePath());
        new NeoEntityBase(g1);

        g1.shutdown();

        try {
            FileUtils.deleteDirectory(storeTempDir);
            logger.info("Deleted temp db: " + storeTempDir.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void constructEntities() {
        GraphDatabaseService g = new EmbeddedGraphDatabase(directory);
        NeoEntityBase entityBase = new NeoEntityBase(g);

        Transaction tx = g.beginTx();
        try {
            entityBase.construct();
            tx.success();
        } catch (Exception e) {
            e.printStackTrace();
            tx.failure();
        } finally {
            tx.finish();
            g.shutdown();
        }

        g = new EmbeddedGraphDatabase(directory);
        Index<Node> xIndex = g.index().forNodes(NeoEntityBase.ENT_GRAPH_LITERAL_INDEX);
        IndexHits<Node> hits = xIndex.get(EntityBase.TEXT, "arjun.satish@gmail.com");
        Node hit = hits.getSingle();

        entityBase = new NeoEntityBase(g);
        Node entity = hit.getRelationships(Direction.INCOMING).iterator().next().getStartNode();
        for (Relationship rel: entity.getRelationships(Direction.OUTGOING)) {
            logger.info(" (" + rel.getProperty(EntityBase.TYPE) + ")  "
                    + rel.getEndNode().getProperty(EntityBase.TEXT));
        }

        URINode node = entityBase.lookup(EntityBase.V_EMAIL, "arjunmv@gmail.com");
        for (TypedEdge edge: node.getAllRelationships()) {
            logger.info(" >>> " + edge.getProperty(EntityBase.TYPE) + " <<< " +
                    edge.getEndNode().getProperty(EntityBase.TEXT));
        }

        g.shutdown();
    }
}
