package esl.cuenet.algorithms.firstk.personal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import esl.cuenet.algorithms.firstk.personal.accessor.Candidates;
import esl.cuenet.algorithms.firstk.personal.accessor.Source;
import org.apache.log4j.Logger;

import java.util.*;

public class Voter {

    private HashMap<Candidates.CandidateReference, Integer> voteTable =
            new HashMap<Candidates.CandidateReference, Integer>();
    private Candidates candidates = Candidates.getInstance();
    private final int K = 10;

    private Logger logger = Logger.getLogger(Voter.class);

    public Voter() {
        zeros();
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
        HashMap<Candidates.CandidateReference, Double> knowsScores = normCopyScores();

        zeros();

        knowsAtTime(secondaries);
        HashMap<Candidates.CandidateReference, Double> knowsAtTimesScores = normCopyScores();

        return combine(votableCandidates, knowsScores, knowsAtTimesScores);
    }

    private List<Candidates.CandidateReference> combine(List<Candidates.CandidateReference> votableCandidates,
                                                        Map<Candidates.CandidateReference, Double> knowsScoresMap,
                                                        Map<Candidates.CandidateReference, Double> knowsAtTimesMap) {

        final double emailWeight = 0.5;
        final double locationWeight = 0.5;

        HashMap<Candidates.CandidateReference, Double> scoresMap = Maps.newHashMap();

        for (Candidates.CandidateReference key: knowsScoresMap.keySet()) {
            scoresMap.put( key,
                    knowsScoresMap.get(key) * locationWeight + knowsAtTimesMap.get(key) * emailWeight);
        }

        List<Candidates.CandidateReference> topK = Lists.newArrayList();
        if (scoresMap.size() == 0) return topK;

        PriorityQueue<Map.Entry<Candidates.CandidateReference, Double>> ballot =
                new PriorityQueue<Map.Entry<Candidates.CandidateReference, Double>>(scoresMap.size(),
                        new Comparator<Map.Entry<Candidates.CandidateReference, Double>>() {
                            @Override
                            public int compare(Map.Entry<Candidates.CandidateReference, Double> o1,
                                               Map.Entry<Candidates.CandidateReference, Double> o2) {
                                return o2.getValue().compareTo(o1.getValue());
                            }

                        });

        ballot.addAll(scoresMap.entrySet());

        List<Map.Entry<Candidates.CandidateReference, Double>> scores = Lists.newArrayList();
        while ( !ballot.isEmpty() ) {
            Map.Entry<Candidates.CandidateReference, Double> entry = ballot.remove();
            if (entry.getValue() > 0) scores.add(entry);
        }

        int i=0;
        while (topK.size() < K && i < scores.size()) {
            Candidates.CandidateReference ref = scores.get(i).getKey();
            if ( !votableCandidates.contains(ref) ) {
                topK.add(ref);
            }
            i++;
        }

        print(scores);

        return topK;
    }

    private void print(List<Map.Entry<Candidates.CandidateReference, Double>> scores) {
        logger.info("----------------------------------------------------------");
        logger.info("                   VOTE TABLE                 ");
        logger.info("----------------------------------------------------------");

        int ix = 0;
        for (Map.Entry<Candidates.CandidateReference, Double> entry: scores) {
            logger.info(entry.getValue() + " " +
                    candidates.get(entry.getKey()).toStringKey(Candidates.NAME_KEY));
            if (ix++ > K) break;
        }
    }

    private void increment(Candidates.CandidateReference reference) {
        if ( !voteTable.containsKey(reference) )
            return;
        int score = voteTable.get(reference);
        voteTable.put(reference, score + 1);
    }

    public void knows(List<Candidates.CandidateReference> references) {
        for (Candidates.CandidateReference ref: references) increment(ref);
    }

    private void zeros() {
        Iterator<Candidates.CandidateReference> iter = candidates.candidateIterator();
        while (iter.hasNext()) voteTable.put(iter.next(), 0);
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

    private HashMap<Candidates.CandidateReference, Double> normCopyScores() {
        int max = Collections.max(voteTable.values());
        HashMap<Candidates.CandidateReference, Double> t = Maps.newHashMap();
        for ( Map.Entry<Candidates.CandidateReference, Integer> entry: voteTable.entrySet()) {
            t.put(entry.getKey(), (double) entry.getValue()/max);
        }
        return t;
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
