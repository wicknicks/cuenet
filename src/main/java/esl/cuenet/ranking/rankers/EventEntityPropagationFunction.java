package esl.cuenet.ranking.rankers;

import esl.cuenet.ranking.PropagationFunction;
import esl.cuenet.ranking.TypedEdge;
import esl.cuenet.ranking.URINode;
import esl.cuenet.ranking.network.OntProperties;

public class EventEntityPropagationFunction extends NodeEvaluator implements PropagationFunction {

    public EventEntityPropagationFunction() {
        super();
    }

    @Override
    public boolean matchStartNode(URINode start) {
        return isEntity(start);
    }

    @Override
    public boolean matchEdge(TypedEdge edge) {
        if (edge == null) return false;
        if ( !edge.hasProperty(OntProperties.ONT_URI) ) return false;
        return edge.getProperty(OntProperties.ONT_URI).equals(participatesInPropertyURI);
    }

    @Override
    public boolean matchEndNode(URINode end) {
        return isEvent(end);
    }

    @Override
    public double propagate(URINode start, TypedEdge edge, URINode end, double startNodeScore) {
        return startNodeScore;
    }

}
