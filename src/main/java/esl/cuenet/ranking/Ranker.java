package esl.cuenet.ranking;

import java.util.Iterator;
import java.util.Map;

public interface Ranker {

    void assign(long nodeId, double score);

    void compute(Iterable<PropagationFunction> functions);

    Iterator<Map.Entry<URINode, Double>> results();

}
