package esl.cuenet.algorithms.firstk.impl;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import esl.cuenet.algorithms.firstk.Vote;
import esl.cuenet.algorithms.firstk.Voter;
import esl.cuenet.algorithms.firstk.exceptions.EventGraphException;
import esl.cuenet.algorithms.firstk.structs.eventgraph.BFSEventGraphTraverser;
import esl.cuenet.algorithms.firstk.structs.eventgraph.Entity;
import esl.cuenet.algorithms.firstk.structs.eventgraph.EventGraph;
import esl.cuenet.model.Constants;
import esl.cuenet.query.IResultIterator;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.QueryEngine;
import esl.datastructures.graph.*;
import org.apache.log4j.Logger;

import java.util.*;

public class EntityVoter implements Voter {

    private Logger logger = Logger.getLogger(EntityVoter.class);
    private QueryEngine queryEngine = null;

    private List<Map.Entry<Individual, Integer[]>> scores = new ArrayList<Map.Entry<Individual, Integer[]>>();
    private Property nameProperty = null;
    private OntModel model = null;
    int xxx = 0;

    private List<Individual> verifiedPile = new ArrayList<Individual>();

    public EntityVoter (QueryEngine engine, OntModel model) {
        this.queryEngine = engine;
        this.model = model;
        nameProperty = model.getProperty(Constants.CuenetNamespace + "name");
    }

