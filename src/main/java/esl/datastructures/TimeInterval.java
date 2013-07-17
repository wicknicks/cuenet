package esl.datastructures;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.impl.IndividualImpl;
import esl.cuenet.model.Constants;

import java.util.HashMap;
import java.util.UUID;

public class TimeInterval extends IndividualImpl {

    private long end = 0;
    private long start = 0;
    private String id = null;
    private static HashMap<String, TimeInterval> tiCache = new HashMap<String, TimeInterval>();

    protected TimeInterval(Node n, EnhGraph g, long start, long end, String id) {
        super(n, g);
        this.start = start;
        this.end = end;
        this.id = id;
        tiCache.put(id, this);
    }

    public long getEnd() {
        return end;
    }

    public String getID() {
        return id;
    }

    public static TimeInterval getFromCache(String id) {
        return tiCache.get(id);
    }

    public long getStart() {
        return start;
    }

    public static TimeInterval createFromMoment(long timestamp, OntModel graph) {
        String id = UUID.randomUUID().toString();
        return new TimeInterval(new TimeNodeURI(id), (EnhGraph) graph, timestamp, timestamp, id);
    }

    public static TimeInterval createFromInterval(long start, long end, OntModel graph) {
        String id = UUID.randomUUID().toString();
        return new TimeInterval(new TimeNodeURI(id), (EnhGraph) graph, start, end, id);
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

    @Override
    public OntClass getOntClass() {
        return getOntModel().getOntClass(Constants.DOLCETimeIntervalURI);
    }

    private static class TimeNodeURI extends Node_URI {
        protected TimeNodeURI(String id) {
            super(Constants.DOLCETimeIntervalURI + " " + id);
        }
    }
}
