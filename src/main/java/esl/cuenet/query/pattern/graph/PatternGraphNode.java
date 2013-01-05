package esl.cuenet.query.pattern.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PatternGraphNode {

    private PatternGraph subEventPatternGraph = null;
    private List<PatternGraph> interleavePatternGraphs = new ArrayList<PatternGraph>();
    private List<PatternGraph> unionPatternGraphs = new ArrayList<PatternGraph>();

    private Quantifier quantifier = Quantifier.NONE;
    private String label = null;
    private MatchBuffer buffer = null;

    private PatternGraphNodeType nodeType;
    private Set<String> classIndex = new HashSet<String>();

    public void collectAllClasses(List<String> classes) {
        if (nodeType != PatternGraphNodeType.REGULAR) return;

        if (label != null) classes.add(label);
        if (subEventPatternGraph != null)
            for (PatternGraphNode node: subEventPatternGraph) {
                node.collectAllClasses(classes);
            }

        for (PatternGraph interleaved: interleavePatternGraphs)
            for (PatternGraphNode node: interleaved) {
                node.collectAllClasses(classes);
            }

        for (PatternGraph union: unionPatternGraphs)
            for (PatternGraphNode node: union) {
                node.collectAllClasses(classes);
            }

        for (int i=0; i<classes.size(); i++) classIndex.addAll(classes);
    }

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
        this.nodeType = PatternGraphNodeType.REGULAR;
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
        this.subEventPatternGraph.setSuperEvent(this);
        return this.subEventPatternGraph;
    }

    public boolean contains(String ontClass) {
        return classIndex.contains(ontClass);
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
