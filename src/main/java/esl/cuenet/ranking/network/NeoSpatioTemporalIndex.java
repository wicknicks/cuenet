package esl.cuenet.ranking.network;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;
import esl.cuenet.model.Constants;
import esl.cuenet.ranking.SpatioTemporalIndex;
import esl.cuenet.ranking.URINode;
import esl.datastructures.Location;
import org.apache.log4j.Logger;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.net.UnknownHostException;
import java.util.*;

public class NeoSpatioTemporalIndex implements SpatioTemporalIndex {

    private final GraphDatabaseService graphDb;
    private final String timestampMillisStart;
    private final String timestampMillisEnd;

    private final String M_START, M_END, M_ID;

    private Logger logger = Logger.getLogger(NeoSpatioTemporalIndex.class);
    private DBCollection txColl;

    public NeoSpatioTemporalIndex(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
        timestampMillisStart = Constants.CuenetNamespace + Constants.TimestampMillisStart;
        timestampMillisEnd = Constants.CuenetNamespace + Constants.TimestampMillisEnd;

        M_END = "end";
        M_START = "start";
        M_ID = "id";

        openDB("128.195.54.27", "neo4j", "tx");
    }

    private void openDB(String location, String dbName, String collection) {
        try {
            Mongo m = new Mongo(location, 27017);
            txColl = m.getDB(dbName).getCollection(collection);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private long getStartTime(Node node) {
        for (Relationship relationship: node.getRelationships(NeoRelationships.BLANK, Direction.OUTGOING)) {
            if ( !relationship.hasProperty(OntProperties.ONT_URI) ) continue;
            if ( timestampMillisStart.equals(relationship.getProperty(OntProperties.ONT_URI)) )
                return (Long) relationship.getEndNode().getProperty(OntProperties.LVALUE);
        }
        throw new RuntimeException("Couldn't find interval start value " + node);
    }

    private long getEndTime(Node node) {
        for (Relationship relationship: node.getRelationships(NeoRelationships.BLANK, Direction.OUTGOING)) {
            if ( !relationship.hasProperty(OntProperties.ONT_URI) ) continue;
            if ( timestampMillisEnd.equals(relationship.getProperty(OntProperties.ONT_URI)) )
                return (Long) relationship.getEndNode().getProperty(OntProperties.LVALUE);
        }
        throw new RuntimeException("Couldn't find interval end value " + node);
    }



    @Override
    public void construct() {
        logger.info("Constructing Index");

        refresh();
        final String timeIntervalURI = Constants.CuenetNamespace + Constants.TimeInterval;
        String query = "START n=node(*) WHERE has(n.ontURI) AND n.ontURI = '" + timeIntervalURI + "' RETURN n";

        ExecutionEngine engine = new ExecutionEngine( graphDb );
        ExecutionResult results = engine.execute(query);

        for (Map<String, Object> result: results)
            for ( Map.Entry<String, Object> column : result.entrySet() ) {
                Node n = (Node) column.getValue();
                index(n.getId(), getStartTime(n), getEndTime(n));
            }

        logger.info("-----------");
    }

    private void index(long id, long startTime, long endTime) {
        BasicDBObject o = new BasicDBObject(M_ID, id);
        o.put(M_END, endTime);
        o.put(M_START, startTime);
        txColl.insert(o);
    }

    private void refresh() {
        txColl.remove(new BasicDBObject());

        BasicDBObject keys = new BasicDBObject(M_START, 1);
        keys.put(M_END, 1);
        txColl.ensureIndex(keys);
    }

    private List<BasicDBObject> execute(String qryString, int limit) {
        BasicDBObject query = (BasicDBObject) JSON.parse(qryString);
        List<BasicDBObject> results = new ArrayList<BasicDBObject>(25);

        for (DBObject o: txColl.find(query, new BasicDBObject("_id", 0)).limit(limit)) {
            results.add((BasicDBObject) o);
        }

        return results;
    }

    private List<BasicDBObject> execute(String qryString, String sortKey, int sortOrder, int limit) {
        BasicDBObject query = (BasicDBObject) JSON.parse(qryString);
        List<BasicDBObject> results = new ArrayList<BasicDBObject>(25);

        for (DBObject o: txColl.find(query, new BasicDBObject("_id", 0)).sort(new BasicDBObject(sortKey, sortOrder)).limit(limit)) {
            results.add((BasicDBObject) o);
        }

        return results;
    }

    private List<BasicDBObject> execute(String qryString) {
        BasicDBObject query = (BasicDBObject) JSON.parse(qryString);
        List<BasicDBObject> results = new ArrayList<BasicDBObject>(25);

        for (DBObject o: txColl.find(query, new BasicDBObject("_id", 0))) {
            results.add((BasicDBObject) o);
        }

        return results;
    }

    @Override
    public void overlaps(long start, long end) {
        String qryString = String.format("{'start': {'$gt': %d}, 'end': {'$lt': %d} }", start, end);
        int ix = 0;
        for (BasicDBObject r: execute(qryString)) logger.info((++ix) + ". " + r);
    }

    @Override
    public void before(long moment) {
        String qryString = String.format("{'end': {'$lt': %d} }", moment);
        execute(qryString);
    }

    @Override
    public void after(long moment) {
        String qryString = String.format("{'start': {'$gt': %d} }", moment);
        execute(qryString);
    }

    @Override
    public void previous (long timestamp) {
        String qryString = String.format("{'end': {'$lt': %d} }", timestamp);
        List<BasicDBObject> results = execute(qryString);
        Collections.sort(results, new Comparator<BasicDBObject>() {
            @Override
            public int compare(BasicDBObject o1, BasicDBObject o2) {
                long l1 = o1.getLong(M_END);
                long l2 = o2.getLong(M_END);

                if (l1 < l2) return 1;
                else if (l1 > l2) return -1;
                return 0;
            }
        });


        logger.info("I_TS - " + new Date(timestamp));
        int ix = 0;
        for (BasicDBObject r: results) {
            long s = r.getLong(M_START);
            logger.info("PREV - " + new Date(s) + " " + r);
            ix++;
            if (ix == 10) break;
        }

        results = execute(qryString, "end", -1, 1);
        logger.info("* I_TS - " + new Date(timestamp));
        ix = 0;
        for (BasicDBObject r: results) {
            long s = r.getLong(M_START);
            logger.info("* PREV - " + new Date(s) + " " + r);
            ix++;
            if (ix == 10) break;
        }
    }

    @Override
    public void next (long timestamp) {
        String qryString = String.format("{'start': {'$gt': %d} }", timestamp);
        List<BasicDBObject> results = execute(qryString);
        Collections.sort(results, new Comparator<BasicDBObject>() {
            @Override
            public int compare(BasicDBObject o1, BasicDBObject o2) {
                long l1 = o1.getLong(M_START);
                long l2 = o2.getLong(M_START);

                if (l1 > l2) return 1;
                else if (l1 < l2) return -1;
                return 0;
            }
        });


        logger.info("I_TS - " + new Date(timestamp));
        int ix = 0;
        for (BasicDBObject r: results) {
            long s = r.getLong(M_START);
            logger.info("NEXT - " + new Date(s) + " " + r);
            ix++;
            if (ix == 10) break;
        }

        results = execute(qryString, 1);
        logger.info("* I_TS - " + new Date(timestamp));
        ix = 0;
        for (BasicDBObject r: results) {
            long s = r.getLong(M_START);
            logger.info("* NEXT - " + new Date(s) + " " + r);
            ix++;
            if (ix == 10) break;
        }
    }

    @Override
    public Iterator<URINode> lookup(Location location) {
        return null;
    }

}
