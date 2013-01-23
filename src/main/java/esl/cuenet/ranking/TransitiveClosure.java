package esl.cuenet.ranking;

import java.util.Iterator;

public interface TransitiveClosure {

    void compute();

    Iterator<TypedEdge> getRelations(URINode node);

}
