package esl.cuenet.ranking;

import esl.datastructures.Location;
import esl.datastructures.TimeInterval;

import java.util.Iterator;

public interface SpatioTemporalIndex {

    void construct();

    Iterator<URINode> lookup(Location location);

    Iterator<URINode> lookup(TimeInterval interval);

}
