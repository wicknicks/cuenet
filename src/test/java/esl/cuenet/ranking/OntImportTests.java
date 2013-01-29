package esl.cuenet.ranking;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import esl.cuenet.ranking.network.NeoOntologyImporter;
import esl.cuenet.ranking.network.OntProperties;
import esl.cuenet.ranking.network.PersistentEventEntityNetwork;
import esl.system.SysLoggerUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Random;

public class OntImportTests {

    private static String directory = "/data/graph_db/tests";
    private Logger logger = Logger.getLogger(NeoGraphTest.class);
    private static OntModel model = null;

    @BeforeClass
    public static void setUp() {
        SysLoggerUtils.initLogger();
//        System.out.println("Deleting Files in " + directory);
//
//        File[] files = (new File(directory)).listFiles();
//        if (files == null) files = new File[]{};
//        for (File file: files) FileUtils.deleteQuietly(file);

        model = ModelFactory.createOntologyModel();

        try {
            model.read(new FileReader("/home/arjun/Documents/Dropbox/Ontologies/cuenet-main/cuenet-main.owl"),
                    "http://www.semanticweb.org/arjun/cuenet-main.owl");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

//    @AfterClass
//    public static void tearDown() {
//        File[] files = (new File(directory)).listFiles();
//        if (files == null) files = new File[]{};
//        for (File file: files) FileUtils.deleteQuietly(file);
//    }

    @Test
    public void importTest() {
        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );
        EventEntityNetwork network = new PersistentEventEntityNetwork( graphDb );

        Transaction tx = graphDb.beginTx();
        try {
            NeoOntologyImporter importer = new NeoOntologyImporter( model );
            importer.loadIntoGraph(network);
            tx.success();
        } catch (Exception e) {
            tx.failure();
            logger.error("Exception = " + e.getLocalizedMessage());
        } finally {
            tx.finish();
            graphDb.shutdown();
        }

        graphDb = new EmbeddedGraphDatabase( directory );

        try {

        logger.info(" ---------------------------------- ");
        logger.info("               URIs                 ");
        logger.info(" ---------------------------------- ");

        Iterable<Node> nodes = graphDb.getAllNodes();
        HashSet<String> uriSet = new HashSet<String>();
        int i = 0;
        for (Node n: nodes) {
            try {
                logger.info((++i) + " " + n.getProperty(OntProperties.ONT_URI));
                uriSet.add((String) n.getProperty(OntProperties.ONT_URI));
            } catch (NotFoundException e) {
                logger.info(e.getMessage());
            }
        }

        logger.info("uriSet.size() = " + uriSet.size());

        } catch (Exception e) {
            logger.info("Exception = " + e.getMessage());
        } finally {
            graphDb.shutdown();
        }
    }
}
