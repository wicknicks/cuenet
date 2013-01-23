package esl.cuenet.ranking;

import esl.datastructures.graph.Node;

public interface EntityBase {

    Node lookup(String key, Object value);

}
