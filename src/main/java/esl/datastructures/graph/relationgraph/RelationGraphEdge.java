package esl.datastructures.graph.relationgraph;

import esl.datastructures.graph.Edge;
import esl.datastructures.graph.sample.DAGEdge;

public class RelationGraphEdge extends DAGEdge implements Edge {

    public RelationGraphEdge(String label, String name) {
        super(label, name);
    }

}