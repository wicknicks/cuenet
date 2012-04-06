package esl.datastructures.graph.relationgraph;

import esl.datastructures.graph.Graph;

import java.util.List;

public interface IRelationGraph extends Graph<RelationGraphNode, RelationGraphEdge> {

    public boolean containsClass(String name);

    public List<RelationGraphNode> getNodesOfType(String type);

}
