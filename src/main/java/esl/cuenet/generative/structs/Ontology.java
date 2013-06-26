package esl.cuenet.generative.structs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Ontology {

    private final int eventCount;

    private HashMap<Integer, SubeventStructure> index = new HashMap<Integer, SubeventStructure>();
    private SubeventStructure current = null;

    public Ontology(int eventCount) {
        this.eventCount = eventCount;
    }

    public int getEventCount() {
        return eventCount;
    }

    public void startTree(int id) {
        // System.out.println("Starting " + id);
        current = new SubeventStructure(id);
        index.put(id, current);
    }

    public void addEdges(int start, int[] end) {
        // System.out.println(start + " " + Arrays.toString(end));
        List<Integer> subeventList;
        if (current.edges.containsKey(start)) subeventList = current.edges.get(start);
        else {
            subeventList = new ArrayList<Integer>();
            current.edges.put(start, subeventList);
        }

        for (int e: end) subeventList.add(e);
    }

    private class SubeventStructure {
        final int eventId;
        HashMap<Integer, List<Integer>> edges = new HashMap<Integer, List<Integer>>();

        public SubeventStructure(int eid) {
            eventId = eid;
        }
    }

}
