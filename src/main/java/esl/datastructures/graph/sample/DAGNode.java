package esl.datastructures.graph.sample;

import esl.datastructures.graph.Edge;
import esl.datastructures.graph.Node;

import java.util.ArrayList;
import java.util.List;

public class DAGNode implements Node {

    private String name;

    public DAGNode(String name) {
        this.name = name;
    }

//    public void addEdge(Edge edge) {
//        edges.add(edge);
//    }
//
//    public List<Edge> getEdges() {
//        return edges;
//    }

    @Override
    public String name() {
        return name;
    }

    public int compareName(String n) {
        return name.compareTo(n);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DAGNode dagNode = (DAGNode) o;

        return !(name != null ? !name.equals(dagNode.name) : dagNode.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}