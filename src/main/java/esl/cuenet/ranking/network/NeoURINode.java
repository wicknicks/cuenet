package esl.cuenet.ranking.network;

import esl.cuenet.ranking.TypedEdge;
import esl.cuenet.ranking.URINode;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.ArrayList;
import java.util.List;

public class NeoURINode implements URINode {

    Node node = null;
    private List<TypedEdge> outgoingEdges = new ArrayList<TypedEdge>(10);

    public NeoURINode(Node node) {
        this.node = node;
        for (Relationship rel: this.node.getRelationships(Direction.BOTH))
            outgoingEdges.add(new NeoTypedEdge(rel));
        NeoCache.getInstance().putNode(node, this);
    }

    @Override
    public long getId() {
        return node.getId();
    }

    @Override
    public TypedEdge createEdgeTo(URINode uriNode) {
        Relationship rel = this.node.createRelationshipTo(((NeoURINode)uriNode).node, NeoRelationships.BLANK);
        TypedEdge edge = new NeoTypedEdge(rel);
        outgoingEdges.add(edge);
        return edge;
    }

    @Override
    public boolean hasProperty(String key) {
        return node.hasProperty(key);
    }

    @Override
    public void setProperty(String key, Object value) {
        node.setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        if (node.hasProperty(key)) return node.getProperty(key);
        return null;
    }

    @Override
    public Iterable<TypedEdge> getAllRelationships() {
        return outgoingEdges;
    }

}
