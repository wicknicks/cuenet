package esl.cuenet.ranking.network;

import esl.datastructures.graph.Node;

import java.util.UUID;

public class URINodes implements Node {

    private String uri;
    private String id;

    public URINodes(String uri) {
        this.uri = uri;
        this.id = UUID.randomUUID().toString();
    }

    public String id() {
        return id;
    }

    @Override
    public String name() {
        return uri;
    }

}
