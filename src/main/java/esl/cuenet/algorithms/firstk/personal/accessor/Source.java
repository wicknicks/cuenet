package esl.cuenet.algorithms.firstk.personal.accessor;

import esl.cuenet.algorithms.firstk.personal.EventContextNetwork;
import esl.cuenet.algorithms.firstk.personal.Location;
import esl.cuenet.algorithms.firstk.personal.Time;

import java.util.List;

public interface Source {

    public List<EventContextNetwork> eventsContaining(EventContextNetwork.Person person, Time interval, Location location);

    public List<EventContextNetwork> participants(EventContextNetwork.Event event);

    public List<EventContextNetwork> subevents(EventContextNetwork.Event event);

    public List<EventContextNetwork.Person> knows (EventContextNetwork.Person person);

    public List<EventContextNetwork.Person> knowsAtTime (EventContextNetwork.Person person, Time time);
}
