package esl.cuenet.ranking;

import esl.datastructures.Location;
import esl.datastructures.TimeInterval;

import java.util.Iterator;

public interface SpatioTemporalIndex {

    static final String OCCURS_DURING_IX = "occurs_during_ix";

    void construct();

    Iterator<URINode> lookup(Location location);

    /* Temporal Lookup Methods */
    void overlaps(long start, long end);

    void before(long moment);

    void after(long moment);

    void previous (long moment);

    void next (long moment);

}
