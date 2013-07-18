package esl.cuenet.algorithms.firstk.personal;

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

        merge(secondaries);

    }

    private void discover(EventContextNetwork.Person person, List<EventContextNetwork> secondaries) {
        for (Source source: sources) {

        }
    }

    private void discover(EventContextNetwork.Event event, List<EventContextNetwork> secondaries) {
        for (Source source: sources) {

        }
    }

    private void merge(List<EventContextNetwork> secondaries) {

    }

}
