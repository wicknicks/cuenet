package esl.cuenet.algorithms.firstk.structs.eventgraph;

import com.hp.hpl.jena.ontology.Individual;
import esl.datastructures.graph.relationgraph.RelationGraphNode;

public class Entity extends RelationGraphNode implements Comparable<Entity> {

    private Individual individual = null;

    public Entity(String name) {
        super(name);
    }

    public Entity (Individual individual) {
        super(individual.getURI());
        this.individual = individual;
    }

    public Individual getIndividual() {
        return individual;
    }

    @Override
    public int compareTo(Entity o) {
        return 0;
    }



}
