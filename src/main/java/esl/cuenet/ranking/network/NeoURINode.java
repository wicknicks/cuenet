package esl.cuenet.ranking.network;

import esl.cuenet.ranking.TypedEdge;
import esl.cuenet.ranking.URINode;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.List;

public class NeoURINode implements URINode {

    Node node = null;
    private List<TypedEdge> outgoingEdges = new ArrayList<TypedEdge>(10);

    public NeoURINode(Node node) {
        this.node = node;
        NeoCache.getInstance().putNode(node.getId(), this);
    }

    @Override
    public long getId() {
        return node.getId();
    }

    @Override
    public TypedEdge createEdgeTo(URINode uriNode) {
        Transaction tx = node.getGraphDatabase().beginTx();
        Relationship rel = this.node.createRelationshipTo(((NeoURINode)uriNode).node, NeoRelationships.BLANK);
        TypedEdge edge = new NeoTypedEdge(rel);
        outgoingEdges.add(edge);
        tx.success();
        tx.finish();
        return edge;
    }

    @Override
    public void setProperty(String key, Object value) {
        Transaction tx = node.getGraphDatabase().beginTx();
        node.setProperty(key, value);
        tx.success();
        tx.finish();
    }

    @Override
    public Object getProperty(String key) {
        return node.getProperty(key);
    }

    @Override
    public Iterable<TypedEdge> getAllRelationships() {
        return outgoingEdges;
    }

}
