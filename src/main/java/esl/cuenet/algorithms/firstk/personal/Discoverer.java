package esl.cuenet.algorithms.firstk.personal;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import esl.cuenet.algorithms.firstk.personal.accessor.Candidates;
import esl.cuenet.algorithms.firstk.personal.accessor.Source;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Discoverer {

    private final EventContextNetwork network;
    private final Source[] sources;
    private final Location location;
    private final Time time;

    private Candidates candidateSet = Candidates.getInstance();
    private Voter voter = new Voter();
    private Verifier verifier = Verifier.getInstance();

    private HashSet<Candidates.CandidateReference> verifiedEntities = Sets.newHashSet();

    private Logger logger = Logger.getLogger(Discoverer.class);

    public Discoverer(EventContextNetwork network, Source[] sources, Time time, Location location) {
        this.network = network;
        this.sources = sources;
        this.time = time;
        this.location = location;
    }

    int recursionsLimit = 10;

    public void dnm () {

        recursionsLimit--;
        if (recursionsLimit < 0) return;

        logger.info("=================  STARTING ITERATION ====================");

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

        if ( newInformation ) return;

        List<Candidates.CandidateReference> topKList = voter.vote(network, sources, time);

        logger.info("Top Ranked Candidates ====> ");
        for (Candidates.CandidateReference ref: topKList) {
            verify(ref);
            if (canTerminate()) break;
        }

        if (canTerminate()) return;

        dnm();
    }

    public void terminate() {
        logger.info("=================  TERMINATING DnM ====================");

        for (Candidates.CandidateReference ref: verifiedEntities)
            logger.info("Found: " + candidateSet.get(ref).toStringKey(Candidates.NAME_KEY));

        verifier.reportUnverified(verifiedEntities);
        logger.info("Verification Calls = " + verifier.numVerificationCalls());
        candidateSet.logistics(false);
    }

    public boolean canTerminate() {
        return verifiedEntities.size() >= verifier.annotationCount();
    }

    private void verify(Candidates.CandidateReference ref) {
        boolean v = verifier.verify(ref);
        if (v) {
            EventContextNetwork.ECNRef personRef = network.createPerson(ref);
            network.createPartiticipationEdge(network.getPhotoCaptureEventRef(), personRef);
            verifiedEntities.add(ref);
            logger.info("Verified True on " + candidateSet.get(ref).toStringKey(Candidates.NAME_KEY));
        }

    }


    private void discover(EventContextNetwork.Person person, List<EventContextNetwork> secondaries) {
        //network.getVotableCandidates();
        for (Source source: sources) {
            source.eventsContaining(network.getCandidateReference(person.reference), time, location);
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
