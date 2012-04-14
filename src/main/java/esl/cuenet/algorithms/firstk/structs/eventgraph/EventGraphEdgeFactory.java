package esl.cuenet.algorithms.firstk.structs.eventgraph;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import esl.cuenet.model.Constants;

public class EventGraphEdgeFactory {

    public static EventGraphEdge createSubeventEdge(OntModel model) throws EventGraphException {
        return createObjectPropertyEdge(model, Constants.SubEvent); /*"subevent-of"*/
    }

    public static EventGraphEdge createParticipatesInEdge(OntModel model) throws EventGraphException {
        return createObjectPropertyEdge(model, Constants.ParticipantIn); /*"participant-in"*/
    }

    public static EventGraphEdge createLiteralEdge(OntModel model, String literalLabel) throws EventGraphException {
        DatatypeProperty property = null;
        for (String nsKey: model.getNsPrefixMap().keySet()) {
            String ns = model.getNsPrefixMap().get(nsKey);
            property = model.getDatatypeProperty(model.getNsPrefixMap().get(ns) + literalLabel);
            if (property == null) continue;
            return new ConcreteEventGraphEdge(property.getURI());
        }

        throw new EventGraphException("Model does not contain \"" + literalLabel + "\" property");
    }

    public static boolean isParticipantInEdge(EventGraphEdge edge) {
        return edge.label().contains(Constants.ParticipantIn); /*"participant-in"*/
    }

    public static boolean isSubeventOfEdge(EventGraphEdge edge) {
        return edge.label().contains(Constants.SubEvent); /*"subevent-of"*/
    }

    public static EventGraphEdge createObjectPropertyEdge(OntModel model, String edgeLabel) throws EventGraphException {
        ObjectProperty property = null;
        for (String nsKey: model.getNsPrefixMap().keySet()) {
            property = model.getObjectProperty(model.getNsPrefixMap().get(nsKey) + edgeLabel);
            if (property == null) continue;
            return new ConcreteEventGraphEdge(property.getURI());
        }

        throw new EventGraphException("Model does not contain \"" + edgeLabel + "\" property");
    }

    private static class ConcreteEventGraphEdge extends EventGraphEdge {
        public ConcreteEventGraphEdge(String uri) {
            super(uri);
        }
    }
}
