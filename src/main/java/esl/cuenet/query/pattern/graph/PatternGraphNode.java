package esl.cuenet.query.pattern.graph;

public class PatternGraphNode {

    private MatchBuffer buffer = null;
    private PatternGraph subEventPatternGraph = null;
    private Quantifier quantifier = Quantifier.NONE;

    public PatternGraphNode(MatchBuffer buffer) {
        this.buffer = buffer;
    }

    public PatternGraphNode(MatchBuffer buffer, Quantifier quantifier) {
        this.buffer = buffer;
        this.quantifier = quantifier;
    }

    public void addToBuffer(EventStreamToken token) {
        buffer.add(token);
    }

    public Quantifier quantifier() {
        return quantifier;
    }

    public PatternGraph createSubEventPatternGraph() {
        this.subEventPatternGraph = new PatternGraph();
        return this.subEventPatternGraph;
    }

    public PatternGraph getSubEventPatternGraph() {
        return subEventPatternGraph;
    }

}
