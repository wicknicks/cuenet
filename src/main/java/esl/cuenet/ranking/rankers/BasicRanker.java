package esl.cuenet.ranking.rankers;

import esl.cuenet.ranking.*;
import esl.cuenet.ranking.network.NeoEntityBase;
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

    private NodeEvaluator evaluator = new NodeEvaluator();
    private SpatioTemporalIndex stIndex = null;

    public BasicRanker(EventEntityNetwork network, EntityBase entityBase) {
        this.network = network;
        this.entityBase = entityBase;
        this.stIndex = network.stIndex(SpatioTemporalIndex.OCCURS_DURING_IX);

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

    private int iters  = 0;
    @Override
    public boolean canTerminate() {
        iters++;
        return (iters > 2);
    }

    @Override
    public void compute(PropagationFunction[] functions) {
        scoreUpdates.clear();
        logger.info("UpdatedQueue contains " + updatedQueue.size() + " elements.");
        Queue<Long> tmp = new LinkedList<Long>(updatedQueue);
        for (long uId: tmp) {
            URINode updateNode = network.getNodeById(uId);
            if (evaluator.isEntity(updateNode)) propagateAlongEntity(updateNode, functions);
            if (evaluator.isEvent(updateNode)) propagateAloneEvent(updateNode, functions);
        }

        for (long id: scoreUpdates.keySet()) {
            if (eventScoreMap.containsKey(id)) eventScoreMap.put(id, eventScoreMap.get(id) + scoreUpdates.get(id));
            if (entityScoreMap.containsKey(id)) entityScoreMap.put(id, entityScoreMap.get(id) + scoreUpdates.get(id));
            //else logger.info("Ouch");
        }

        int c = 0;
        updatedQueue.clear();
        for (Map.Entry<Long, Double> entry: scoreUpdates.entrySet()) {
            if (entry.getValue() > 0) {
                updatedQueue.add(entry.getKey());
                c++;
            }
        }

        logger.info("Touched: " + c + " nodes.");
    }

    private void propagateAloneEvent(URINode updateNode, PropagationFunction[] functions) {
        propagateToConnectedNodes(updateNode, functions);
        propagateToNeighboringEvents(updateNode, functions);
    }

    private void propagateToNeighboringEvents(URINode eventNode, PropagationFunction[] functions) {

    }

    private void propagateToConnectedNodes(URINode eventNode, PropagationFunction[] functions) {
        double score = eventScoreMap.get(eventNode.getId());
        for (TypedEdge edge: eventNode.getAllRelationships()) {
            URINode otherNode = (edge.getStartNode() == eventNode) ? edge.getEndNode() : edge.getStartNode();
            otherNode = unnest(otherNode);
            if (otherNode == null) continue;
            double val = propagate(eventNode, edge, otherNode, functions, score);
            updateScore(otherNode, val);
        }
    }

    private URINode unnest(URINode node) {
        for (TypedEdge edge: node.getAllRelationships()) {
            if ( !edge.hasProperty(OntProperties.TYPE) ) continue;
            if ( !OntProperties.IS_SAME_AS.equals(edge.getProperty(OntProperties.TYPE))) continue;
            return (edge.getStartNode() == node) ? edge.getEndNode() : edge.getStartNode();
        }
        return null;
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
        List<Map.Entry<Long, Double>> scores = new ArrayList<Map.Entry<Long, Double>>(entityScoreMap.entrySet());
        Collections.sort(scores, new Comparator<Map.Entry<Long, Double>>() {
            @Override
            public int compare(Map.Entry<Long, Double> o1, Map.Entry<Long, Double> o2) {
                double diff = o1.getValue() - o2.getValue();
                if (diff > 0) return -1;
                else if (diff < 0) return 1;
                return 0;
            }
        });

        for (int i=0; i<10; i++) {
            logger.info(i + ". ID = " + scores.get(i).getKey() + "; SCORE = " + scores.get(i).getValue());
            NeoEntityBase.printEntity(network.getNodeById(scores.get(i).getKey()), logger);
        }

        return null;
    }

}
