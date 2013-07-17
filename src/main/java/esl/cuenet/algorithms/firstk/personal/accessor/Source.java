package esl.cuenet.algorithms.firstk.personal.accessor;

import esl.cuenet.algorithms.firstk.personal.Location;
import esl.cuenet.algorithms.firstk.personal.Time;

import java.util.List;

public interface Source {

    public List<Object> eventsContaining(Object person, Time interval, Location location);

    public List<Object> participants(Object event);

    public List<Object> subevents(Object event);

    public List<Object> knows (Object person);

    public List<Object> knowsAtTime (Object person, Time time);
}
