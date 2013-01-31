package esl.cuenet.ranking.network;

import com.hp.hpl.jena.ontology.OntModel;
import esl.cuenet.ranking.*;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.HashMap;
import java.util.Iterator;

public class PersistentEventEntityNetwork implements EventEntityNetwork {

    private final OntModel model;
    private final OntoInstanceFactory ontoInstanceFactory;
    private final NetworkTraverser traverser;
    private final SpatioTemporalIndex stIndex;
    private final TextIndex textIndex;
    private final GraphDatabaseService graphDb;

    private HashMap<String, TextIndex> textIndexMap = new HashMap<String, TextIndex>(5);

    public PersistentEventEntityNetwork(GraphDatabaseService graphDb) {
        this(null, null, null, null, null, graphDb);
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
    }

    @Override
    public URINode createNode() {
        return new NeoURINode(graphDb.createNode());
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
    public String version() {
        return null;
    }
}
