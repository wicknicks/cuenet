package esl.cuenet.ranking;

public interface TypedEdge {

    public URINode getStartNode();

    public URINode getEndNode();

    void setProperty(String key, Object value);

    boolean hasProperty(String key);

    Object getProperty(String key);

}
