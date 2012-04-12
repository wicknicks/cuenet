package esl.cuenet.algorithms.firstk.structs.eventgraph;

import com.hp.hpl.jena.ontology.Individual;
import esl.datastructures.graph.relationgraph.RelationGraphNode;

import java.util.List;

public class Event extends EventGraphNode implements Comparable<Event> {

    public Event (Individual individual) {
        super(individual, EventGraph.NodeType.EVENT);
    }

    public Individual getIndividual() {
        return individual;
    }

    @Override
    public int compareTo(Event o) {
        return 0;
    }

}


