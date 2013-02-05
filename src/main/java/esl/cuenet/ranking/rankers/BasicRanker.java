package esl.cuenet.ranking.rankers;

import esl.cuenet.ranking.*;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BasicRanker implements Ranker {

    private final EventEntityNetwork network;
    private final EntityBase entityBase;

    private HashMap<Long, Double> scoreMap = new HashMap<Long, Double>(100);
    private Logger logger = Logger.getLogger(BasicRanker.class);

    public BasicRanker(EventEntityNetwork network, EntityBase entityBase) {
        this.network = network;
        this.entityBase = entityBase;

        for (long id: this.entityBase) scoreMap.put(id, 0.0);
    }

    @Override
    public void assign(long nodeId, double score) {
        scoreMap.put(nodeId, score);
    }

    @Override
    public void compute(Iterable<PropagationFunction> functions) {
        logger.info(scoreMap.size() + " items in map.");
        Iterator<TypedEdge> iter = network.getEdgesIterator();

        int[] matches = new int[]{0, 0};

        int mx;
        while(iter.hasNext()) {
            TypedEdge edge = iter.next();
            mx = 0;
            for (PropagationFunction f: functions) {
                if (f.matchStartNode(edge.getStartNode()) &&
                        f.matchEdge(edge) &&
                        f.matchEndNode(edge.getEndNode())) matches[mx]++;
                mx++;
            }
        }

        logger.info("MATCHES = " + matches[0] + ", " + matches[1]);
    }

    @Override
    public Iterator<Map.Entry<URINode, Double>> results() {
        return null;
    }

}
