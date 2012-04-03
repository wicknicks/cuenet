package esl.datastructures.graph;

import java.util.List;

/**
 *  An interface for a fully connected graph.
 *
 */

public interface Graph {

    /**
     * The root node of the graph. This can be a null.
     * @return the start node.
     */
    public Node getStartNode();

    public Node createNode(String name);

    public Node getNodeByName(String name);

    public List<Edge> getOutgoingEdges(Node node);

    public Edge createEdge(String label, Node n1, Node n2);

}
