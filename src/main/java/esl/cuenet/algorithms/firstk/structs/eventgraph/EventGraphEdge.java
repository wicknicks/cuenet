package esl.cuenet.algorithms.firstk.structs.eventgraph;

import com.hp.hpl.jena.rdf.model.Property;
import esl.datastructures.graph.relationgraph.RelationGraphEdge;

import java.util.UUID;

public abstract class EventGraphEdge extends RelationGraphEdge implements Comparable<EventGraphEdge> {

    private Property property = null;

    public EventGraphEdge(String uri) {
        super(uri, "cnid:" + UUID.randomUUID().toString());
    }

    @Override
    public int compareTo(EventGraphEdge o) {
        return this.label().compareTo(o.label());
    }

    public String uri() {
        return label();
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public Property getProperty() {
        return property;
    }
}
