package esl.cuenet.algorithms.firstk;

import com.hp.hpl.jena.ontology.Individual;

public class Vote {

    public Vote() {}
    public Vote(String entityID, double score, Individual entityIndividual) {
        this.entityID = entityID;
        this.score = score;
        this.entity = entityIndividual;
    }

    public String entityID;
    public double score;
    public Individual entity;
}

