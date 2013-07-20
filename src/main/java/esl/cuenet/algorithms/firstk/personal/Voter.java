package esl.cuenet.algorithms.firstk.personal;

import com.google.common.collect.Lists;
import esl.cuenet.algorithms.firstk.personal.accessor.Candidates;
import esl.cuenet.algorithms.firstk.personal.accessor.Source;

import java.util.*;

public class Voter {

    private HashMap<Candidates.CandidateReference, Integer> voteTable =
            new HashMap<Candidates.CandidateReference, Integer>();
    private Candidates candidates = Candidates.getInstance();
    private final int K = 10;

    public Voter() {
        Iterator<Candidates.CandidateReference> iter = candidates.candidateIterator();
        while (iter.hasNext()) voteTable.put(iter.next(), 0);

    }

    public List<Candidates.CandidateReference> vote(EventContextNetwork network, Source[] sources, Time time) {
        List<EventContextNetwork.ECNRef> _persons = network.getVotableEntities();
        List<Candidates.CandidateReference> votableCandidates = Lists.newArrayList();
        for (EventContextNetwork.ECNRef p: _persons) votableCandidates.add(network.getCandidateReference(p));

        List<Candidates.CandidateReference> references = Lists.newArrayList();
        List<EventContextNetwork> secondaries = Lists.newArrayList();

        for (Candidates.CandidateReference ref: votableCandidates) {
            for (Source source: sources) {
                List<Candidates.CandidateReference> p = source.knows(ref);
                if (p != null) references.addAll(p);
                List<EventContextNetwork> e = source.knowsAtTime(ref, time);
                if (e != null) secondaries.addAll(e);
            }
        }

        knows(references);
        knowsAtTime(secondaries);

        List<Map.Entry<Candidates.CandidateReference, Integer>> scores = sortScores();

//        System.out.println("----------------------------------------------------------");
//        System.out.println("                   VOTE TABLE                 ");
//        System.out.println("----------------------------------------------------------");

//        for (Map.Entry<Candidates.CandidateReference, Integer> entry: scores) {
//            System.out.println(entry.getValue() + " " +
//                    candidates.get(entry.getKey()).toStringKey(Candidates.NAME_KEY));
//        }

        List<Candidates.CandidateReference> topK = Lists.newArrayList();
        if (scores.size() == 0) return topK;

        int i=0;
        while (topK.size() < K && i < scores.size()) {
            Candidates.CandidateReference ref = scores.get(i).getKey();
            if ( !votableCandidates.contains(ref) ) {
                topK.add(ref);
            }
            i++;
        }
        return topK;
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
            public void visit(EventContextNetwork.Event event) { }

            @Override
            public void visit(EventContextNetwork.Person person) {
                increment(secondary.getCandidateReference(person.reference));
            }
        });
    }

    public List<Map.Entry<Candidates.CandidateReference, Integer>> sortScores() {

        Set<Map.Entry<Candidates.CandidateReference, Integer>> entrySet = voteTable.entrySet();

        PriorityQueue<Map.Entry<Candidates.CandidateReference, Integer>> ballot =
                new PriorityQueue<Map.Entry<Candidates.CandidateReference, Integer>>(entrySet.size(), new Comparator<Map.Entry<Candidates.CandidateReference, Integer>>() {

                    @Override
                    public int compare(Map.Entry<Candidates.CandidateReference, Integer> o1, Map.Entry<Candidates.CandidateReference, Integer> o2) {
                        return o2.getValue() - o1.getValue();
                    }

                });

        ballot.addAll(entrySet);

        List<Map.Entry<Candidates.CandidateReference, Integer>> sortedList = Lists.newArrayList();
        while ( !ballot.isEmpty() ) {
            Map.Entry<Candidates.CandidateReference, Integer> entry = ballot.remove();
            if (entry.getValue() > 0) sortedList.add(entry);
        }

        return sortedList;
    }



}

