package esl.cuenet.ranking.rankers;

import esl.cuenet.ranking.PropagationFunction;
import esl.cuenet.ranking.TypedEdge;
import esl.cuenet.ranking.URINode;

public class TemporalPropagationFunction extends NodeEvaluator implements PropagationFunction {

    @Override
    public boolean matchStartNode(URINode start) {
        return isEvent(start);
    }

    @Override
    public boolean matchEdge(TypedEdge edge) {
        return (edge == null);
    }

    @Override
    public boolean matchEndNode(URINode end) {
        return isEvent(end);
    }

    @Override
    public double propagate(URINode start, TypedEdge edge, URINode end, double startNodeScore) {
        return 0;
    }

}
