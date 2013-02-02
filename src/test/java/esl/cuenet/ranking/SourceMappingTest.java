package esl.cuenet.ranking;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import esl.cuenet.ranking.network.NeoEntityBase;
import esl.cuenet.ranking.network.NeoOntoInstanceImporter;
import esl.cuenet.ranking.network.PersistentEventEntityNetwork;
import esl.cuenet.ranking.sources.EmailSource;
import esl.cuenet.ranking.sources.FacebookPhotoSource;
import esl.system.SysLoggerUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SourceMappingTest {

    private static Logger logger = Logger.getLogger(SourceMappingTest.class);
    private static OntModel model = null;
    private static String directory = "/data/graph_db/sources";

    @BeforeClass
    public static void setup() {
        SysLoggerUtils.initLogger();
        model = ModelFactory.createOntologyModel();

        logger.info("Deleting Files in " + directory);

        File[] files = (new File(directory)).listFiles();
        if (files == null) files = new File[]{};
        for (File file: files) FileUtils.deleteQuietly(file);


        try {
            model.read(new FileReader("/home/arjun/Documents/Dropbox/Ontologies/cuenet-main/cuenet-main.owl"),
                    "http://www.semanticweb.org/arjun/cuenet-main.owl");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

//    @Test
    public void testEmailSourceInstantiator() {
        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );
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

        long a = System.currentTimeMillis();
        SourceInstantiator src = new EmailSource();
        tx = graphDb.beginTx();
        try {
            src.populate(new PersistentEventEntityNetwork(graphDb), entityBase);
            tx.success();
        } catch (Exception e) {
            tx.failure();
            e.printStackTrace();
        } finally {
            tx.finish();
            logger.info("Time Taken: " + (System.currentTimeMillis() - a));
            graphDb.shutdown();
        }

    }

//    @Test
    public void testFacebookPhotoSourceInstantiator() {
        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );
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

        long a = System.currentTimeMillis();
        SourceInstantiator src = new FacebookPhotoSource();
        tx = graphDb.beginTx();
        try {
            src.populate(new PersistentEventEntityNetwork(graphDb), entityBase);
            tx.success();
        } catch (Exception e) {
            tx.failure();
            e.printStackTrace();
        } finally {
            tx.finish();
            logger.info("Time Taken: " + (System.currentTimeMillis() - a));
            graphDb.shutdown();
        }
    }

    @Test
    public void sourceInstantiationTest() throws IOException {
        //int c = System.in.read();
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
}
