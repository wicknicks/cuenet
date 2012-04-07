package esl.cuenet.query;

import com.hp.hpl.jena.vocabulary.RDF;
import esl.datastructures.graph.relationgraph.RelationGraph;
import esl.datastructures.graph.relationgraph.RelationGraphEdge;
import esl.datastructures.graph.relationgraph.RelationGraphNode;

import java.util.ArrayList;
import java.util.List;

public class QueryGraph extends RelationGraph {

    public List<RelationGraphNode> getAllNodes() {
        return getNodes();
    }

    public List<RelationGraphNode> getAllTypedNodes() {
        List<RelationGraphNode> typedNodes = new ArrayList<RelationGraphNode>();
        List<RelationGraphNode> nodes = getNodes();
        for (RelationGraphNode node: nodes) {
            List<RelationGraphEdge> edges = getOutgoingEdges(node);
            for (RelationGraphEdge edge: edges) {
                if (edge.label().equalsIgnoreCase("type-of")) typedNodes.add(node);
                else if (edge.label().equalsIgnoreCase(RDF.type.getURI())) typedNodes.add(node);
            }
        }

        return typedNodes;
    }
}