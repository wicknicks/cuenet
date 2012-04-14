package esl.datastructures;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.ontology.impl.IndividualImpl;
import esl.cuenet.model.Constants;

public class TimeInterval extends IndividualImpl {

    private long end = 0;
    private long start = 0;

    protected TimeInterval(Node n, EnhGraph g, long start, long end) {
        super(n, g);
        this.start = start;
        this.end = end;
    }

    public static TimeInterval createFromMoment(long timestamp, EnhGraph graph) {
        return new TimeInterval(new TimeNodeURI(), graph, timestamp, timestamp);
    }

    public static TimeInterval createFromInterval(long start, long end, EnhGraph graph) {
        return new TimeInterval(new TimeNodeURI(), graph, start, end);
    }

    /**
     * is this before OTHER
     */
    public boolean isBefore(TimeInterval other) {
        return this.end < other.start;
    }

    /**
     * does this meet other OR does other meet this
     */
    public boolean meets(TimeInterval other) {
        return (this.start == other.end) || (this.end == other.start);
    }

    /**
     * do this and other start at the same time
     */
    public boolean starts(TimeInterval other) {
        return (this.start == other.start);
    }

    /**
     * do this and other finish at the same time
     */
    public boolean finish(TimeInterval other) {
        return (this.end == other.end);
    }

    /**
     * does this overlap with other or other overlap with this
     */
    public boolean overlaps(TimeInterval other) {
        return (this.end > other.start && other.end > this.start);
    }

    /**
     * does this contain other
     */
    public boolean contains(TimeInterval other) {
        return (this.start < other.start && this.end > other.end);
    }

    /**
     * are this and other cotemporal?
     */
    public boolean cotemporal(TimeInterval other) {
        return (this.start == other.start && this.end == other.end);
    }


    private static class TimeNodeURI extends Node_URI {
        protected TimeNodeURI() {
            super(Constants.DOLCETimeIntervalURI);
        }
    }
}
