package esl.cuenet.query.pattern.graph;

import java.util.ArrayList;
import java.util.List;

public class PatternGraphNode {

    private MatchBuffer buffer = null;
    private PatternGraph subEventPatternGraph = null;
    private List<PatternGraph> interleavePatternGraphs = new ArrayList<PatternGraph>();
    private List<PatternGraph> unionPatternGraphs = new ArrayList<PatternGraph>();
    private Quantifier quantifier = Quantifier.NONE;
    private String label = null;
    private PatternGraphNodeType nodeType;

    public static enum PatternGraphNodeType {
        REGULAR(""),
        INTERLEAVE_START("I(S)"),
        INTERLEAVE_END("I(E)"),
        UNION_START("U(S)"),
        UNION_END("U(E)");

        private String desc;

        PatternGraphNodeType(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return desc;
        }
    }

    public PatternGraphNode(String label) {
        this.label = label;
        this.nodeType = PatternGraphNodeType.REGULAR;
    }

    public PatternGraphNode(PatternGraphNodeType type) {
        this.nodeType = type;
        this.label = null;
    }

    public PatternGraphNode(String label, Quantifier quantifier) {
        this.label = label;
        this.quantifier = quantifier;
    }

    public void addToBuffer(EventStreamToken token) {
        buffer.add(token);
    }

    public String label() {
        return label;
    }

    public PatternGraph createInterleavedGraph() {
        PatternGraph iGraph = new PatternGraph();
        this.interleavePatternGraphs.add(iGraph);
        return iGraph;
    }

    public PatternGraph createUnionGraph() {
        PatternGraph uGraph = new PatternGraph();
        this.unionPatternGraphs.add(uGraph);
        return uGraph;
    }

    public Quantifier quantifier() {
        return quantifier;
    }

    public PatternGraphNodeType type() {
        return nodeType;
    }

    public String toString() {
        String s;
        if (label != null) s = label;
        else if (nodeType != PatternGraphNodeType.REGULAR) s = nodeType.toString();
        else s = "";
        return s;
    }

    public PatternGraph createSubEventPatternGraph() {
        this.subEventPatternGraph = new PatternGraph();
        return this.subEventPatternGraph;
    }

    public PatternGraph getSubEventPatternGraph() {
        return subEventPatternGraph;
    }

    public List<PatternGraph> getInterleavePatternGraphs() {
        return interleavePatternGraphs;
    }

    public List<PatternGraph> getUnionPatternGraphs() {
        return unionPatternGraphs;
    }
}
