package esl.cuenet.query.pattern.graph;

public class EventStreamToken {

    private String individual = null;
    private EventStreamToken subEventToken = null;

    public EventStreamToken(String individual) {
        this.individual = individual;
    }

    public String getOntClass() {
        return individual;
    }

    public boolean match(String oc) {
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
        return individual;
    }

}
