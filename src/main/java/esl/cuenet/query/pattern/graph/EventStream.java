package esl.cuenet.query.pattern.graph;

import java.util.Iterator;
import java.util.LinkedList;

public class EventStream extends LinkedList<EventStreamToken> {

    @Override
    public String toString() {
        if (this.size() == 0) return "";

        StringBuilder builder = new StringBuilder();
        for (EventStreamToken o : this) {
            builder.append(o.toString());
            builder.append(" -> ");
        }
        return builder.substring(0, builder.length() - 4);
    }

}
