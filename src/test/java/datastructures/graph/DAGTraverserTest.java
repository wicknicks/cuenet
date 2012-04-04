package datastructures.graph;

import esl.datastructures.graph.*;
import esl.datastructures.graph.sample.DAG;
import esl.datastructures.graph.sample.DAGEdge;
import esl.datastructures.graph.sample.DAGNode;
import org.apache.log4j.Logger;
import org.junit.Test;
import test.TestBase;

public class DAGTraverserTest extends TestBase {

    private Logger logger = Logger.getLogger(DAGTraverserTest.class);

    public DAGTraverserTest() {
        super();
    }

    @Test
    public void createGraphAndDFSTraverseTest() {
        final Graph<DAGNode, DAGEdge> graph = new DAG();
        TraversalContext traversalContext = new TraversalContext();
        traversalContext.setCx("");


        DAGNode a = graph.getStartNode();
        DAGNode b = graph.createNode("B");
        DAGNode c = graph.createNode("C");
        DAGNode d = graph.createNode("D");
        DAGNode e = graph.createNode("E");
        DAGNode f = graph.createNode("F");
        DAGNode g = graph.createNode("G");

        graph.createEdge("l1", a, b);
        graph.createEdge("l2", a, c);
        graph.createEdge("l3", a, e);
        graph.createEdge("l4", b, d);
        graph.createEdge("l5", b, f);
        graph.createEdge("l6", c, g);
        graph.createEdge("l7", c, f);
        graph.createEdge("l8", e, f);

        DFSTraverser<DAGNode, DAGEdge> traverser = new DFSTraverser<DAGNode, DAGEdge>();
        traverser.setTraversalContext(traversalContext);
        traverser.setNodeVisitorCallback(new NodeVisitor() {
            @Override
            public void visit(Node node, TraversalContext context) {
                context.setCx(context.getCx() + " -> " + node.name());
                logger.info("[Visiting] " + node.name());
            }
        });
        traverser.setEdgeVisitorCallback(new EdgeVisitor() {
            @Override
            public void visit(Edge edge, TraversalContext context) {
                if (edge.label() != null) logger.info("[Traversing] " + edge.label());
                logger.info("[Traversing] Edge from " + graph.getOriginNode((DAGEdge) edge).name() + " to " + graph.getDestinationNode((DAGEdge) edge).name());
            }
        });

        traverser.start(graph);

        logger.info("Done " + traversalContext.getCx());
    }

}