package esl.datastructures.graph.relationgraph;

import esl.datastructures.graph.Graph;

public interface IRelationGraph extends Graph<RelationGraphNode, RelationGraphEdge> {

    public boolean containsClass(String name);

}