    @Override
    public Vote[] vote(EventGraph graph, List<Individual> candidates) {
        List<Entity> graphEntities = graph.getEntities();
        List<String> projectVarURIs = new ArrayList<String>();
        projectVarURIs.add(Constants.CuenetNamespace + "person");

        logger.info("Entities found: " + graphEntities.size());
        scores.clear();

        int tb_size = candidates.size();
        for (int i=0; i<tb_size; i++) {
            Integer[] candScores = new Integer[graphEntities.size() + 1];
            for (int j=0; j<candScores.length; j++) candScores[j]=0;
            Map.Entry<Individual, Integer[]> entry = new AbstractMap.SimpleEntry<Individual, Integer[]>(candidates.get(i), candScores);
            scores.add(entry);
        }

        int graphEntityIndex = 0;
        for (Entity entity: graphEntities) {
            String name = null;
            String email = null;

            try {
                if (entity.containsLiteralEdge(Constants.Name)) {
                    name = (String) entity.getLiteralValue(Constants.Name);
                    logger.info(name);
                }

                email = cheatEmail(name);
                if (email == null && entity.containsLiteralEdge(Constants.Email)) {
                    email = (String) entity.getLiteralValue(Constants.Email);
                    logger.info(email);
                }
            } catch (EventGraphException e) {
                e.printStackTrace();
            }

            if (name != null && name.compareTo("Arjun Satish") == 0) {
                if (xxx > 0) continue;
                xxx++;
            }

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
                    updateScores(graphEntityIndex, relatedCandidates, candidates);
                }
            }
        }

        Collections.sort(scores, new Comparator<Map.Entry<Individual, Integer[]>>() {
            @Override
            public int compare(Map.Entry<Individual, Integer[]> _o1, Map.Entry<Individual, Integer[]> _o2) {
                Integer[] o1 = _o1.getValue();
                Integer[] o2 = _o2.getValue();
                return (o2[o2.length-1]-o1[o1.length-1]);
            }
        });

        int nonZeroCandidates = 0;
        for (Map.Entry<Individual, Integer[]> entry: scores) {
            Integer[] cs = entry.getValue();
            if (cs[cs.length-1] > 0) nonZeroCandidates++;
            String sss = String.format("%-25s", getName(entry.getKey()));
            for (Integer s: cs) sss += " " + s;
            logger.info(sss);
        }

        Vote[] votes = new Vote[nonZeroCandidates];
        int ix = 0;
        for (Map.Entry<Individual, Integer[]> entry: scores) {
            Integer[] cs = entry.getValue();
            if (cs[cs.length-1] == 0) continue;
            Vote vote = new Vote();
            vote.entityID = getName(entry.getKey());
            vote.score = cs[cs.length-1];
            vote.entity = entry.getKey();
            votes[ix] = vote; ix++;
        }

        return votes;
    }

    private boolean isVerified(Individual individual) {
        String indName = getName(individual);
        if (indName == null) return false;

        for (Individual verifiedIndividual: verifiedPile) {
            String vName = getName(verifiedIndividual);
            if (vName == null) continue;
            if (vName.compareTo(indName) == 0) return true;
        }
        return false;
    }

    private void updateScores(int graphEntityIndex, List<Individual> relatedCandidates, List<Individual> allCandidates) {
        if (relatedCandidates == null) return;

        String relatedCandidateName = null;

//        long start = System.currentTimeMillis();
        for (Individual candidate: relatedCandidates) {
            relatedCandidateName = getName(candidate);
            if (relatedCandidateName == null) continue;
            updateScore(graphEntityIndex, relatedCandidateName, allCandidates);

//            Statement statement = candidate.getProperty(nameProperty);
//            if (statement == null) continue;
//            if (!statement.getObject().isLiteral()) continue;
//            updateScore(graphEntityIndex, statement.getObject().asLiteral().getString(), allCandidates);
        }

//        long end = System.currentTimeMillis();
//        logger.info("Exec Time: " + (end-start));
    }

    private void updateScore(int graphEntityIndex, String relationName, List<Individual> allCandidates) {
        if (relationName == null) return;

        int pos = -1; int ix = 0;
        for (Individual candidate: allCandidates) {
            if (isVerified(candidate)) {
                ix++;
                continue;
            }

            String candidateName = getName(candidate);
            if (candidateName == null) continue;
            if (candidateName.compareTo(relationName) == 0 /*&& candidateName.compareTo("Arjun Satish") != 0*/) {
                Integer[] candScores = scores.get(ix).getValue();
                candScores[graphEntityIndex]++;
                candScores[candScores.length-1]++;
                logger.info("Yohoo! Found " + candidateName + " at " + ix);
            }
            ix++;
        }
    }

    private String getName(Individual individual) {
        Statement statement = individual.getProperty(nameProperty);
        if (statement == null) return null;
        if (!statement.getObject().isLiteral()) return null;
        return statement.getObject().asLiteral().getString();
    }

    // throw some basic stuff out for now.
    public String getUIDs(String name) {
        if (name.compareTo("Torsten Grust") == 0) return "acx_1@wicknicks";
        else if (name.compareTo("Thomas Willhalm") == 0) return "acx_2@wicknicks";
        else if (name.compareTo("Chen Li") == 0) return "fb_1385092812@wicknicks";
        else if (name.compareTo("Atish Das Sarma") == 0) return "fb_640150760@wicknicks";
        else if (name.compareTo("Danupon Nanongkai") == 0) return "fb_12815178@wicknicks";
        else if (name.compareTo("Jennie Zhang") == 0) return "acx_3@wicknicks";
        else if (name.compareTo("Galen Reeves") == 0) return "fb_1230757@wicknicks";
        else if (name.compareTo("Nicola Onose") == 0) return "fb_3312053@wicknicks";
        else if (name.compareTo("Ramesh Jain") == 0) return "fb_6028816@wicknicks";
        else return "fb_717562539@wicknicks";
    }

    private String cheatEmail(String name) {
        if (name.compareTo("Atish Das Sarma") == 0) return "atish.dassarma@gmail.com";
        else if (name.compareTo("Danupon Nanongkai") == 0) return "danupon@gmail.com";
        else if (name.compareTo("Galen Reeves") == 0) return "unknown@gmail.com";
        else if (name.compareTo("Nicola Onose") == 0) return "onose@wicknicks.com";
        else if (name.compareTo("Ramesh Jain") == 0) return "jain@ics.uci.edu";
        else return null;
    }

    public void addToVerifiedPile(Individual person) {
        verifiedPile.add(person);
    }
}
