package esl.cuenet.algorithms.firstk.personal.accessor;

import esl.cuenet.algorithms.firstk.personal.EventContextNetwork;
import esl.cuenet.algorithms.firstk.personal.Location;
import esl.cuenet.algorithms.firstk.personal.Time;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface Source {

    public List<EventContextNetwork> eventsContaining(Candidates.CandidateReference person, Time interval, Location location);

    public List<EventContextNetwork> participants(EventContextNetwork.Event event);

    public List<EventContextNetwork> subevents(EventContextNetwork.Event event);

    public List<Candidates.CandidateReference> knows (Candidates.CandidateReference person);

    public List<EventContextNetwork> knowsAtTime (Candidates.CandidateReference person, Time time);

    public void writeInstances(File instanceFile) throws IOException;
}
