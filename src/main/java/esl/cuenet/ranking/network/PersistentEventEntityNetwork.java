package esl.cuenet.ranking.network;

import com.hp.hpl.jena.ontology.OntModel;
import esl.cuenet.ranking.*;
import esl.cuenet.ranking.rankers.EventEntityPropagationFunction;
import org.apache.log4j.Logger;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import java.util.*;

public class PersistentEventEntityNetwork implements EventEntityNetwork {

    private final OntModel model;
    private final OntoInstanceFactory ontoInstanceFactory;
    private final NetworkTraverser traverser;
    private final SpatioTemporalIndex stIndex;
    private final TextIndex textIndex;
    private final GraphDatabaseService graphDb;

    private Logger logger = Logger.getLogger(PersistentEventEntityNetwork.class);

    private Transaction transaction = null;

    private HashMap<String, TextIndex> textIndexMap = new HashMap<String, TextIndex>(5);
    private List<URINode> events = null;

    public PersistentEventEntityNetwork(GraphDatabaseService graphDb) {
        this(null, null, null, new NeoSpatioTemporalIndex(graphDb), null, graphDb);
    }

    public PersistentEventEntityNetwork(OntModel model, OntoInstanceFactory factory,
                                        NetworkTraverser traverser, SpatioTemporalIndex stIndex,
                                        TextIndex textIndex, GraphDatabaseService graphDb) {
        this.model = model;
        this.ontoInstanceFactory =  factory;
        this.traverser = traverser;
        this.stIndex = stIndex;
        this.textIndex = textIndex;
        this.graphDb = graphDb;

        Index<Node> nodeIndex = graphDb.index().forNodes(EventEntityNetwork.EVENT_INDEX);
        IndexHits<Node> hits = nodeIndex.get(EntityBase.TYPE, EventEntityNetwork.EVENT);

        events = new ArrayList<URINode>(hits.size());
        for (Node n: hits) events.add(NeoCache.getInstance().lookupNode(n));
    }

    @Override
    public URINode createNode() {
        return new NeoURINode(graphDb.createNode());
    }

    @Override
    public URINode getNodeById(long id) {
        return NeoCache.getInstance().lookupNode(graphDb.getNodeById(id));
    }

    @Override
    public EventEntityNetwork subnet(Iterator<URINode> nodeIterator) {
        return null;
    }

    @Override
    public NetworkTraverser traverser() {
        return traverser;
    }

    @Override
    public SpatioTemporalIndex stIndex(String indexName) {
        return stIndex;
    }

    @Override
    public TextIndex textIndex(String indexName) {
        return new NeoLuceneIndex(graphDb.index().forNodes(indexName));
    }

    @Override
    public OntoInstanceFactory ontoInstanceFactory() {
        return ontoInstanceFactory;
    }

    @Override
    public void rank(Ranker ranker) {
    }

    @Override
    public void startBulkLoad() {
        transaction = graphDb.beginTx();
    }

    @Override
    public void finishBulkLoad() {
        transaction.success();
        transaction.finish();
    }

    @Override
    public void flush() {
        finishBulkLoad();
        startBulkLoad();
    }

    @Override
    public String version() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Iterator<TypedEdge> getEdgesIterator() {
        logger.info("Loading all relations");
        List<TypedEdge> edges = new ArrayList<TypedEdge>();

        String query = "START r=rel(*) RETURN r";
        PropagationFunction function = new EventEntityPropagationFunction();

        ExecutionEngine engine = new ExecutionEngine( graphDb );
        ExecutionResult results = engine.execute(query);

        int ix = 0;
        for (Map<String, Object> result: results) {
            for ( Map.Entry<String, Object> column : result.entrySet() ) {
                ix++;
                NeoTypedEdge te = new NeoTypedEdge((Relationship) column.getValue());
                if (function.matchEdge(te)) {
                    ix--; ix++;
                }
                if (column.getKey().equals("r")) edges.add(te);
            }
            if (ix % 100000 == 0) logger.info(ix);
        }

        logger.info("Total Relations: " + ix);
        return edges.iterator();
    }

    @Override
    public Iterator<URINode> getEventsIterator() {
        return events.iterator();
    }
}
