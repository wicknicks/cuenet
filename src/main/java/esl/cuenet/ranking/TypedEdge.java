package esl.cuenet.ranking;

public interface TypedEdge {

    public URINode getStartNode();

    public URINode getEndNode();

    void setProperty(String key, Object value);

    Object getProperty(String key);

}
