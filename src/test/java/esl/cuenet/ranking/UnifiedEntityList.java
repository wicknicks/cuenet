package esl.cuenet.ranking;

import com.mongodb.BasicDBObject;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.source.accessors.Utils;
import esl.system.SysLoggerUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.Traversal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnifiedEntityList {

    private Logger logger = Logger.getLogger(UnifiedEntityList.class);
    private enum Relation implements RelationshipType {
        SAME
    }
    private class EmailScanner extends MongoDB {

        private HashMap<String, Node> nodeMap = new HashMap<String, Node>();


        public EmailScanner() {
            super("arjun");
        }

        public void query() {
            DBReader reader = this.startReader("emails");
            BasicDBObject keys = new BasicDBObject();
            keys.put("to", 1);
            keys.put("cc", 1);
            keys.put("from", 1);
            keys.put("_id", 0);
            reader.getAll(keys);

            String to, from, cc;
            List<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>();

            while (reader.hasNext()) {
                BasicDBObject obj = (BasicDBObject) reader.next();
                to = obj.getString("to");
                if (to != null) entries.addAll(Utils.parseEmailAddresses(to));

                from = obj.getString("from");
                if (from != null) entries.addAll(Utils.parseEmailAddresses(from));

                cc = obj.getString("cc");
                if (cc != null)entries.addAll(Utils.parseEmailAddresses(cc));
            }

            GraphDatabaseService graphDb = new EmbeddedGraphDatabase(directory);
            Transaction tx = graphDb.beginTx();
            try {
                construct(graphDb, entries);
                tx.success();

                find(nodeMap.get("srafatirad@gmail.com"));

            } catch (Exception e) {
                tx.failure();
                e.printStackTrace();
            } finally {
                tx.finish();
                graphDb.shutdown();
            }

        }

        private void find(Node node) {
            TraversalDescription td = Traversal.description()
                    .depthFirst()
                    .relationships(Relation.SAME)
                    .evaluator(Evaluators.excludeStartPosition());

            Traverser traverser = td.traverse(node);
            String output = "\n";
            for (Path path: traverser) {
                output += "At depth " + path.length() + " => ("
                        + path.endNode().getProperty("type") + ") " + path.endNode().getProperty("text") + "\n";
            }
            logger.info(output);
        }

        private void construct(GraphDatabaseService graphDb, List<Map.Entry<String, String>> entries) {
            for(Map.Entry<String, String> entry: entries) {
                String em = entry.getKey();
                String nm = entry.getValue();
                if (nm == null) continue;

                Node emailNode, nameNode;
                if ( !nodeMap.containsKey(em) ) {
                    emailNode = graphDb.createNode();
                    emailNode.setProperty("text", em);
                    emailNode.setProperty("count", 1);
                    emailNode.setProperty("type", "email");
                    nodeMap.put(em, emailNode);
                } else {
                    emailNode = nodeMap.get(em);
                    emailNode.setProperty("count", ((Integer) emailNode.getProperty("count")) + 1);
                }

                if ( !nodeMap.containsKey(nm) ) {
                    nameNode = graphDb.createNode();
                    nameNode.setProperty("text", nm);
                    nameNode.setProperty("type", "name");
                    nameNode.setProperty("count", 1);
                    nodeMap.put(nm, nameNode);
                } else {
                    nameNode = nodeMap.get(nm);
                    nameNode.setProperty("count", ((Integer) nameNode.getProperty("count")) + 1);
                }

                nameNode.createRelationshipTo(emailNode, Relation.SAME);
            }
        }
    }

    static String directory = "/data/graph_db/email";

    @BeforeClass
    public static void setup() {
        SysLoggerUtils.initLogger();

        System.out.println("Deleting Files in " + directory);

        File[] files = (new File(directory)).listFiles();
        if (files == null) files = new File[]{};
        for (File file: files) FileUtils.deleteQuietly(file);

    }

    @Test
    public void unifyEntities() {
        (new EmailScanner()).query();
    }

}
