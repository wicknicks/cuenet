package esl.cuenet.ranking.network;

import esl.cuenet.ranking.TypedEdge;
import esl.cuenet.ranking.URINode;
import org.neo4j.graphdb.Relationship;

public class NeoTypedEdge implements TypedEdge {

    private Relationship relationship;

    public NeoTypedEdge(Relationship relationship) {
        this.relationship = relationship;
        NeoCache.getInstance().putEdge(relationship.getId(), this);
    }

    @Override
    public URINode getStartNode() {
        return NeoCache.getInstance().lookupNode(relationship.getStartNode());
    }

    @Override
    public URINode getEndNode() {
        return NeoCache.getInstance().lookupNode(relationship.getEndNode());
    }

    @Override
    public void setProperty(String key, Object value) {
        relationship.setProperty(key, value);
    }

    @Override
    public boolean hasProperty(String key) {
        return relationship.hasProperty(key);
    }

    @Override
    public Object getProperty(String key) {
        return relationship.getProperty(key);
    }

}
