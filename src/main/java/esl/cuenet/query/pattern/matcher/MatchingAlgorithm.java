package esl.cuenet.query.pattern.matcher;

import esl.cuenet.query.pattern.exceptions.PatternGraphException;
import esl.cuenet.query.pattern.graph.EventStream;
import esl.cuenet.query.pattern.graph.EventStreamToken;
import esl.cuenet.query.pattern.graph.PatternGraph;
import esl.cuenet.query.pattern.graph.PatternGraphNode;

import java.util.HashSet;
import java.util.Stack;

public class MatchingAlgorithm {

    private ModelPatterns patterns = null;

    private enum MatchingResults {
        VIOLATION,
        MATCH,
        MISMATCH
    }

    public MatchingAlgorithm(ModelPatterns patterns) {
        this.patterns = patterns;
        for (PatternGraph p: patterns) p.buildIndex();
    }

    public FringeClasses find(EventStream stream) {
        FringeClasses results = new FringeClasses();
        int pos = 0;
        for (PatternGraph pattern: patterns) {
            if ( !containment(pattern, stream, pos) ) continue;
            if (violates(pattern, stream)) continue;

            pos = matches(pattern, stream);
            if (pos == pattern.size()) results.add(stream.getLast());
            else if (pos == 0)
                throw new PatternGraphException("Matching failed at position 0 -- Bad Pattern Graph?");
            else {
                for (String s: pattern.getEvents(pos)) results.add(s);
                results.add(stream.getLast());
            }
        }
        return results;
    }

    private int matches(PatternGraph pattern, EventStream stream) {
        int curIx = 0, tokIx = 0;
        PatternGraphNode current;
        EventStreamToken token;

        while (true) {
            current = pattern.get(curIx);
            token = stream.get(tokIx);
            MatchingResults match = match(current, token);
            if (match == MatchingResults.MATCH) {
                tokIx++;
                if (tokIx == stream.size()) break;
            } else if (match == MatchingResults.MISMATCH) {
                curIx++;
                if (curIx == pattern.size()) break;
            } else if (match == MatchingResults.VIOLATION) {
                return -1;
            }
        }
        return curIx;
    }

    private MatchingResults match(PatternGraphNode n, EventStreamToken token) {
        Stack<PatternGraphNode> dfsStack = new Stack<PatternGraphNode>();

        EventStreamToken sub = token;
        dfsStack.push(n);

        while ( !dfsStack.empty() ) {
            PatternGraphNode node = dfsStack.pop();

            // VISIT NODE
            if (node.label().equals(sub.getOntClass())) {
                /* check violation */

                if (sub.getSubEventToken() == null) return MatchingResults.MATCH;
                else sub = sub.getSubEventToken();
            }

            if (node.getSubEventPatternGraph() != null) {
                for (int i=node.getSubEventPatternGraph().size()-1; i>=0; i--)
                    dfsStack.push(node.getSubEventPatternGraph().get(i));
            }
        }

        return MatchingResults.MISMATCH;
    }

    private boolean containment(PatternGraph pattern, EventStream stream, int start) {
        return true;
    }

    private boolean violates(PatternGraph pattern, EventStream stream) {
        return false;
    }

}
