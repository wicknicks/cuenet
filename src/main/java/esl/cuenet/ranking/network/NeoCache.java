package esl.cuenet.ranking.network;

import esl.cuenet.ranking.TypedEdge;
import esl.cuenet.ranking.URINode;
import org.neo4j.graphdb.Node;

import java.util.HashMap;

public class NeoCache {

    private static NeoCache cache = new NeoCache();

    private HashMap<Long, URINode> uriNodeMap = new HashMap<Long, URINode>(100);
    private HashMap<Long, TypedEdge> edgeMap = new HashMap<Long, TypedEdge>(100);

    private HashMap<Long, Node> neoNodeMap = new HashMap<Long, Node>(100);

    public static NeoCache getInstance() {
        return cache;
    }

    private NeoCache() {

    }

    public void putNode(Node _node, URINode _uriNode) {
        uriNodeMap.put(_node.getId(), _uriNode);
        neoNodeMap.put(_node.getId(), _node);
    }

    public void putEdge(long id, TypedEdge r) {
        edgeMap.put(id, r);
    }

    public URINode lookupNode(long id) {
        return uriNodeMap.get(id);
    }

    public URINode lookupNode(Node _node) {
        if ( !uriNodeMap.containsKey(_node.getId()) ) {
            putNode(_node, new NeoURINode(_node));
        }
        return lookupNode(_node.getId());
    }

    public TypedEdge lookupEdge(long id) {
        return edgeMap.get(id);
    }

}
