package esl.cuenet.ranking;

public interface PropagationFunction {

    boolean matchStartNode(URINode start);

    boolean matchEdge(TypedEdge edge);

    boolean matchEndNode(URINode end);

    void propagate(URINode start, TypedEdge edge, URINode end, double startNodeScore);

}
