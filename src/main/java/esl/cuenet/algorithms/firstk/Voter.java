package esl.cuenet.algorithms.firstk;

import esl.datastructures.graph.relationgraph.RelationGraph;

public interface Voter {

    public Vote[] vote(RelationGraph graph);

}

