package esl.datastructures.graph;

public interface Edge {

    public String label();

    public Node getOrigin();

    public Node getDestination();

}
