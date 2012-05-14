package esl.cuenet.algorithms.firstk.impl;

import com.hp.hpl.jena.ontology.Individual;

public class Score<E> {

    public Score(Individual individual) {
        this.individual = individual;
        this.scores = 0;
        this.isDiscovered = false;
        this.isVerified = false;
    }

    public Individual individual;
    public Integer scores;
    public boolean isDiscovered;
    public boolean isVerified;

}
