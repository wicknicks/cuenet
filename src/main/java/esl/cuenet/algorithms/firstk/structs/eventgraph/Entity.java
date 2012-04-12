package esl.cuenet.algorithms.firstk.structs.eventgraph;

import com.hp.hpl.jena.ontology.Individual;
import esl.datastructures.graph.relationgraph.RelationGraphNode;

public class Entity extends EventGraphNode implements Comparable<Entity> {

    public Entity (Individual individual) {
        super(individual, EventGraph.NodeType.ENTITY);
    }

    public Individual getIndividual() {
        return individual;
    }

    @Override
    public int compareTo(Entity o) {
        return 0;
    }

}
