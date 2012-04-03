package datastructures.graph;

import esl.datastructures.graph.Edge;
import esl.datastructures.graph.EdgeVisitor;
import esl.datastructures.graph.Node;
import esl.datastructures.graph.NodeVisitor;
import esl.datastructures.graph.sample.DAG;
import esl.datastructures.graph.DAGTraverser;
import org.apache.log4j.Logger;
import org.junit.Test;
import test.TestBase;

import java.util.HashMap;

public class DAGTraverserTest extends TestBase {

    private Logger logger = Logger.getLogger(DAGTraverserTest.class);

    public DAGTraverserTest() {
        super();
    }

    @Test
    public void createGraphAndDFSTraverseTest() {
        DAG graph = new DAG();
        HashMap<String, Node> nodeMap = new HashMap<String, Node>();

        Node a = graph.getStartNode();
        Node b = graph.createNode("B");
        Node c = graph.createNode("C");
        Node d = graph.createNode("D");
        Node e = graph.createNode("E");
        Node f = graph.createNode("F");
        Node g = graph.createNode("G");

        graph.createEdge("l1", a, b);
        graph.createEdge("l2", a, c);
        graph.createEdge("l3", a, e);
        graph.createEdge("l4", b, d);
        graph.createEdge("l5", b, f);
        graph.createEdge("l6", c, g);
        graph.createEdge("l7", c, f);
        graph.createEdge("l8", e, f);

        DAGTraverser traverser = new DAGTraverser();
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
                logger.info("[Traversing] Edge from " + edge.getOrigin().name() + " to " + edge.getDestination().name());
            }
        });
        traverser.start(graph);

        logger.info("Done");
    }

}