package esl.cuenet.ranking;

import java.util.Iterator;

public interface TextIndex {

    void construct();

    Iterator<URINode> lookup(String query);

}
