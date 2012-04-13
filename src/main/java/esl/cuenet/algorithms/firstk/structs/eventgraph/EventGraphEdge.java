package esl.cuenet.algorithms.firstk.structs.eventgraph;

import esl.datastructures.graph.relationgraph.RelationGraphEdge;

import java.util.UUID;

public abstract class EventGraphEdge extends RelationGraphEdge implements Comparable<EventGraphEdge> {

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

}
