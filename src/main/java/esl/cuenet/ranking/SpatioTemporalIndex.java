package esl.cuenet.ranking;

import esl.datastructures.Location;
import esl.datastructures.TimeInterval;
import esl.datastructures.graph.Node;

import java.util.Iterator;

public interface SpatioTemporalIndex {

    void construct();

    Iterator<Node> lookup(Location location);

    Iterator<Node> lookup(TimeInterval interval);

}
