package esl.cuenet.ranking;

public interface URINode {

    TypedEdge createEdgeTo(URINode node);

    void setProperty(String key, Object value);

    Object getProperty(String key);

    Iterable<TypedEdge> getAllRelationships();

}
