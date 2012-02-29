package esl.cuenet.algorithms;

import com.hp.hpl.jena.ontology.Individual;

import java.util.List;

public interface IRanker {

    RankerResults rank(List<Individual> dataStoreIndividuals, List<Individual> contextStoreIndividuals);

}
