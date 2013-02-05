package esl.cuenet.ranking;

import java.util.Iterator;

public interface EventEntityNetwork extends Versioned {

    URINode createNode();

    URINode getNodeById(long id);

    EventEntityNetwork subnet(Iterator<URINode> nodeIterator);

    NetworkTraverser traverser();

    SpatioTemporalIndex stIndex(String indexName);

    TextIndex textIndex(String indexName);

    OntoInstanceFactory ontoInstanceFactory();

    Iterator<TypedEdge> getEdgesIterator();

    void rank(Ranker ranker);

    void startBulkLoad();

    void finishBulkLoad();

    void flush();

}
