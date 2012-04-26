package esl.cuenet.algorithms.firstk;

import esl.cuenet.algorithms.firstk.structs.eventgraph.Entity;
import esl.cuenet.algorithms.firstk.structs.eventgraph.Event;

public interface Verification {

    public double verify(Entity entity, Event photoCaptureEvent);

}
