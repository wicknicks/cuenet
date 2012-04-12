package esl.cuenet.algorithms.firstk.structs.eventgraph;

import com.hp.hpl.jena.ontology.Individual;
import esl.datastructures.graph.relationgraph.RelationGraphNode;

public abstract class EventGraphNode extends RelationGraphNode {

    protected Individual individual = null;
    private EventGraph.NodeType type = null;

    public EventGraphNode(Individual individual, EventGraph.NodeType type) {
        super(individual.getURI());
        this.individual = individual;
        this.type = type;
    }

    public Individual getIndividual() {
        return individual;
    }

    public EventGraph.NodeType getType() {
        return type;
    }
}