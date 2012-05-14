package esl.cuenet.algorithms.firstk.impl;

import com.hp.hpl.jena.ontology.Individual;

import java.util.HashMap;
import java.util.Iterator;

public class CandidateVotingTable<E> {

    private String name;
    private HashMap<E, Score<E>> candidateTable = new HashMap<E, Score<E>>();

    public CandidateVotingTable(String name) {
        this.name = name;
    }

    public boolean contains(E candidate) {
        return candidateTable.get(candidate) != null;
    }

    public void addToCandidateTable(E candidate, Individual individual) {
        candidateTable.put(candidate, new Score<E>(individual));
    }

    public void updateScore(E candidateRef, int delta) {
        Score<E> scoreArray = candidateTable.get(candidateRef);
        if (scoreArray == null) throw new RuntimeException("Candiate not found: " + candidateRef);
        scoreArray.scores += delta;
    }

    public Score<E> getScore(E candidateRef) {
        return candidateTable.get(candidateRef);
    }

    public Iterator<E> iterator() {
        return candidateTable.keySet().iterator();
    }

    public String toString() {
        return name + " candidate table, with " + candidateTable.size() + " entries.";
    }

}
