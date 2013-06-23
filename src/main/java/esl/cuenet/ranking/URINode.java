package esl.cuenet.ranking;

public interface URINode {

    long getId();

    TypedEdge createEdgeTo(URINode node);

    boolean hasProperty(String key);

    void setProperty(String key, Object value);

    Object getProperty(String key);

    Iterable<TypedEdge> getAllRelationships();

}
