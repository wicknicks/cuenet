package esl.cuenet.ranking;

import com.hp.hpl.jena.ontology.Individual;
import esl.datastructures.graph.Node;

public interface OntoInstanceFactory {

    Node createNode();

    Node createNode(Individual ontologyInstance);

}
