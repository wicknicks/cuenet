package esl.cuenet.ranking.network;

import esl.datastructures.graph.Edge;

import java.util.UUID;

public class TypedEdges implements Edge {

    private final String uri;
    private final String id;

    public TypedEdges(String uri) {
        this.uri = uri;
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public String label() {
        return uri;
    }

    public String id() {
        return id;
    }

    @Override
    public String name() {
        throw new RuntimeException("Not implemented");
    }

}
