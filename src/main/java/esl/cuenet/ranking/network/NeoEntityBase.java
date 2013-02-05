package esl.cuenet.ranking.network;

import com.mongodb.BasicDBObject;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.ranking.EntityBase;
import esl.cuenet.ranking.TypedEdge;
import esl.cuenet.ranking.URINode;
import esl.cuenet.source.accessors.AccessorConstants;
import esl.cuenet.source.accessors.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
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

public class NeoEntityBase implements EntityBase {

    private final GraphDatabaseService graphDbExt;
    private Logger logger = Logger.getLogger(NeoEntityBase.class);
    private HashMap<String, Node> nodeMap = new HashMap<String, Node>();
    public static final String ENT_GRAPH_LITERAL_INDEX = "text";

    private HashSet<Long> entityIdSet = new HashSet<Long>();

    @Override
    public Iterator<Long> iterator() {
        return entityIdSet.iterator();
    }

    enum Relation implements RelationshipType {
        SAME
    }

    public NeoEntityBase (GraphDatabaseService graphDb) {
        this.graphDbExt = graphDb;
        load();
    }

    public static void printEntity(URINode entity, Logger logger) {
        if (entity == null) {
            logger.info("NULL");
            return;
        }
        String s = "";
        for (TypedEdge r: entity.getAllRelationships()) {
            s += r.getProperty(EntityBase.TYPE) + " " + r.getEndNode().getProperty(EntityBase.TEXT) + "; ";
        }
        logger.info(s);
    }

    private void load() {
        String query = "START n=node(*) WHERE has(n." + EntityBase.TYPE + ") AND n." + EntityBase.TYPE + "= '" + EntityBase.ENTITY + "' RETURN n";

        ExecutionEngine engine = new ExecutionEngine( graphDbExt );
        ExecutionResult results = engine.execute(query);

        for (Map<String, Object> result: results)
            for ( Map.Entry<String, Object> column : result.entrySet() ) {
                Node n = (Node) column.getValue();
                entityIdSet.add(n.getId());
        }

        logger.info("Loaded " + entityIdSet.size() + " entities.");
    }

