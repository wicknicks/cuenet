package esl.datastructures.graph;

public interface Traverser <N extends Node, E extends Edge> {

    public enum Type {
        Depth_First_Search,
        Iterative_Depth_First_Search,
        Leaf_Finder,
        Random
    }

    public void setNodeVisitorCallback(NodeVisitor visitorCallback);

    public void setEdgeVisitorCallback(EdgeVisitor visitorCallback);

    public void start(Graph<N, E> graph);

}
