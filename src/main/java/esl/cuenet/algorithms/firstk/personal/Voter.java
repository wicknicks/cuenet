package esl.cuenet.algorithms.firstk.personal;

import esl.cuenet.algorithms.firstk.personal.accessor.Candidates;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Voter {

    private HashMap<Candidates.CandidateReference, Integer> voteTable =
            new HashMap<Candidates.CandidateReference, Integer>();
    private Candidates candidates = Candidates.getInstance();

    public Voter() {
        Iterator<Candidates.CandidateReference> iter = candidates.candidateIterator();
        while (iter.hasNext()) voteTable.put(iter.next(), 0);

    }

    private void increment(Candidates.CandidateReference reference) {
        if ( !voteTable.containsKey(reference) )
            return;
        int score = voteTable.get(reference);
        voteTable.put(reference, score+1);
    }

    public void knows(List<Candidates.CandidateReference> references) {
        for (Candidates.CandidateReference ref: references) increment(ref);
    }

    public void knowsAtTime(List<EventContextNetwork> secondaries) {
        for (EventContextNetwork secondary: secondaries)
            visitAndUpdate(secondary);
    }

    private void visitAndUpdate(final EventContextNetwork secondary) {

        secondary.visit(new EventContextNetwork.Visitor() {

            @Override
            public void visit(EventContextNetwork.Event event) {

            }

            @Override
            public void visit(EventContextNetwork.Person person) {
                increment(secondary.getCandidateReference(person.reference));
            }
        });
    }

    public void printScores() {

        for (Map.Entry<Candidates.CandidateReference, Integer> entry: voteTable.entrySet()) {
            if (entry.getValue() > 0) {
                System.out.println(entry.getValue() + " " + candidates.get(entry.getKey()));
            }
        }
    }


}
