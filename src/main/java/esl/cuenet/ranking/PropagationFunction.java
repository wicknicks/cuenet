package esl.cuenet.ranking;

public interface PropagationFunction {

    boolean matchStartNode(URINode start);

    boolean matchEdge(TypedEdge edge);

    boolean matchEndNode(URINode end);

    double propagate(URINode start, TypedEdge edge, URINode end, double startNodeScore);

}
