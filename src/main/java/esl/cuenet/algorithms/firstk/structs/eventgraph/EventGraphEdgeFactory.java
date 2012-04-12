package esl.cuenet.algorithms.firstk.structs.eventgraph;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;

public class EventGraphEdgeFactory {

    public static EventGraphEdge createSubeventEdge(OntModel model) throws EventGraphException {
        return createObjectPropertyEdge(model, "subevent-of");
    }

    public static EventGraphEdge createParticipatesInEdge(OntModel model) throws EventGraphException {
        return createObjectPropertyEdge(model, "participantes-in");
    }

    public static EventGraphEdge createLiteralEdge(OntModel model, String literalLabel) throws EventGraphException {
        DatatypeProperty property = null;
        for (String nsKey: model.getNsPrefixMap().keySet()) {
            String ns = model.getNsPrefixMap().get(nsKey);
            property = model.getDatatypeProperty(model.getNsPrefixMap().get(ns) + literalLabel);
            if (property == null) continue;
            return new ConcreteEventGraphEdge(property.getURI());
        }

        throw new EventGraphException("Model does not contain " + literalLabel + " property");
    }

    private static EventGraphEdge createObjectPropertyEdge(OntModel model, String edgeLabel) throws EventGraphException {
        ObjectProperty property = model.getObjectProperty(model.getNsPrefixMap().get("") + edgeLabel);
        if (property == null) throw new EventGraphException("Model does not contain " + edgeLabel + " property");
        return new ConcreteEventGraphEdge(property.getURI());
    }

    private static class ConcreteEventGraphEdge extends EventGraphEdge {
        public ConcreteEventGraphEdge(String uri) {
            super(uri);
        }
    }
}
