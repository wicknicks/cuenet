package esl.datastructures.graph.relationgraph;

import esl.datastructures.graph.Node;
import esl.datastructures.graph.sample.DAGNode;

public class RelationGraphNode extends DAGNode implements Node {

//        String name;
//        List<Edge> edges = new ArrayList<Edge>();
//
//        public RelationGraphNode(String name) {
//            super(name);
//        }
//
//        @Override
//        public String name() {
//            return name;
//        }
//
//        public void addEdge(Edge edge) {
//            edges.add(edge);
//        }
//
//        public List<Edge> getEdges() {
//            return edges;
//        }
//

    public boolean compare(String relNodeName) {
        return !(this.name() == null || relNodeName == null) && this.name().compareTo(relNodeName) == 0;
    }

    public RelationGraphNode(String name) {
        super(name);
    }
}