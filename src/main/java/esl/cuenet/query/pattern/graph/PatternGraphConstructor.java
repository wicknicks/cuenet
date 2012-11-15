package esl.cuenet.query.pattern.graph;

import java.util.Stack;

public class PatternGraphConstructor {

    private Stack<PatternGraph> graphStack = new Stack<PatternGraph>();

    public PatternGraphConstructor() {
        PatternGraph patternGraph = new PatternGraph();
        graphStack.add(patternGraph);
    }

    public PatternGraph getGraph() {
        if (graphStack.size() != 1) throw new PatternGraphException("Parsing Not Completed -- Expecting SubEvent Stop");
        return graphStack.peek();
    }

    public void startSubEventPattern(String superEventLabel) {
        if (graphStack.size() < 1) throw new PatternGraphException("GraphStack Underflow");

        PatternGraph patternGraph = graphStack.peek();

        PatternGraphNode node = new PatternGraphNode(superEventLabel);
        PatternGraph subEventPatternGraph = node.createSubEventPatternGraph();

        patternGraph.followedByNodes.add(node);
        graphStack.add(subEventPatternGraph);
    }

    public void add(String eventLabel) {
        PatternGraphNode node = new PatternGraphNode(eventLabel);
        PatternGraph patternGraph = graphStack.peek();
        patternGraph.followedByNodes.add(node);
    }

    public void endSubEventPattern() {
        if (graphStack.size() == 0) throw new PatternGraphException("Incorrect subevent pattern termination");
        graphStack.pop();
    }

    public void stop() {
        if (graphStack.size() != 1) throw new PatternGraphException("Incorrect pattern termination");
        System.out.println("Completed");
    }
}
