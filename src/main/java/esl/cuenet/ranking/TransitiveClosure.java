package esl.cuenet.ranking;

import esl.datastructures.graph.Edge;
import esl.datastructures.graph.Node;

import java.util.Iterator;

public interface TransitiveClosure {

    void compute();

    Iterator<Edge> getRelations(Node node);

}
