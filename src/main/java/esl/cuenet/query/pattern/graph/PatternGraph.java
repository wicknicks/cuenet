package esl.cuenet.query.pattern.graph;

import java.util.*;

public class PatternGraph extends LinkedList<PatternGraphNode> {

    private Set<String> classIndex = new HashSet<String>();
    private PatternGraphNode superEvent = null;

    public void setSuperEvent(PatternGraphNode superEvent) {
        this.superEvent = superEvent;
    }

    public PatternGraphNode getSuperEvent() {
        return superEvent;
    }

    public List<String> getEvents(int pos) {
        PatternGraphNode node = get(pos);
        List<String> collectibles = new ArrayList<String>();
        node.collectAllClasses(collectibles);
        return collectibles;
    }

    public void buildIndex() {
        for (int i=0; i<size(); i++) classIndex.addAll(getEvents(i));
    }
}
