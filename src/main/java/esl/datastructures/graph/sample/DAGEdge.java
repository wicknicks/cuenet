package esl.datastructures.graph.sample;

import esl.datastructures.graph.Edge;

public class DAGEdge implements Edge {

    protected String label = null;
    protected String name = null;

    public DAGEdge(String label) {
        this.label = label;
    }

    public DAGEdge(String label, String name) {
        this.label = label;
        this.name = name;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DAGEdge dagEdge = (DAGEdge) o;

        return !(label != null ? !label.equals(dagEdge.label) : dagEdge.label != null) &&
                !(name != null ? !name.equals(dagEdge.name) : dagEdge.name != null);
    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
