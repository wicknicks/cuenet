package esl.cuenet.ranking;

import java.util.Iterator;

public interface EventEntityNetwork extends Versioned {

    public URINode createNode();

    EventEntityNetwork subnet(Iterator<URINode> nodeIterator);

    NetworkTraverser traverser();

    SpatioTemporalIndex stIndex();

    TextIndex textIndex();

    OntoInstanceFactory ontoInstanceFactory();

    void rank(Ranker ranker);

}
