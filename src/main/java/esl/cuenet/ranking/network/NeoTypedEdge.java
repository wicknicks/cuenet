package esl.cuenet.ranking.network;

import com.hp.hpl.jena.ontology.OntModel;
import esl.cuenet.ranking.TypedEdge;
import esl.cuenet.ranking.URINode;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

public class NeoTypedEdge implements TypedEdge {

    private Relationship relationship;
    private static OntModel model = null;

    public NeoTypedEdge(Relationship relationship) {
        this.relationship = relationship;
        NeoCache.getInstance().putEdge(relationship.getId(), this);
    }

    @Override
    public URINode getStartNode() {
        return NeoCache.getInstance().lookupNode(relationship.getStartNode().getId());
    }

    @Override
    public URINode getEndNode() {
        return NeoCache.getInstance().lookupNode(relationship.getEndNode().getId());
    }

    @Override
    public void setProperty(String key, Object value) {
        Transaction tx = relationship.getGraphDatabase().beginTx();
        relationship.setProperty(key, value);
        tx.success();
        tx.finish();
    }

    @Override
    public Object getProperty(String key) {
        return relationship.getProperty(key);
    }

}
