package esl.cuenet.algorithms.firstk.structs.eventgraph;

import esl.datastructures.graph.*;

import java.util.*;

public class BFSEventGraphTraverser implements Traverser<EventGraphNode, EventGraphEdge> {

    private EventGraph relationGraph = null;
    private NodeVisitor nodeVisitor = null;
    private EdgeVisitor edgeVisitor = null;
    private TraversalContext traversalContext = null;


    public BFSEventGraphTraverser(EventGraph relationGraph) {
        this.relationGraph = relationGraph;
    }

    public void start() {
        if (edgeVisitor == null || nodeVisitor == null)
            throw new RuntimeException("Edge and Node Visitors not set before traversal");
        traverseDFS(relationGraph);
    }

    private void traverseDFS(EventGraph eventGraph) {

        HashMap<String, Integer> seenMap = new HashMap<String, Integer>();
        Queue<Map.Entry<EventGraphNode, EventGraphEdge>> nodeQueue =
                new LinkedList<Map.Entry<EventGraphNode, EventGraphEdge>>();

        for (EventGraphNode node: eventGraph.getStartNodes())
            nodeQueue.add(new AbstractMap.SimpleEntry<EventGraphNode, EventGraphEdge>(node, null));

        boolean allSeen;
        while( nodeQueue.size() > 0) {
            Map.Entry<EventGraphNode, EventGraphEdge> entry = nodeQueue.peek();
            EventGraphNode n = entry.getKey();

            if (seenMap.get(n.name()) == null) seenMap.put(n.name(), 0);

            if (seenMap.get(n.name()) != 0) {
                nodeQueue.remove();
                continue;
            }

            if (entry.getValue() != null) edgeVisitor.visit(entry.getValue(), traversalContext);
            nodeVisitor.visit(n, traversalContext);
            seenMap.put(n.name(), 1);

            if (eventGraph.getEdges(n) == null || eventGraph.getEdges(n).size() == 0) {
                nodeQueue.remove();
                continue;
            }

            allSeen = true;
            for (EventGraphEdge e: eventGraph.getEdges(n)) {
                EventGraphNode dest = eventGraph.getDestination(e);
                if ( seenMap.get(dest.name()) == null || seenMap.get(dest.name()) == 0 ) {
                    nodeQueue.add(new AbstractMap.SimpleEntry<EventGraphNode, EventGraphEdge>(dest, e));
                    allSeen = false;
                }
            }

            if (allSeen) nodeQueue.remove();
        }
    }

    @Override
    public void setTraversalContext(TraversalContext context) {
        this.traversalContext = context;
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
    public void start(Graph<EventGraphNode, EventGraphEdge> graph) {
        throw new RuntimeException("Use start() instead.");
    }

}
