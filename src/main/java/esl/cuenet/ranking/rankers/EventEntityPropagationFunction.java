package esl.cuenet.ranking.rankers;

import esl.cuenet.ranking.PropagationFunction;
import esl.cuenet.ranking.TypedEdge;
import esl.cuenet.ranking.URINode;
import esl.cuenet.ranking.network.OntProperties;

public class EventEntityPropagationFunction extends NodeEvaluator implements PropagationFunction {

    int ix = 0;

    @Override
    public boolean matchStartNode(URINode start) {
        return isEvent(start);
    }

    @Override
    public boolean matchEdge(TypedEdge edge) {
        if (edge == null) return false;
        if ( !edge.hasProperty(OntProperties.ONT_URI) ) return false;
        return edge.getProperty(OntProperties.ONT_URI).equals(participatesInPropertyURI);
    }

    @Override
    public boolean matchEndNode(URINode end) {
        return isEntity(end);
    }

    @Override
    public double propagate(URINode start, TypedEdge edge, URINode end, double startNodeScore) {
        ix++;
        return startNodeScore * _DAMPNER;
    }

    public int count() {
        return ix;
    }
}
