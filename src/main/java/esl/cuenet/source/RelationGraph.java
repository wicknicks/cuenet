package esl.cuenet.source;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import esl.cuenet.mapper.tree.IParseTreeNode;
import esl.datastructures.graph.Edge;
import esl.datastructures.graph.Node;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class RelationGraph implements IRelationGraph {

    private List<RelationGraphNode> nodes = new ArrayList<RelationGraphNode>();
    private Logger logger = Logger.getLogger(RelationGraph.class);

    @Override
    public boolean containsClass(String name) {
        for (RelationGraphNode node: nodes)
            if (node.compare(name)) return true;

        return false;
    }

    @Override
    public Node getStartNode() {
        return null;
    }

    @Override
    public Node createNode(String name) {
        RelationGraphNode node = new RelationGraphNode();
        node.name = name;
        nodes.add(node);
        return node;
    }

    @Override
    public Node getNodeByName(String name) {
        for (RelationGraphNode node: nodes)
            if (node.compare(name)) return node;
        return null;
    }

    @Override
    public List<Edge> getOutgoingEdges(Node node) {
        assert (node instanceof RelationGraphNode);
        return ((RelationGraphNode)(node)).edges;
    }

    @Override
    public Edge createEdge(String label, Node n1, Node n2) {
        assert (n1 instanceof RelationGraphNode);
        assert (n2 instanceof RelationGraphNode);

        RelationGraphEdge edge = new RelationGraphEdge(label, n1, n2);
        ((RelationGraphNode)n1).addEdge(edge);

        return edge;
    }

    public class RelationGraphNode implements Node {

        String name;
        List<Edge> edges = new ArrayList<Edge>();

        @Override
        public String name() {
            return name;
        }

        public void addEdge(Edge edge) {
            edges.add(edge);
        }

        public List<Edge> getEdges() {
            return edges;
        }

        public boolean compare(String relNodeName) {
            return !(this.name == null || relNodeName == null) && this.name.compareTo(relNodeName) == 0;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }

    public class RelationGraphEdge implements Edge {

        private Node destination;
        private Node origin;
        private String label;

        public RelationGraphEdge(String label, Node n1, Node n2) {
            this.label = label;
            this.origin = n1;
            this.destination = n2;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public Node getOrigin() {
            return origin;
        }

        @Override
        public Node getDestination() {
            return destination;
        }
    }
}