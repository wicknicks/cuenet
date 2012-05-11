package esl.cuenet.algorithms.firstk;

import com.hp.hpl.jena.ontology.Individual;
import esl.cuenet.algorithms.firstk.structs.eventgraph.EventGraph;

import java.util.List;

public interface Voter {

    public Vote[] vote(EventGraph graph, List<Individual> candidates);

    public void addToVerifiedPile(Individual person);

}

