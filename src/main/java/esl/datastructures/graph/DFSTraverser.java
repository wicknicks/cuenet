package esl.datastructures.graph;

import org.apache.log4j.Logger;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class DFSTraverser<N extends Node, E extends Edge> implements Traverser<N, E> {

    private Logger logger = Logger.getLogger(DFSTraverser.class);
    private NodeVisitor nodeVisitor = null;
    private EdgeVisitor edgeVisitor = null;
    private TraversalContext traversalContext = null;

    public void setTraversalContext (TraversalContext object) {
        this.traversalContext = object;
    }

    private void traverseDFS(Graph<N, E> graph) {

        HashMap<String, Integer> seenMap = new HashMap<String, Integer>();
        Stack<Map.Entry<N, E>> nodeStack = new Stack<Map.Entry<N, E>>();
        nodeStack.add(new AbstractMap.SimpleEntry<N, E>(graph.getStartNode(), null));

        boolean allSeen;
        while( !nodeStack.empty() ) {
            Map.Entry<N, E> entry = nodeStack.peek();
            N n = entry.getKey();

            if (seenMap.get(n.name()) == null) seenMap.put(n.name(), 0);

            if (seenMap.get(n.name()) != 0) {
                nodeStack.pop();
                continue;
            }

            if (entry.getValue() != null) edgeVisitor.visit(entry.getValue(), traversalContext);
            nodeVisitor.visit(n, traversalContext);
            seenMap.put(n.name(), 1);

            if (graph.getOutgoingEdges(n) == null || graph.getOutgoingEdges(n).size() == 0) {
                nodeStack.pop();
                continue;
            }

            allSeen = true;
            for (E e: graph.getOutgoingEdges(n)) {
                N dest = graph.getDestinationNode(e);
                if ( seenMap.get(dest.name()) == null || seenMap.get(dest.name()) == 0 ) {
                    nodeStack.push(new AbstractMap.SimpleEntry<N, E>(dest, e));
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
    public void start(Graph<N, E> graph) {
        if (edgeVisitor == null || nodeVisitor == null)
            throw new RuntimeException("Edge and Node Visitors not set before traversal");

        traverseDFS(graph);
    }

}
