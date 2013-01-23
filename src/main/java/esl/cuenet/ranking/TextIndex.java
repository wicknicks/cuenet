package esl.cuenet.ranking;

import esl.datastructures.graph.Node;

import java.util.Iterator;

public interface TextIndex {

    void construct();

    Iterator<Node> lookup(String query);

}
