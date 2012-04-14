package esl.cuenet.algorithms.firstk;

import esl.cuenet.algorithms.firstk.structs.eventgraph.EventGraph;

public interface Voter {

    public Vote[] vote(EventGraph graph);

}

