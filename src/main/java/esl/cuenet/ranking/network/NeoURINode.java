package esl.cuenet.ranking.network;

import esl.cuenet.ranking.TypedEdge;
import esl.cuenet.ranking.URINode;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class NeoURINode implements URINode {

    private Node node = null;

    @Override
    public TypedEdge createEdge(URINode uriNode) {
        Relationship rel = this.node.createRelationshipTo(((NeoURINode)uriNode).node, NeoRelationships.BLANK);
        return new NeoTypedEdge(rel);
    }
}
