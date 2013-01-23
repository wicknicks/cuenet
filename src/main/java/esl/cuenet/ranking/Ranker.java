package esl.cuenet.ranking;

import esl.datastructures.graph.Node;

import java.util.Iterator;
import java.util.Map;

public interface Ranker {

    Iterator<Map.Entry<Node, Double>> results();

}
