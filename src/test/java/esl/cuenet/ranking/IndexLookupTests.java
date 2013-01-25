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
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.io.File;
import java.util.HashSet;
import java.util.Random;

public class IndexLookupTests {

    private static String directory = "/data/graph_db/tests";
    private Logger logger = Logger.getLogger(IndexLookupTests.class);
    private static OntModel model = null;

    @BeforeClass
    public static void setUp() {
        SysLoggerUtils.initLogger();
        model = ModelFactory.createOntologyModel();
    }

    @Test
    public void test() {
        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );
        Index<Node> ix = graphDb.index().forNodes(NeoOntologyImporter.nodeURIIndexName);

        IndexHits<Node> hits = ix.get(OntProperties.ONT_URI, "http://www.w3.org/2000/01/rdf-schema#Datatype");
        logger.info("hits size = " + hits.size());
        logger.info(hits.getSingle().getProperty(OntProperties.ONT_URI));

        graphDb.shutdown();
    }

    @Test
    public void readIxTest() {
        HashSet<String> uris = new HashSet<String>();
        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            Statement statement = iter.nextStatement();
            uris.add(statement.getSubject().getURI());
            if (statement.getObject().isResource()) uris.add(statement.getObject().toString());
        }

        String[] URIs = new String[uris.size()];
        uris.toArray(URIs);

        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );
        EventEntityNetwork network = new PersistentEventEntityNetwork( graphDb );

        Random generator = new Random();
        TextIndex index = network.textIndex(NeoOntologyImporter.nodeURIIndexName);
        try {
            logger.info("Looking up --> " + "http://www.w3.org/2000/01/rdf-schema#Datatype");
            lookup(index, "http://www.w3.org/2000/01/rdf-schema#Datatype");

            for (int i=0; i < 10; i++) {
                int u = generator.nextInt(URIs.length);
                logger.info("Looking up --> " + URIs[u]);
                lookup(index, URIs[u]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            graphDb.shutdown();
        }
    }

    private void lookup(TextIndex index, String uri) {
        URINode n = index.lookup(OntProperties.ONT_URI, uri);
        if (n == null) logger.info("No results found");
        else logger.info("RESULT => " + n.getProperty(OntProperties.ONT_URI));
    }

    //    @Test
    public void write() {
        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );
        Transaction tx = graphDb.beginTx();

        Node n1 = graphDb.createNode();
        n1.setProperty("name", "Agatha");
        logger.info("n1.id = " + n1.getId());

        Node n2 = graphDb.createNode();
        n2.setProperty("name", "Poirot");

        Node n3 = graphDb.createNode();
        n3.setProperty("name", "Marple");

        n1.createRelationshipTo(n2, NeoGraphTest.MysteriousRelations.CREATED);
        n1.createRelationshipTo(n3, NeoGraphTest.MysteriousRelations.CREATED);
        n2.createRelationshipTo(n3, NeoGraphTest.MysteriousRelations.KNOWS);
        n3.createRelationshipTo(n2, NeoGraphTest.MysteriousRelations.KNOWS);

        Index<Node> nameIx = graphDb.index().forNodes("nameIx");
        nameIx.add(n1, "name", n1.getProperty("name"));
        nameIx.add(n2, "name", n2.getProperty("name"));
        nameIx.add(n2, "fictional", "true");
        nameIx.add(n3, "name", n3.getProperty("name"));
        nameIx.add(n3, "fictional", "true");

        tx.success();
        tx.finish();
        graphDb.shutdown();
    }

}
