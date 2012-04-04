package esl.datastructures.graph.sample;

import esl.datastructures.graph.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DAG implements Graph<DAGNode, DAGEdge> {

    private DAGNode start = null;
    private List<DAGNode> nodes = new ArrayList<DAGNode>();
    private HashMap<DAGNode, List<DAGEdge>> nodeEdgeMap = new HashMap<DAGNode, List<DAGEdge>>();
    private HashMap<DAGEdge, DAGNode> edgeDestinationMap = new HashMap<DAGEdge, DAGNode>();
    private HashMap<DAGEdge, DAGNode> edgeOriginMap = new HashMap<DAGEdge, DAGNode>();

    public DAG() {
        start = new DAGNode("_root");
        List<DAGEdge> edges = new ArrayList<DAGEdge>();
        nodeEdgeMap.put(start, edges);
        nodes.add(start);
    }

    @Override
    public DAGNode getStartNode() {
        return start;
    }

    @Override
    public DAGNode createNode(String nodeName) {
        DAGNode node = new DAGNode(nodeName);
        List<DAGEdge> edges = new ArrayList<DAGEdge>();
        nodeEdgeMap.put(node, edges);
        nodes.add(node);
        return node;
    }

    @Override
    public DAGEdge createEdge(String lbl, DAGNode n1, DAGNode n2) {
        return createEdge(lbl, UUID.randomUUID().toString(), n1, n2);
    }

    @Override
    public DAGNode getNodeByName(String name) {
        DAGNode n = null;
        for (DAGNode node: nodes) {
            if (node.compareName(name) == 0) n = node;
        }
        return n;
    }

    @Override
    public List<DAGEdge> getOutgoingEdges(DAGNode node) {
        if ( !nodeEdgeMap.containsKey(node) ) return null;
        return nodeEdgeMap.get(node);
    }

    @Override
    public DAGNode getDestinationNode(DAGEdge edge) {
        return edgeDestinationMap.get(edge);
    }

    @Override
    public DAGNode getOriginNode(DAGEdge edge) {
        return edgeOriginMap.get(edge);
    }

    @Override
    public DAGEdge createEdge(String label, String name, DAGNode n1, DAGNode n2) {
        if (n1 == null) throw new RuntimeException("Null Node (n1)");
        if (n2 == null) throw new RuntimeException("Null Node (n2)");
        if (label == null) throw new RuntimeException("Null label");
        if (name == null) throw new RuntimeException("Null name");
        if ( !nodeEdgeMap.containsKey(n1) ) throw new RuntimeException("Node not found: " + n1.name());
        if ( !nodeEdgeMap.containsKey(n2) ) throw new RuntimeException("Node not found: " + n2.name());

        DAGEdge edge = new DAGEdge(label, name);
        if ( edgeDestinationMap.containsKey(edge) ) { throw new RuntimeException("Duplicate Edges"); }

        List<DAGEdge> edges = nodeEdgeMap.get(n1);
        edges.add(edge);
        edgeDestinationMap.put(edge, n2);
        edgeOriginMap.put(edge, n1);

        return edge;
    }

}
