package esl.cuenet.ranking;

import java.util.Iterator;

public interface EventEntityNetwork extends Versioned {

    public URINode createNode();

    EventEntityNetwork subnet(Iterator<URINode> nodeIterator);

    NetworkTraverser traverser();

    SpatioTemporalIndex stIndex(String indexName);

    TextIndex textIndex(String indexName);

    OntoInstanceFactory ontoInstanceFactory();

    void rank(Ranker ranker);

}
