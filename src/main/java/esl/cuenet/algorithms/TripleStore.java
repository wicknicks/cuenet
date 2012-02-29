package esl.cuenet.algorithms;

import com.hp.hpl.jena.ontology.Individual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TripleStore implements IStatementStore {

    List<Individual> dataStoreIndividuals = new ArrayList<Individual>();
    List<Individual> contextStoreIndividuals = new ArrayList<Individual>();

    public TripleStore(Individual[] individuals) {
        Collections.addAll(dataStoreIndividuals, individuals);
    }

    public Individual[] getIndividualsFromDataStore() {
        Individual[] individuals = new Individual[dataStoreIndividuals.size()];
        dataStoreIndividuals.toArray(individuals);
        return individuals;
    }

    public Individual[] getIndividualsFromContextStore() {
        Individual[] individuals = new Individual[contextStoreIndividuals.size()];
        contextStoreIndividuals.toArray(individuals);
        return individuals;
    }

    public boolean pushup(Individual individual) {

        Individual x = null;
        for (Individual cx : contextStoreIndividuals) {
            if (cx.getId().getLabelString().compareTo(individual.getId().getLabelString()) == 0) {
                x = cx;
                break;
            }
        }

        if (x != null) {
            dataStoreIndividuals.add(x);
            contextStoreIndividuals.remove(x);
            return false;
        }

        return true;
    }
}
