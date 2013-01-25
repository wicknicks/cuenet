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
        System.out.println("Deleting Files in " + directory);

        File[] files = (new File(directory)).listFiles();
        if (files == null) files = new File[]{};
        for (File file: files) FileUtils.deleteQuietly(file);

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
        NeoOntologyImporter importer = new NeoOntologyImporter( model );
        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );
        EventEntityNetwork network = new PersistentEventEntityNetwork( graphDb );

        importer.loadIntoGraph(network);

        graphDb.shutdown();

//        graphDb = new EmbeddedGraphDatabase( directory );
//        network = new PersistentEventEntityNetwork( graphDb );
//
//        HashSet<String> uris = new HashSet<String>();
//        StmtIterator iter = model.listStatements();
//        while (iter.hasNext()) {
//            Statement statement = iter.nextStatement();
//            uris.add(statement.getSubject().getURI());
//            if (statement.getObject().isResource()) uris.add(statement.getObject().toString());
//        }
//
//        String[] URIs = new String[uris.size()];
//        uris.toArray(URIs);
//
//        TextIndex index = network.textIndex(NeoOntologyImporter.nodeURIIndexName);
//        Random generator = new Random();
//        for (int i=0; i < 10; i++) {
//            int u = generator.nextInt(URIs.length);
//            logger.info("Looking up --> " + URIs[u]);
//            if ( URIs[u].charAt(0) == '-' ) continue;
//            URINode node = index.lookup(OntProperties.ONT_URI, "B" + URIs[u]);
//            if (node == null)
//                logger.info("No nodes corresponding to key: " + OntProperties.ONT_URI + " " + URIs[u]);
//            else
//            assert(node.getProperty(OntProperties.ONT_URI) == URIs[u]);
//        }
    }
}
