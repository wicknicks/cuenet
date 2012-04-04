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

        DAGNode a = graph.getStartNode();
        DAGNode b = graph.createNode("B");
        DAGNode c = graph.createNode("C");
        DAGNode d = graph.createNode("D");
        DAGNode e = graph.createNode("E");
        DAGNode f = graph.createNode("F");
        DAGNode g = graph.createNode("G");

        graph.createEdge("l1", null,  a, b);
        graph.createEdge("l2", null, a, c);
        graph.createEdge("l3", null, a, e);
        graph.createEdge("l4", null, b, d);
        graph.createEdge("l5", null, b, f);
        graph.createEdge("l6", null, c, g);
        graph.createEdge("l7", null, c, f);
        graph.createEdge("l8", null, e, f);

        DFSTraverser<DAGNode, DAGEdge> traverser = new DFSTraverser<DAGNode, DAGEdge>();
        traverser.setNodeVisitorCallback(new NodeVisitor() {
            @Override
            public void visit(Node node) {
                logger.info("[Visiting] " + node.name());
            }
        });
        traverser.setEdgeVisitorCallback(new EdgeVisitor() {
            @Override
            public void visit(Edge edge) {
                if (edge.label() != null) logger.info("[Traversing] " + edge.label());
                logger.info("[Traversing] Edge from " + graph.getOriginNode((DAGEdge)edge).name() + " to " + graph.getDestinationNode((DAGEdge)edge).name());
            }
        });

        traverser.start(graph);

        logger.info("Done");
    }

}