package esl.cuenet.algorithms.firstk.personal;

import com.google.common.collect.Lists;
import esl.cuenet.algorithms.firstk.personal.accessor.Candidates;
import esl.cuenet.algorithms.firstk.personal.accessor.Source;

import java.util.ArrayList;
import java.util.List;

public class Discoverer {

    private final EventContextNetwork network;
    private final Source[] sources;
    private final Location location;
    private final Time time;

    public Discoverer(EventContextNetwork network, Source[] sources, Time time, Location location) {
        this.network = network;
        this.sources = sources;
        this.time = time;
        this.location = location;
    }

    public void dnm () {

        final List<EventContextNetwork> secondaries = new ArrayList<EventContextNetwork>();
        network.visit(new EventContextNetwork.Visitor() {

            @Override
            public void visit(EventContextNetwork.Event event) {
                discover(event, secondaries);
            }

            @Override
            public void visit(EventContextNetwork.Person person) {
                discover(person, secondaries);
            }
        });

        boolean newInformation = false;
        if (secondaries.size() > 0)
            newInformation = merge(secondaries);

        if ( !newInformation ) vote();
    }

    private void vote() {
        List<EventContextNetwork.ECNRef> persons = network.getVotableEntities();

        List<Candidates.CandidateReference> references = Lists.newArrayList();
        List<EventContextNetwork> secondaries = Lists.newArrayList();

        Voter voter = new Voter();

        for (EventContextNetwork.ECNRef person: persons) {
            for (Source source: sources) {
                List<Candidates.CandidateReference> p = source.knows(network.getCandidateReference(person));
                if (p != null) references.addAll(p);
                List<EventContextNetwork> e = source.knowsAtTime(network.getCandidateReference(person), time);
                if (e != null) secondaries.addAll(e);
            }
        }

        voter.knows(references);
        voter.knowsAtTime(secondaries);

        voter.printScores();
    }

    private void discover(EventContextNetwork.Person person, List<EventContextNetwork> secondaries) {
        //network.getVotableCandidates();
        for (Source source: sources) {

        }
    }

    private void discover(EventContextNetwork.Event event, List<EventContextNetwork> secondaries) {
        for (Source source: sources) {

        }
    }

    private boolean merge(List<EventContextNetwork> secondaries) {
        return false;
    }

}
