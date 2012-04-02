package esl.datastructures.graph.sample;

import esl.datastructures.graph.Edge;
import esl.datastructures.graph.Graph;
import esl.datastructures.graph.Node;

import java.util.ArrayList;
import java.util.List;

public class DAG implements Graph {

    private DAGNode start = null;
    private List<DAGNode> nodes = new ArrayList<DAGNode>();
    private final static String labelName = "";

    public DAG() {
        start = new DAGNode("_root");
        nodes.add(start);
    }

    @Override
    public Node getStartNode() {
        return start;
    }

    @Override
    public Node createNode(String nodeName) {
        DAGNode node = new DAGNode(nodeName);
        nodes.add(node);
        return node;
    }

    @Override
    public Node getNodeByName(String name) {
        Node n = null;
        for (DAGNode node: nodes) {
            if (node.compareName(name) == 0) n = node;
        }
        return n;
    }

    @Override
    public List<Edge> getOutgoingEdges(Node node) {
        for (DAGNode n: nodes) {
            if (n.equals(node)) return n.edges;
        }
        return null;
    }

    @Override
    public Edge createEdge(Node n1, Node n2) {
        assert (n1 instanceof DAGNode);
        assert (n2 instanceof DAGNode);

        Edge edge = new DAGEdge(labelName, n1, n2);

        ((DAGNode)n1).addEdge(edge);

        return edge;
    }

    private class DAGNode implements Node {

        private String name;
        private List<Edge> edges = new ArrayList<Edge>();

        public DAGNode(String name) {
            this.name = name;
        }

        public void addEdge(Edge edge) {
            edges.add(edge);
        }

        public List<Edge> getEdges() {
            return edges;
        }

        @Override
        public String name() {
            return name;
        }

        public int compareName(String n) {
            return name.compareTo(n);
        }
    }

    private class DAGEdge implements Edge {

        private Node destination;
        private Node origin;
        private String label;

        public DAGEdge(String label, Node n1, Node n2) {
            this.label = label;
            this.origin = n1;
            this.destination = n2;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public Node getOrigin() {
            return origin;
        }

        @Override
        public Node getDestination() {
            return destination;
        }
    }
}
