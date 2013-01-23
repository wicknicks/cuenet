package esl.cuenet.ranking.network;

import esl.cuenet.ranking.TypedEdge;
import org.neo4j.graphdb.Relationship;

public class NeoTypedEdge implements TypedEdge {

    private Relationship relationship;

    public NeoTypedEdge(Relationship relationship) {
        this.relationship = relationship;
    }


}
