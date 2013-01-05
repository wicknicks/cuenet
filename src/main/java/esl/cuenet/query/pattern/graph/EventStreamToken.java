package esl.cuenet.query.pattern.graph;

import esl.cuenet.query.pattern.exceptions.EventStreamException;

public class EventStreamToken {

    private String individual = null;
    private EventStreamToken subEventToken = null;

    public EventStreamToken(String individual) {
        this.individual = individual;
    }

    public EventStreamToken(String ... individuals) {
        if (individuals.length == 1) throw new EventStreamException();
        this.individual = individuals[0];
        EventStreamToken sub = this;
        for (int i=1; i<individuals.length; i++) {
            sub = sub.createSubEventToken(individuals[i]);
        }
    }

    public String getOntClass() {
        return individual;
    }

    public boolean equal(String oc) {
        return individual.equals(oc);
    }

    public EventStreamToken createSubEventToken(String subEventInstance) {
        this.subEventToken = new EventStreamToken(subEventInstance);
        return this.subEventToken;
    }

    public EventStreamToken getSubEventToken() {
        return subEventToken;
    }

    public String toString() {
        if (subEventToken == null) return individual;
        else return individual + ":" + subEventToken.toString();
    }

}
