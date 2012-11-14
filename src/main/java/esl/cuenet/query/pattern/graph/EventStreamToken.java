package esl.cuenet.query.pattern.graph;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;

public class EventStreamToken {

    private Individual individual;
    private EventStreamToken subEventToken;

    public EventStreamToken(Individual individual) {
        this.individual = individual;
    }

    public OntClass getOntClass() {
        return individual.getOntClass();
    }

    public boolean match(OntClass oc) {
        return individual.getOntClass().getURI().equals(oc.getURI());
    }

    public EventStreamToken createSubEventToken(Individual subEventInstance) {
        this.subEventToken = new EventStreamToken(subEventInstance);
        return this.subEventToken;
    }

    public EventStreamToken getSubEventToken() {
        return subEventToken;
    }

    public String toString() {
        return "";
    }

}
