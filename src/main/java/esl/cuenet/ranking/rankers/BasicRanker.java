package esl.cuenet.ranking.rankers;

import esl.cuenet.ranking.*;
import esl.cuenet.ranking.network.OntProperties;
import org.apache.log4j.Logger;

import java.util.*;

public class BasicRanker implements Ranker {

    private final EventEntityNetwork network;
    private final EntityBase entityBase;
    private static final double _THRESHOLD = 0.25;

    private HashMap<Long, Double> entityScoreMap = new HashMap<Long, Double>(100);
    private HashMap<Long, Double> eventScoreMap = new HashMap<Long, Double>();
    private Logger logger = Logger.getLogger(BasicRanker.class);

    private HashMap<Long, Double> scoreUpdates = new HashMap<Long, Double>();
    private Queue<Long> updatedQueue = new LinkedList<Long>();

    public BasicRanker(EventEntityNetwork network, EntityBase entityBase) {
        this.network = network;
        this.entityBase = entityBase;

        for (long id: this.entityBase) entityScoreMap.put(id, 0.0);

        Iterator<URINode> eventsIter = network.getEventsIterator();
        while (eventsIter.hasNext()) eventScoreMap.put(eventsIter.next().getId(), 0.0);

        logger.info("Initialized BasicRanker with (" + entityScoreMap.size() + ", " + eventScoreMap.size() + ").");
    }

    @Override
    public void assign(long nodeId, double score) {
        entityScoreMap.put(nodeId, score);
        updatedQueue.add(nodeId);
    }

    @Override
    public void compute(PropagationFunction[] functions) {
        scoreUpdates.clear();

        Queue<Long> tmp = new LinkedList<Long>(updatedQueue);
        for (long uId: tmp) {
            URINode updateNode = network.getNodeById(uId);
            if (updateNode.hasProperty(EntityBase.TYPE) && EntityBase.ENTITY.equals(updateNode.getProperty(EntityBase.TYPE)))
                propagateAlongEntity(updateNode, functions);
        }

        for (long id: scoreUpdates.keySet()) {
            if (eventScoreMap.containsKey(id)) eventScoreMap.put(id, eventScoreMap.get(id) + scoreUpdates.get(id));
            if (entityScoreMap.containsKey(id)) entityScoreMap.put(id, entityScoreMap.get(id) + scoreUpdates.get(id));
        }

        int c = 0;
        for (double score: scoreUpdates.values()) if (score > 0) c++;

        logger.info("Touched: " + c + " nodes.");
    }

    private void propagateAlongEntity(URINode entityNode, PropagationFunction[] functions) {
        double score = entityScoreMap.get(entityNode.getId());
        for (TypedEdge edge: entityNode.getAllRelationships()) {
            if (edge.hasProperty(OntProperties.TYPE) && edge.getProperty(OntProperties.TYPE).equals(OntProperties.IS_SAME_AS))
            {
                URINode entityAliasNode = (edge.getStartNode() == entityNode) ? edge.getEndNode() : edge.getStartNode();
                for (TypedEdge aliasEdge: entityAliasNode.getAllRelationships()) {
                    URINode otherNode = (aliasEdge.getStartNode() == entityAliasNode) ? aliasEdge.getEndNode() : aliasEdge.getStartNode();
                    double val = propagate(entityNode, aliasEdge, otherNode, functions, score);
                    updateScore(otherNode, val);
                }
            }
        }
    }

    private void updateScore(URINode node, double update) {
        if (scoreUpdates.containsKey(node.getId())) update += scoreUpdates.get(node.getId());
        scoreUpdates.put(node.getId(), update);
    }

    private double propagate(URINode startNode, TypedEdge edge, URINode endNode, PropagationFunction[] functions, double startNodeScore) {
        double ret = 0;
        for (PropagationFunction function: functions) {
            if (function.matchStartNode(startNode) &&
                    function.matchEdge(edge) &&
                    function.matchEndNode(endNode)) {
                ret += function.propagate(startNode, edge, endNode, startNodeScore);
            }
        }
        return ret;
    }

    @Override
    public Iterator<Map.Entry<URINode, Double>> results() {
        return null;
    }

}
