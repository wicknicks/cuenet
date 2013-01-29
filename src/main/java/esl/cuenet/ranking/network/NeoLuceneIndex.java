package esl.cuenet.ranking.network;

import esl.cuenet.ranking.TextIndex;
import esl.cuenet.ranking.URINode;
import org.neo4j.graphdb.Node;
//import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

public class NeoLuceneIndex implements TextIndex {

    private final Index<Node> nodeIndex;

    public NeoLuceneIndex(Index<Node> nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    @Override
    public URINode lookup(String key, Object value) {
        IndexHits<Node> hits = nodeIndex.get(key, value);
        if (hits.size() == 0) throw new RuntimeException("No nodes corresponding to key: " + key + " " + value);
        return NeoCache.getInstance().lookupNode(hits.getSingle());
    }

    @Override
    public void put(URINode node, String key, Object value) {
        //Transaction tx = nodeIndex.getGraphDatabase().beginTx();
        if (!(node instanceof NeoURINode)) throw new RuntimeException("cannot index nodes which are not NeoURI Nodes");
        nodeIndex.putIfAbsent( ((NeoURINode)node).node, key, value);
        //tx.success();
        //tx.finish();
    }

}
