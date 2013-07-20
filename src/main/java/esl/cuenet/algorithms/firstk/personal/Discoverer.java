package esl.cuenet.algorithms.firstk.personal;

import com.google.common.collect.Lists;
import esl.cuenet.algorithms.firstk.personal.accessor.Candidates;
import esl.cuenet.algorithms.firstk.personal.accessor.Source;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class Discoverer {

    private final EventContextNetwork network;
    private final Source[] sources;
    private final Location location;
    private final Time time;

    private Candidates candidateSet = Candidates.getInstance();
    private Voter voter = new Voter();
    private Verifier verifier = Verifier.getInstance();

    private Logger logger = Logger.getLogger(Discoverer.class);

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

        if ( newInformation ) return;

        List<Candidates.CandidateReference> topKList = voter.vote(network, sources, time);

        logger.info("Top Ranked Candidates ====> ");
        for (Candidates.CandidateReference ref: topKList) {
            logger.info(verifier.verify(ref) + " " + candidateSet.get(ref).toStringKey(Candidates.NAME_KEY));
        }
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
