package esl.cuenet.algorithms.firstk.impl;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import esl.cuenet.algorithms.firstk.Vote;
import esl.cuenet.algorithms.firstk.Voter;
import esl.cuenet.algorithms.firstk.exceptions.EventGraphException;
import esl.cuenet.algorithms.firstk.structs.eventgraph.Entity;
import esl.cuenet.algorithms.firstk.structs.eventgraph.EventGraph;
import esl.cuenet.model.Constants;
import esl.cuenet.query.IResultIterator;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.QueryEngine;
import org.apache.log4j.Logger;

import java.util.*;

public class HashIndexedEntityVoter implements Voter {

    private Logger logger = Logger.getLogger(EntityVoter.class);

    private String entityBeingDiscovered = null;
//    private List<String> discoveredEntityURIs = new ArrayList<String>();
//    private List<String> verifiedEntityURIs = new ArrayList<String>();

    private HashMap<String, ScoreArray> candidateTable = new HashMap<String, ScoreArray>();

    List<String> projectVarURIs = new ArrayList<String>();

    private QueryEngine queryEngine = null;
    private Property nameProperty = null;
    private Property emailProperty = null;

    public HashIndexedEntityVoter(QueryEngine engine, OntModel model) {
        this.queryEngine = engine;
        nameProperty = model.getProperty(Constants.CuenetNamespace + "name");
        emailProperty = model.getProperty(Constants.CuenetNamespace + "email");
        projectVarURIs.add(Constants.CuenetNamespace + "person");
    }

    @Override
    public Vote[] vote(EventGraph graph, List<Individual> doNotUseArg0) {
        List<Entity> graphEntities = graph.getEntities();
        logger.info("Entities found: " + graphEntities.size());

        merge(graphEntities);

        for (Entity entity: graphEntities) {
            if (isDiscovered(entity)) continue;
            entityBeingDiscovered = getLiteralValue(entity.getIndividual(), nameProperty);
            discover(entity);
            addToDiscoveredPile(entityBeingDiscovered);
        }

        List<Vote> votes = new ArrayList<Vote>();
        for(ScoreArray scoreArray: candidateTable.values()) {
            int tot = 0;
            if (isVerified(getLiteralValue(scoreArray.individual, nameProperty))) continue;
            Collection<Integer> scores = scoreArray.scores.values();
            for (Integer s: scores) tot += s;
            if (tot == 0) continue;
            Vote v = new Vote();
            v.entity = scoreArray.individual;
            v.entityID = getLiteralValue(v.entity, nameProperty);
            v.score = tot;
            votes.add(v);
        }

        Vote[] rVotes = new Vote[votes.size()];
        votes.toArray(rVotes);
        return rVotes;
    }

    private void discover(Entity entity) {
        String name = getValueFromEntityNode(entity, Constants.Name);
        String email = getValueFromEntityNode(entity, Constants.Email);

        String sparqlQuery = "SELECT ?x \n" +
                " WHERE { \n" +
                "?x <" + RDF.type + "> <" + Constants.CuenetNamespace + "person> .\n" +
                "?y <" + RDF.type + "> <" + Constants.CuenetNamespace + "person> .\n" +
                "?y <" + Constants.CuenetNamespace + "knows" + ">" + " ?x .\n";

        if (email != null)
            sparqlQuery += "?y <" + Constants.CuenetNamespace + "email> \"" + email + "\" .\n";
        if (name != null)
            sparqlQuery += "?y <" + Constants.CuenetNamespace + "name> \"" + name + "\" .\n";

        sparqlQuery += "}";

        logger.info("Executing Sparql Query: \n" + sparqlQuery);
        List<IResultSet> relations = queryEngine.execute(sparqlQuery);
        logger.info("Found Relations from " + relations.size() + " sources for " + name);

        for (IResultSet resultSet : relations) {
            logger.info(resultSet.printResults());
            IResultIterator resultIterator = resultSet.iterator();
            while(resultIterator.hasNext()) {
                Map<String, List<Individual>> result = resultIterator.next(projectVarURIs);
                List<Individual> relatedCandidates = result.get(Constants.CuenetNamespace + "person");
                updateScores(entity.getIndividual(), relatedCandidates);
            }
        }
    }

    private void merge(Individual ind) {
        String name = getLiteralValue(ind, nameProperty);
        ScoreArray scoreArray = candidateTable.get(name);
        if (scoreArray == null) candidateTable.put(name, new ScoreArray(ind));
        else merge(scoreArray.individual, ind);
    }

    private void merge(List<Entity> entities) {
        for (Entity entity: entities) {
            merge(entity.getIndividual());
        }
    }

    private void updateScores(Individual graphEntityIndividual, List<Individual> relatedCandidates) {
        for (Individual rCandidate: relatedCandidates) {
            String rName = getLiteralValue(rCandidate, nameProperty);
            ScoreArray original = candidateTable.get(rName);
            if (original == null) {
                //original = new ScoreArray(rCandidate);
                //candidateTable.put(rName, original);
                continue;
            }

            if (entityBeingDiscovered.equals(rName)) continue;
            if (isVerified(rName)) continue;

            String name = getLiteralValue(graphEntityIndividual, nameProperty);
            Integer score = original.scores.get(name);
            if (score == null) original.scores.put(name, 1);
            else original.scores.put(name, score+1);
        }
    }

    private String getValueFromEntityNode(Entity entity, String constant) {
        String val = null;
        if (!entity.containsLiteralEdge(constant)) return null;
        try {
            val = (String) entity.getLiteralValue(constant);
            logger.info(val);
        } catch (EventGraphException e) {
            e.printStackTrace();
        }
        return val;
    }

    @Override
    public void addToVerifiedPile(Individual entity) {
        String name = getLiteralValue(entity, nameProperty);
        ScoreArray scoreArray = candidateTable.get(name);
        if (scoreArray == null) {
            scoreArray = new ScoreArray(entity);
            candidateTable.put(name, scoreArray);
        }
        scoreArray.isVerified = true;
    }

    private boolean isVerified(String name) {
        ScoreArray scoreArray = candidateTable.get(name);
        return scoreArray.isVerified;
    }

    private void addToDiscoveredPile(String name) {
        ScoreArray scoreArray = candidateTable.get(name);
        scoreArray.isDiscovered = true;
    }

    private boolean isDiscovered(Entity entity) {
        String name = getLiteralValue(entity.getIndividual(), nameProperty);
        ScoreArray scoreArray = candidateTable.get(name);
        return scoreArray.isDiscovered;
    }

    private String getLiteralValue(Individual individual, Property property) {
        Statement statement = individual.getProperty(property);
        if (statement == null) return null;
        if (!statement.getObject().isLiteral()) return null;
        return statement.getObject().asLiteral().getString();
    }

    private class ScoreArray {
        public ScoreArray(Individual individual) {
            this.individual = individual;
            this.scores = new HashMap<String, Integer>(10);
            this.isDiscovered = false;
            this.isVerified = false;
        }
        public Individual individual;
        public HashMap<String, Integer> scores;
        public boolean isDiscovered;
        public boolean isVerified;
    }

    private void merge(Individual original, Individual newIndividual) {
        String oem = getLiteralValue(original, emailProperty);
        String nem = getLiteralValue(newIndividual, emailProperty);

        if (oem == null && nem != null)
            original.addLiteral(emailProperty, nem);
    }
}
