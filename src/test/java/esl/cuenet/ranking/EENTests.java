package esl.cuenet.ranking;

import esl.cuenet.ranking.network.PersistentEventEntityNetwork;
import esl.system.SysLoggerUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.io.File;

public class EENTests {

    private static String directory = "/data/graph_db/tests";
    private Logger logger = Logger.getLogger(EENTests.class);

    @BeforeClass
    public static void setUp() {
        SysLoggerUtils.initLogger();
        System.out.println("Deleting Files in " + directory);

        File[] files = (new File(directory)).listFiles();
        if (files == null) files = new File[]{};
        for (File file: files) FileUtils.deleteQuietly(file);
    }

    @AfterClass
    public static void tearDown() {
        File[] files = (new File(directory)).listFiles();
        if (files == null) files = new File[]{};
        for (File file: files) FileUtils.deleteQuietly(file);
    }

    @Test
    public void createSimpleEEN() {
        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );

        EventEntityNetwork network = new PersistentEventEntityNetwork( graphDb );
        URINode n1 = network.createNode();
        n1.setProperty("name", "Agatha");

        URINode n2 = network.createNode();
        n2.setProperty("name", "Poirot");

        URINode n3 = network.createNode();
        n3.setProperty("name", "Marple");

        n1.createEdgeTo(n2);
        n1.createEdgeTo(n3);
        n2.createEdgeTo(n3);
        n3.createEdgeTo(n2);

        logger.info("n1 = " + n1.getProperty("name"));
        logger.info("n2 = " + n2.getProperty("name"));
        logger.info("n3 = " + n3.getProperty("name"));

        for (TypedEdge e: n1.getAllRelationships()) {
            logger.info(e.getStartNode().getProperty("name") + " => " + e.getEndNode().getProperty("name"));
        }

    }

    @Test
    public void createSimpleEENWithIndex() {
        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );

        EventEntityNetwork network = new PersistentEventEntityNetwork( graphDb );
        URINode n1 = network.createNode();
        n1.setProperty("name", "Agatha");

        URINode n2 = network.createNode();
        n2.setProperty("name", "Poirot");

        URINode n3 = network.createNode();
        n3.setProperty("name", "Marple");

        n1.createEdgeTo(n2);
        n1.createEdgeTo(n3);
        n2.createEdgeTo(n3);
        n3.createEdgeTo(n2);

        logger.info("n1 = " + n1.getProperty("name"));
        logger.info("n2 = " + n2.getProperty("name"));
        logger.info("n3 = " + n3.getProperty("name"));

        TextIndex sample = network.textIndex("sample");
        sample.put(n1, "name", n1.getProperty("name"));
        sample.put(n2, "name", n2.getProperty("name"));
        sample.put(n2, "fictional", "true");
        sample.put(n3, "name", n3.getProperty("name"));
        sample.put(n3, "fictional", "true");

        URINode u = sample.lookup("name", "Agatha");
        logger.info("u = " + u.getProperty("name"));
    }

}
