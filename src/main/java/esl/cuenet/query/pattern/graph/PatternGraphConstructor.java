package esl.cuenet.query.pattern.graph;

import esl.cuenet.query.pattern.exceptions.PatternGraphException;

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

        patternGraph.add(node);
        graphStack.add(subEventPatternGraph);
    }

    public void startInterleaved() {
        PatternGraph patternGraph = graphStack.peek();

        PatternGraphNode node = new PatternGraphNode(PatternGraphNode.PatternGraphNodeType.INTERLEAVE_START);
        patternGraph.add(node);

        PatternGraph firstInterleaveGraphPath = node.createInterleavedGraph();
        graphStack.add(firstInterleaveGraphPath);
    }

    public void switchInterleavePath() {
        graphStack.pop();

        PatternGraph entryGraph = graphStack.peek();
        PatternGraphNode interleaveStartNode = entryGraph.getLast();
        if (interleaveStartNode.type() != PatternGraphNode.PatternGraphNodeType.INTERLEAVE_START)
            throw new PatternGraphException();

        PatternGraph firstInterleaveGraphPath = interleaveStartNode.createInterleavedGraph();
        graphStack.add(firstInterleaveGraphPath);
    }

    public void endInterleaved() {
        graphStack.pop();

        PatternGraph entryGraph = graphStack.peek();
        entryGraph.add(new PatternGraphNode(PatternGraphNode.PatternGraphNodeType.INTERLEAVE_END));
    }

    public void startUnion() {
        PatternGraph patternGraph = graphStack.peek();

        PatternGraphNode node = new PatternGraphNode(PatternGraphNode.PatternGraphNodeType.UNION_START);
        patternGraph.add(node);

        PatternGraph firstUnionGraphPath = node.createUnionGraph();
        graphStack.add(firstUnionGraphPath);
    }

    public void switchUnionPath() {
        graphStack.pop();

        PatternGraph entryGraph = graphStack.peek();
        PatternGraphNode unionStartNode = entryGraph.getLast();
        if (unionStartNode.type() != PatternGraphNode.PatternGraphNodeType.UNION_START)
            throw new PatternGraphException();

        PatternGraph firstUnionGraphPath = unionStartNode.createUnionGraph();
        graphStack.add(firstUnionGraphPath);
    }

    public void endUnion() {
        graphStack.pop();

        PatternGraph entryGraph = graphStack.peek();
        entryGraph.add(new PatternGraphNode(PatternGraphNode.PatternGraphNodeType.UNION_END));
    }

    public void add(String eventLabel) {
        PatternGraphNode node = new PatternGraphNode(eventLabel);
        PatternGraph patternGraph = graphStack.peek();
        patternGraph.add(node);
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
