package esl.cuenet.query;

import esl.datastructures.graph.relationgraph.RelationGraph;
import esl.datastructures.graph.relationgraph.RelationGraphNode;

import java.util.List;

public class QueryGraph extends RelationGraph {

    public List<RelationGraphNode> getAllNodes() {
        return getNodes();

    }

}