package esl.cuenet.ranking;

import esl.datastructures.graph.Node;

import java.util.Iterator;

public interface EventEntityNetwork extends Versioned {

    EventEntityNetwork subnet(Iterator<Node> nodeIterator);

    NetworkTraverser traverser();

    SpatioTemporalIndex stIndex();

    TextIndex textIndex();

    OntoInstanceFactory ontoInstanceFactory();

    void rank(Ranker ranker);

}
