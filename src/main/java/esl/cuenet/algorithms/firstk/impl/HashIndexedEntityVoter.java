package esl.cuenet.algorithms.firstk.impl;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
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
    private List<String> discoveredEntityURIs = new ArrayList<String>();
    private String entityBeingDiscovered = null;
    private List<String> verifiedEntityURIs = new ArrayList<String>();
    private HashMap<String, ScoreArray> candidateMap = new HashMap<String, ScoreArray>();

    private QueryEngine queryEngine = null;
    private Property nameProperty = null;

    public HashIndexedEntityVoter(QueryEngine engine, OntModel model) {
        this.queryEngine = engine;
        nameProperty = model.getProperty(Constants.CuenetNamespace + "name");
    }

    @Override
    public Vote[] vote(EventGraph graph, List<Individual> candidates) {
        List<Entity> graphEntities = graph.getEntities();
        List<String> projectVarURIs = new ArrayList<String>();
        projectVarURIs.add(Constants.CuenetNamespace + "person");

        logger.info("Entities found: " + graphEntities.size());

        merge(candidates);

        for (Entity entity: graphEntities) {
            String name = getValueFromEntityNode(entity, Constants.Name);
            String email = getValueFromEntityNode(entity, Constants.Email);

            if (isDiscovered(name)) continue;
            entityBeingDiscovered = getLiteralValue(entity.getIndividual(), nameProperty);

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
            discoveredEntityURIs.add(getLiteralValue(entity.getIndividual(), nameProperty));
        }

        List<Vote> votes = new ArrayList<Vote>();
        for(ScoreArray scoreArray: candidateMap.values()) {
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

    private void merge(List<Individual> candidates) {
        for (Individual candidate: candidates) {
            String name = getLiteralValue(candidate, nameProperty);
            ScoreArray original = candidateMap.get(name);
            if (original == null) candidateMap.put(name, new ScoreArray(candidate));
            else merge(original.individual, candidate);
        }
    }

    private void updateScores(Individual graphEntityIndividual, List<Individual> relatedCandidates) {
        for (Individual rCandidate: relatedCandidates) {
            String rName = getLiteralValue(rCandidate, nameProperty);
            if (entityBeingDiscovered.equals(rName)) continue;
            if (isVerified(rName)) continue;
            String name = getLiteralValue(graphEntityIndividual, nameProperty);
            ScoreArray original = candidateMap.get(rName);
            if (original == null) continue;
            Integer score = original.scores.get(name);
            if (score == null) original.scores.put(name, 1);
            else original.scores.put(name, score+1);
        }
    }

    private boolean isVerified(String name) {
        for (String u: verifiedEntityURIs) if (u.equals(name)) return true;
        return false;
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
        verifiedEntityURIs.add(getLiteralValue(entity, nameProperty));
    }

    private void merge(Individual original, Individual newIndividual) {
//        StmtIterator iter  = newIndividual.listProperties();
//        while (iter.hasNext()) {
//            Statement st = iter.next();
//            if (st.getObject().isLiteral()) continue;
//            if (!containsProperty(original, st.getPredicate()))
//                original.addProperty(st.getPredicate(), st.getObject());
//        }
    }

    private boolean containsProperty(Individual original, Property predicate) {
        return (original.getProperty(predicate) != null);
    }

    private boolean isDiscovered(String name) {
        for (String u: discoveredEntityURIs) if (u.equals(name)) return true;
        return false;
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
        }
        public Individual individual;
        public HashMap<String, Integer> scores;
    }

}
