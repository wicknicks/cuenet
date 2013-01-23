package esl.cuenet.ranking.network;

import com.hp.hpl.jena.ontology.OntModel;
import esl.cuenet.ranking.*;
import esl.datastructures.graph.Node;

import java.util.Iterator;

public class PersistentEventEntityNetwork implements EventEntityNetwork {

    public PersistentEventEntityNetwork(OntModel model) {

    }

    @Override
    public EventEntityNetwork subnet(Iterator<Node> nodeIterator) {
        return null;
    }

    @Override
    public NetworkTraverser traverser() {
        return null;
    }

    @Override
    public SpatioTemporalIndex stIndex() {
        return null;
    }

    @Override
    public TextIndex textIndex() {
        return null;
    }

    @Override
    public OntoInstanceFactory ontoInstanceFactory() {
        return null;
    }

    @Override
    public void rank(Ranker ranker) {
    }

    @Override
    public String version() {
        return null;
    }
}
