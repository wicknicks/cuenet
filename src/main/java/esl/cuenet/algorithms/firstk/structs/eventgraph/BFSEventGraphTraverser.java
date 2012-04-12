package esl.cuenet.algorithms.firstk.structs.eventgraph;

import esl.datastructures.graph.*;
import esl.datastructures.graph.relationgraph.RelationGraph;
import esl.datastructures.graph.relationgraph.RelationGraphEdge;
import esl.datastructures.graph.relationgraph.RelationGraphNode;

import java.util.*;

public class BFSEventGraphTraverser implements Traverser<RelationGraphNode, RelationGraphEdge> {

    private RelationGraph relationGraph = null;
    private NodeVisitor nodeVisitor = null;
    private EdgeVisitor edgeVisitor = null;
    private TraversalContext traversalContext = null;


    public BFSEventGraphTraverser(RelationGraph relationGraph) {
        this.relationGraph = relationGraph;
    }

    public void start() {
        if (edgeVisitor == null || nodeVisitor == null)
            throw new RuntimeException("Edge and Node Visitors not set before traversal");
        traverseDFS(relationGraph);
    }

    private void traverseDFS(RelationGraph relationGraph) {

        HashMap<String, Integer> seenMap = new HashMap<String, Integer>();
        Queue<Map.Entry<RelationGraphNode, RelationGraphEdge>> nodeQueue =
                new LinkedList<Map.Entry<RelationGraphNode, esl.datastructures.graph.relationgraph.RelationGraphEdge>>();
        nodeQueue.add(new AbstractMap.SimpleEntry<RelationGraphNode, RelationGraphEdge>(relationGraph.getStartNode(), null));

        boolean allSeen;
        while( nodeQueue.size() > 0) {
            Map.Entry<RelationGraphNode, RelationGraphEdge> entry = nodeQueue.peek();
            RelationGraphNode n = entry.getKey();

            if (seenMap.get(n.name()) == null) seenMap.put(n.name(), 0);

            if (seenMap.get(n.name()) != 0) {
                nodeQueue.remove();
                continue;
            }

            if (entry.getValue() != null) edgeVisitor.visit(entry.getValue(), traversalContext);
            nodeVisitor.visit(n, traversalContext);
            seenMap.put(n.name(), 1);

            if (relationGraph.getOutgoingEdges(n) == null || relationGraph.getOutgoingEdges(n).size() == 0) {
                nodeQueue.remove();
                continue;
            }

            allSeen = true;
            for (RelationGraphEdge e: relationGraph.getOutgoingEdges(n)) {
                RelationGraphNode dest = relationGraph.getDestinationNode(e);
                if ( seenMap.get(dest.name()) == null || seenMap.get(dest.name()) == 0 ) {
                    nodeQueue.add(new AbstractMap.SimpleEntry<RelationGraphNode, RelationGraphEdge>(dest, e));
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
    public void start(Graph<RelationGraphNode, RelationGraphEdge> relationGraphNodeRelationGraphEdgeGraph) {
        throw new RuntimeException("Use start() instead.");
    }

}
