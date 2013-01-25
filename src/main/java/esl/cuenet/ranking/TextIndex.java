package esl.cuenet.ranking;

public interface TextIndex {

    URINode lookup(String key, Object value);

    void put(URINode node, String key, Object value);
}
