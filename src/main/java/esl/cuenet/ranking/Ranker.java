package esl.cuenet.ranking;

import java.util.Iterator;
import java.util.Map;

public interface Ranker {

    Iterator<Map.Entry<URINode, Double>> results();

}
