package esl.cuenet.query.pattern.matcher;

import esl.cuenet.query.pattern.graph.EventStreamToken;

import java.util.ArrayList;
import java.util.List;

public class FringeClasses extends ArrayList<String> {

    private List<EventStreamToken> tokens = new ArrayList<EventStreamToken>();

    public FringeClasses() {

    }

    public FringeClasses(EventStreamToken token) {
        this.tokens.add(token);
    }

    public void add(EventStreamToken token) {
        this.tokens.add(token);
    }

    public List<String> getClasses() {
        ArrayList<String> classes = new ArrayList<String>();

        for (EventStreamToken token: tokens) {
            classes.add(token.getOntClass());

            while(true) {
                EventStreamToken sub = token.getSubEventToken();
                if (sub == null) return classes;
                classes.add(sub.getOntClass());

            }
        }

        return classes;
    }
}
