package esl.cuenet.query.pattern.graph;

import java.util.ArrayList;
import java.util.List;

public class MatchBuffer {

    private List<EventStreamToken> entries = new ArrayList<EventStreamToken>();

    public void add(EventStreamToken token) {
        entries.add(token);
    }



}
