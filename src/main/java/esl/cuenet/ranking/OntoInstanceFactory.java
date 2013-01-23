package esl.cuenet.ranking;

import com.hp.hpl.jena.ontology.Individual;

public interface OntoInstanceFactory {

    URINode createNode(Individual ontologyInstance);

}
