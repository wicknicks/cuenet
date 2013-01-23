package esl.cuenet.ranking.network;

import esl.cuenet.ranking.TypedEdge;
import esl.cuenet.ranking.URINode;

import java.util.HashMap;

public class NeoCache {

    private static NeoCache cache = new NeoCache();

    private HashMap<Long, URINode> nodeMap = new HashMap<Long, URINode>(100);
    private HashMap<Long, TypedEdge> edgeMap = new HashMap<Long, TypedEdge>(100);

    public static NeoCache getInstance() {
        return cache;
    }

    private NeoCache() {

    }

    public void putNode(long id, URINode n) {
        nodeMap.put(id, n);
    }

    public void putEdge(long id, TypedEdge r) {
        edgeMap.put(id, r);
    }

    public URINode lookupNode(long id) {
        return nodeMap.get(id);
    }

    public TypedEdge lookupEdge(long id) {
        return edgeMap.get(id);
    }

}
