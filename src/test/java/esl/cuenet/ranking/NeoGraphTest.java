package esl.cuenet.ranking;

import esl.system.SysLoggerUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.Traversal;

import java.io.File;
import java.util.HashSet;
import java.util.Map;

public class NeoGraphTest {

    private static String directory = "/data/graph_db/tests";
    private Logger logger = Logger.getLogger(NeoGraphTest.class);

    enum MysteriousRelations implements RelationshipType
    {
        KNOWS,
        CREATED
    }

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
    public void createSimpleGraph() {
        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );

        Transaction tx = graphDb.beginTx();

        Node n1 = graphDb.createNode();
        n1.setProperty("name", "Agatha");
        logger.info("ID n1 = " + n1.getId());

        Node n2 = graphDb.createNode();
        n2.setProperty("name", "Poirot");
        logger.info("ID n2 = " + n2.getId());

        Node n3 = graphDb.createNode();
        n3.setProperty("name", "Marple");
        logger.info("ID n3 = " + n3.getId());

        n1.createRelationshipTo(n2, MysteriousRelations.CREATED);
        n1.createRelationshipTo(n3, MysteriousRelations.CREATED);
        n2.createRelationshipTo(n3, MysteriousRelations.KNOWS);
        n3.createRelationshipTo(n2, MysteriousRelations.KNOWS);

        tx.success();
        tx.finish();
        graphDb.shutdown();
    }

    @Test
    public void loadAndTraverseGraph() {
        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );
        //Transaction tx = graphDb.beginTx();   // Look ma, no transactions!

        Node n = graphDb.getNodeById(1L);
        logger.info("LOOKUP: " + n.getProperty("name"));

        n = graphDb.getNodeById(2L);
        logger.info("LOOKUP: " + n.getProperty("name"));

        n = graphDb.getNodeById(3L);
        logger.info("LOOKUP: " + n.getProperty("name"));

        n = graphDb.getNodeById(1L);
        String output = n.getProperty("name") + "'s relations\n";
        TraversalDescription td = Traversal.description()
                .depthFirst()
                .relationships(MysteriousRelations.CREATED, Direction.OUTGOING)
                .evaluator(Evaluators.excludeStartPosition());

        for (Path path: td.traverse(n)) {
            output += "At depth " + path.length() + " => " +
                    path.endNode().getProperty( "name" ) + "\n";
        }

        logger.info("\n" + output);

        //tx.finish();   // Look ma, no transactions!
        graphDb.shutdown();
    }


    @Test
    public void createIndexedGraph() {
        tearDown();

        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );
        Transaction tx = graphDb.beginTx();

        Node n1 = graphDb.createNode();
        n1.setProperty("name", "Agatha");
        logger.info("n1.id = " + n1.getId());

        Node n2 = graphDb.createNode();
        n2.setProperty("name", "Poirot");

        Node n3 = graphDb.createNode();
        n3.setProperty("name", "Marple");

        n1.createRelationshipTo(n2, MysteriousRelations.CREATED);
        n1.createRelationshipTo(n3, MysteriousRelations.CREATED);
        n2.createRelationshipTo(n3, MysteriousRelations.KNOWS);
        n3.createRelationshipTo(n2, MysteriousRelations.KNOWS);

        Index<Node> nameIx = graphDb.index().forNodes("nameIx");
        nameIx.add(n1, "name", n1.getProperty("name"));
        nameIx.add(n2, "name", n2.getProperty("name"));
        nameIx.add(n2, "fictional", "true");
        nameIx.add(n3, "name", n3.getProperty("name"));
        nameIx.add(n3, "fictional", "true");

        tx.success();
        tx.finish();
        graphDb.shutdown();

        graphDb = new EmbeddedGraphDatabase( directory );
        nameIx = graphDb.index().forNodes("nameIx");
        IndexHits<Node> hits = nameIx.get("name", "Agatha");
        logger.info("hits size = " + hits.size());
        logger.info("n1.id = " + hits.getSingle().getId());

        hits = nameIx.get("fictional", "true");
        logger.info("hits size = " + hits.size());
        logger.info("n2.id = " + hits.next().getId() + ", " + hits.next().getId());
        graphDb.shutdown();

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
        results = engine.execute(query);

        for (Map<String, Object> result: results) {
            for ( Map.Entry<String, Object> column : result.entrySet() ) {
                logger.info(column.getKey() + " " + column.getValue());
            }
        }

        graphDb.shutdown();
    }

}
