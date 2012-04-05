package esl.datastructures.graph.relationgraph;

import org.apache.log4j.Logger;

import java.util.*;

public class RelationGraph implements IRelationGraph {

    private List<RelationGraphNode> nodes = new ArrayList<RelationGraphNode>();
    private HashMap<RelationGraphNode, List<RelationGraphEdge>> edgeMap = new HashMap<RelationGraphNode, List<RelationGraphEdge>>();
    private HashMap<RelationGraphEdge, RelationGraphNode> edgeDestinationMap = new HashMap<RelationGraphEdge, RelationGraphNode>();
    private Logger logger = Logger.getLogger(RelationGraph.class);
    private HashMap<RelationGraphEdge, RelationGraphNode> edgeOriginMap = new HashMap<RelationGraphEdge, RelationGraphNode>();

    public RelationGraph() {
        super();
    }

    protected List<RelationGraphNode> getNodes() {
        return nodes;
    }

    @Override
    public boolean containsClass(String name) {
        for (RelationGraphNode node: nodes)
            if (node.compare(name)) return true;

        return false;
    }

    @Override
    public RelationGraphNode getStartNode() {
        return null;
    }

    @Override
    public RelationGraphNode createNode(String name) {
        RelationGraphNode node = new RelationGraphNode(name);
        nodes.add(node);
        edgeMap.put(node, new ArrayList<RelationGraphEdge>());
        return node;
    }

    @Override
    public RelationGraphEdge createEdge(String label, RelationGraphNode n1, RelationGraphNode n2) {
        return createEdge(label, UUID.randomUUID().toString(), n1, n2);
    }

    @Override
    public RelationGraphEdge createEdge(String label, String name, RelationGraphNode n1, RelationGraphNode n2) {
        if (n1 == null) throw new RuntimeException("Null Node (n1)");
        if (n2 == null) throw new RuntimeException("Null Node (n2)");
        if (label == null) throw new RuntimeException("Null label");
        if (name == null) throw new RuntimeException("Null name");
        if ( !edgeMap.containsKey(n1) ) throw new RuntimeException("Node not found: " + n1.name());
        if ( !edgeMap.containsKey(n2) ) throw new RuntimeException("Node not found: " + n2.name());

        RelationGraphEdge edge;
        edge = new RelationGraphEdge(label, name);
        if ( edgeDestinationMap.containsKey(edge) ) { throw new RuntimeException("Duplicate Edges"); }

        List<RelationGraphEdge> edges = edgeMap.get(n1);
        edges.add(edge);
        edgeDestinationMap.put(edge, n2);
        edgeOriginMap.put(edge, n1);

        return edge;
    }

    @Override
    public RelationGraphNode getNodeByName(String name) {
        for (RelationGraphNode node: nodes)
            if (node.compare(name)) return node;
        return null;
    }

    @Override
    public List<RelationGraphEdge> getOutgoingEdges(RelationGraphNode node) {
        if ( !edgeMap.containsKey(node) ) return null;
        return edgeMap.get(node);
    }

    @Override
    public RelationGraphNode getDestinationNode(RelationGraphEdge edge) {
        return edgeDestinationMap.get(edge);
    }

    @Override
    public RelationGraphNode getOriginNode(RelationGraphEdge edge) {
        return edgeOriginMap.get(edge);
    }

}