    public void construct() {

        File storeTempDir = null;

        try {
            storeTempDir = new File(FileUtils.getTempDirectoryPath() + File.separator + System.currentTimeMillis());
            logger.info("Creating temp graph db at: " + storeTempDir.getAbsolutePath());
            FileUtils.forceMkdir(storeTempDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        GraphDatabaseService graphDb = new EmbeddedGraphDatabase(storeTempDir.getAbsolutePath());
        Transaction tx = graphDb.beginTx();

        try {
            (new EmailScanner()).populate(graphDb);
            (new FacebookIdScanner()).populate(graphDb);

            tx.success();
            mergeIntoMainDB();

        } catch (Exception e) {
            tx.failure();
            e.printStackTrace();
        } finally {
            tx.finish();
            graphDb.shutdown();
        }

        try {
            FileUtils.deleteDirectory(storeTempDir);
            logger.info("Deleted temp db: " + storeTempDir.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mergeIntoMainDB() {
        LinkedList<Node> set = new LinkedList<Node>(nodeMap.values());
        int md;

        Index<Node> xIndex = graphDbExt.index().forNodes(ENT_GRAPH_LITERAL_INDEX);
        while (set.size() > 0) {
            Node node = set.getFirst();
            if (node.getProperty("seen").equals("true")) {
                set.remove(node);
                continue;
            }

            TraversalDescription td = Traversal.description()
                    .depthFirst()
                    .relationships(Relation.SAME)
                    .evaluator(Evaluators.excludeStartPosition());

            Traverser traverser = td.traverse(node);
//            String output = node.getProperty("text") + "\n";

            md = 0;
            Node entityNode = graphDbExt.createNode();
            entityNode.setProperty(EntityBase.TYPE, EntityBase.ENTITY);

            Node propertyNode = graphDbExt.createNode();
            propertyNode.setProperty(EntityBase.TEXT, node.getProperty(EntityBase.TEXT));
            entityNode.createRelationshipTo(propertyNode, NeoRelationships.BLANK).
                    setProperty(EntityBase.TYPE,  node.getProperty(EntityBase.TYPE));

            xIndex.add(propertyNode, EntityBase.TEXT, node.getProperty(EntityBase.TEXT));

            for (Path path: traverser) {
//                output += "At depth " + path.length() + " => ("
//                        + path.endNode().getProperty(EntityBase.TYPE) + ") "
//                        + path.endNode().getProperty(EntityBase.TEXT) + "\n";
                path.endNode().setProperty("seen", "true");
                md++;

                propertyNode = graphDbExt.createNode();
                propertyNode.setProperty(EntityBase.TEXT, path.endNode().getProperty(EntityBase.TEXT));
                entityNode.createRelationshipTo(propertyNode, NeoRelationships.BLANK).
                        setProperty(EntityBase.TYPE, path.endNode().getProperty(EntityBase.TYPE));

                xIndex.add(propertyNode, EntityBase.TEXT, path.endNode().getProperty(EntityBase.TEXT));
            }

            set.remove(node);
//            if (md == 0) entityNode.delete();
//            logger.info(output);
        }
    }

    @Override
    public URINode lookup(String key, Object value) {
        Index<Node> xIndex = graphDbExt.index().forNodes(NeoEntityBase.ENT_GRAPH_LITERAL_INDEX);
        IndexHits<Node> hits = xIndex.get(EntityBase.TEXT, value);
        if (hits.size() == 0) return null;

        Node hit = hits.getSingle();
        Relationship outgoing = hit.getRelationships(Direction.INCOMING).iterator().next();

        if (outgoing.getProperty(EntityBase.TYPE).equals(key))
            return new NeoURINode(outgoing.getStartNode());

        return null;
    }

    private class FacebookIdScanner extends MongoDB {

        public FacebookIdScanner() {
            super(AccessorConstants.DBNAME);
        }

        public void populate(GraphDatabaseService graphDb) {
            DBReader reader = this.startReader("fb_users");
            BasicDBObject keys = new BasicDBObject();
            keys.put("id", 1);
            keys.put("name", 1);
            keys.put("_id", 0);

            reader.getAll(keys);
            String id, name;
            while (reader.hasNext()) {
                BasicDBObject obj = (BasicDBObject) reader.next();
                id = obj.getString("id");
                name = obj.getString("name");

                Node fbIdNode = graphDb.createNode();
                fbIdNode.setProperty(EntityBase.TEXT, id);
                fbIdNode.setProperty(EntityBase.TYPE, EntityBase.V_FB_ID);
                fbIdNode.setProperty("seen", "false");
                fbIdNode.setProperty("count", 1);

                Node fbUser;
                if (nodeMap.containsKey(name)) fbUser = nodeMap.get(name);
                else {
                    fbUser = graphDb.createNode();
                    fbUser.setProperty(EntityBase.TEXT, name);
                    fbUser.setProperty(EntityBase.TYPE, EntityBase.V_NAME);
                    fbUser.setProperty("count", 1);
                    fbUser.setProperty("seen", "false");
                    nodeMap.put(name, fbUser);
                }

                fbUser.createRelationshipTo(fbIdNode, Relation.SAME);
            }
        }
    }

    private class EmailScanner extends MongoDB {

        public EmailScanner() {
            super(AccessorConstants.DBNAME);
        }

        public void populate(GraphDatabaseService graphDb) {
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

            construct(graphDb, entries);
        }

        private void construct(GraphDatabaseService graphDb, List<Map.Entry<String, String>> entries) {
            for(Map.Entry<String, String> entry: entries) {
                String em = entry.getKey();
                String nm = entry.getValue();
                if (nm == null) continue;

                Node emailNode, nameNode;
                if ( !nodeMap.containsKey(em) ) {
                    emailNode = graphDb.createNode();
                    emailNode.setProperty(EntityBase.TEXT, em);
                    emailNode.setProperty(EntityBase.TYPE, EntityBase.V_EMAIL);
                    emailNode.setProperty("seen", "false");
                    emailNode.setProperty("count", 1);
                    nodeMap.put(em, emailNode);
                } else {
                    emailNode = nodeMap.get(em);
                    emailNode.setProperty("count", ((Integer) emailNode.getProperty("count")) + 1);
                }

                if ( !nodeMap.containsKey(nm) ) {
                    nameNode = graphDb.createNode();
                    nameNode.setProperty(EntityBase.TEXT, nm);
                    nameNode.setProperty(EntityBase.TYPE, EntityBase.V_NAME);
                    nameNode.setProperty("count", 1);
                    nameNode.setProperty("seen", "false");
                    nodeMap.put(nm, nameNode);
                } else {
                    nameNode = nodeMap.get(nm);
                    nameNode.setProperty("count", ((Integer) nameNode.getProperty("count")) + 1);
                }

                nameNode.createRelationshipTo(emailNode, Relation.SAME);
            }
        }
    }
}
