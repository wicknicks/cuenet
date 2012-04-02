package esl.datastructures.graph;

import org.apache.log4j.Logger;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class DAGTraverser implements Traverser {

    private Logger logger = Logger.getLogger(DAGTraverser.class);
    private NodeVisitor nodeVisitor = null;
    private EdgeVisitor edgeVisitor = null;

    private void traverseDFS(Graph graph) {

        HashMap<String, Integer> seenMap = new HashMap<String, Integer>();
        Stack<Map.Entry<Node, Edge>> nodeStack = new Stack<Map.Entry<Node, Edge>>();
        nodeStack.add(new AbstractMap.SimpleEntry<Node, Edge>(graph.getStartNode(), null));

        boolean allSeen;
        while( !nodeStack.empty() ) {
            Map.Entry<Node, Edge> entry = nodeStack.peek();
            Node n = entry.getKey();

            if (seenMap.get(n.name()) == null) seenMap.put(n.name(), 0);

            if (seenMap.get(n.name()) != 0) {
                nodeStack.pop();
                continue;
            }

            if (entry.getValue() != null) edgeVisitor.visit(entry.getValue());
            nodeVisitor.visit(n);
            seenMap.put(n.name(), 1);

            if (graph.getOutgoingEdges(n) == null || graph.getOutgoingEdges(n).size() == 0) {
                nodeStack.pop();
                continue;
            }

            allSeen = true;
            for (Edge e: graph.getOutgoingEdges(n)) {
                Node dest = e.getDestination();
                if ( seenMap.get(dest.name()) == null || seenMap.get(dest.name()) == 0 ) {
                    nodeStack.push(new AbstractMap.SimpleEntry<Node, Edge>(dest, e));
                    allSeen = false;
                }
            }

            if (allSeen) nodeStack.pop();
        }
    }

    @Override
    public void setNodeVisitorCallback(NodeVisitor visitorCallback) {
        this.nodeVisitor = visitorCallback;
    }

    @Override
    public void setEdgeVisitorCallback(EdgeVisitor visitorCallback) {
        this.edgeVisitor = visitorCallback;
    }

    @Override
    public void start(Graph graph) {
        if (edgeVisitor == null || nodeVisitor == null)
            throw new RuntimeException("Edge and Node Visitors not set before traversal");

        traverseDFS(graph);
    }
}
