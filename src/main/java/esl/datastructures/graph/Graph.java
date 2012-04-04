package esl.datastructures.graph;

import java.util.List;

/**
 *  An interface for a fully connected directed graph.
 */

public interface Graph<N extends Node, E extends Edge> {

    /**
     * The root node of the graph. This can be a null.
     * @return the start node.
     */
    public N getStartNode();

    /**
     * Methods to create a node in this graph
     * @param name name of the node. This value is returned with node.name() also.
     */
    public N createNode(String name);

    /**
     * Methods to create an edge in this graph between n1, n2 with label lbl
     */
    public E createEdge(String lbl, N n1, N n2);

    /**
     * Methods to create an edge in this graph between n1, n2 with label lbl and name nm
     */
    public E createEdge(String lbl, String nm, N n1, N n2);

    /**
     * Search and return graph for node with name "nm"
     * @param nm name of query node
     */
    public N getNodeByName(String nm);

    /**
     * Retrieve all edges of node
     * @param node i/p node
     */
    public List<E> getOutgoingEdges(N node);

    /**
     *
     * Retrieve destination node of an edge
     * @param edge i/p edge
     */
    public N getDestinationNode(E edge);

    /**
     *
     * Retrieve origin node of an edge
     * @param edge i/p edge
     */
    public N getOriginNode(E edge);

}
