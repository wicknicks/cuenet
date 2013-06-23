package esl.cuenet.ranking;

import java.util.Iterator;
import java.util.Map;

public interface Ranker {

    void assign(long nodeId, double score);

    boolean canTerminate();

    void compute(PropagationFunction[] functions);

    Iterator<Map.Entry<URINode, Double>> results();

}
