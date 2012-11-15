package esl.cuenet.query.pattern.graph;

public class PatternGraphNode {

    private MatchBuffer buffer = null;
    private PatternGraph subEventPatternGraph = null;
    private Quantifier quantifier = Quantifier.NONE;
    private String label = null;

    public PatternGraphNode(String label) {
        this.label = label;
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

    public Quantifier quantifier() {
        return quantifier;
    }

    public String toString() {
        return label + quantifier;
    }

    public PatternGraph createSubEventPatternGraph() {
        this.subEventPatternGraph = new PatternGraph();
        return this.subEventPatternGraph;
    }

    public PatternGraph getSubEventPatternGraph() {
        return subEventPatternGraph;
    }

}
