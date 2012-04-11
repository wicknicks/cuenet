package esl.cuenet.algorithms.firstk.structs.eventgraph;

import com.hp.hpl.jena.ontology.Individual;
import esl.datastructures.graph.relationgraph.RelationGraphNode;

import java.util.List;

public class Event extends RelationGraphNode implements Comparable<Event> {

    private Individual individual = null;

    public Event(String name) {
        super(name);
    }

    public Event (Individual individual) {
        super(individual.getURI());
        this.individual = individual;
    }

    public Individual getIndividual() {
        return individual;
    }

    public List<Event> getSubEvents() {
        return null;
    }

    @Override
    public int compareTo(Event o) {
        return 0;
    }
}


