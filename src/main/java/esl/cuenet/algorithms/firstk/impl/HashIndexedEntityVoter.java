package esl.cuenet.algorithms.firstk.impl;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import esl.cuenet.algorithms.firstk.Vote;
import esl.cuenet.algorithms.firstk.structs.eventgraph.Entity;
import esl.cuenet.algorithms.firstk.structs.eventgraph.EventGraph;
import esl.cuenet.model.Constants;
import esl.cuenet.query.IResultIterator;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.QueryEngine;
import org.apache.log4j.Logger;

import java.util.*;

public class HashIndexedEntityVoter {

    private Logger logger = Logger.getLogger(EntityVoter.class);

    private CandidateVotingTable<String> candidateTable = new CandidateVotingTable<String>("eventgraph");
    private HashMap<String, CandidateVotingTable<String>> discoveredCandidatesTables = new
            HashMap<String, CandidateVotingTable<String>>();
    private List<String> projectVarURIs = new ArrayList<String>();

    private QueryEngine queryEngine = null;
    private Property nameProperty = null;
    private Property emailProperty = null;
    private List<EntityContext> verifiedEntities = null;

    public HashIndexedEntityVoter(QueryEngine engine, OntModel model) {
        this.queryEngine = engine;
        nameProperty = model.getProperty(Constants.CuenetNamespace + "name");
        emailProperty = model.getProperty(Constants.CuenetNamespace + "email");
        projectVarURIs.add(Constants.CuenetNamespace + "person");
        verifiedEntities = new ArrayList<EntityContext>();
    }

    public Vote[] vote(EventGraph graph, List<Entity> discoverableEntities) {
        List<Entity> graphEntities = graph.getEntities();
        logger.info("Entities found: " + graphEntities.size());

        List<EntityContext> discoverableEntityContexts = new ArrayList<EntityContext>();

        for (Entity entity: discoverableEntities) {
            EntityContext ecx = new EntityContext(entity,
                    getLiteralValue(entity.getIndividual(), nameProperty),
                    getLiteralValue(entity.getIndividual(), emailProperty));
            discoverableEntityContexts.add(ecx);
        }

        for (EntityContext ecx: discoverableEntityContexts) {
            if (discoveredCandidatesTables.get(ecx.name) == null)
                discover(ecx);
        }

        updateScoresForEventAttendees(graph.getEntities());

        return extractTopDCandidates();
    }

    private Vote[] extractTopDCandidates() {
        Iterator<String> ctIter = candidateTable.iterator();
        ArrayList<Vote> nonZeroVotes = new ArrayList<Vote>();
        while(ctIter.hasNext()) {
            String name = ctIter.next();
            Score<String> score = candidateTable.getScore(name);
            if (score.scores > 0) nonZeroVotes.add(
                    new Vote(getLiteralValue(score.individual, nameProperty),
                            score.scores, score.individual));
        }

        int dups = 0;
        for (Vote v: nonZeroVotes) {
            if (discoveredCandidatesTables.get(v.entityID) != null ||
                    isVerified(v.entityID)) dups++;
        }

        Vote[] votes = new Vote[nonZeroVotes.size() - dups];

        int i = 0;
        for (Vote v: nonZeroVotes)
            if (discoveredCandidatesTables.get(v.entityID) == null && !isVerified(v.entityID))
                votes[i++] = v;

        nonZeroVotes.toArray(votes);
        return votes;
    }

    private void updateScoresForEventAttendees(List<Entity> entities) {
        String name;
        for (Entity entity: entities) {
            name = getLiteralValue(entity.getIndividual(), nameProperty);
            if ( !candidateTable.contains(name) )
                candidateTable.addToCandidateTable(name, entity.getIndividual());
            updateScoresForEventAttendee(name);
        }
    }

    private void updateScoresForEventAttendee(String name) {
        for (Map.Entry<String, CandidateVotingTable<String>> dctEntry:
                discoveredCandidatesTables.entrySet()) {
            Score<String> score = dctEntry.getValue().getScore(name);
            if (score == null) continue;
            candidateTable.updateScore(name, score.scores + 1);
        }
    }

    private void discover(EntityContext ecx) {
        CandidateVotingTable<String> votingTable = new CandidateVotingTable<String>(ecx.name);
        for (IResultSet resultSet : query(ecx)) {
            logger.info(resultSet.printResults());
            IResultIterator resultIterator = resultSet.iterator();
            while(resultIterator.hasNext()) {
                Map<String, List<Individual>> result = resultIterator.next(projectVarURIs);
                List<Individual> relatedCandidates = result.get(Constants.CuenetNamespace + "person");
                updateScores(votingTable, relatedCandidates);
            }
        }
        discoveredCandidatesTables.put(ecx.name, votingTable);
    }

    private void updateScores(CandidateVotingTable<String> votingTable,
                              List<Individual> relatedCandidates) {
        String name;
        for (Individual candidate: relatedCandidates) {
            name = getLiteralValue(candidate, nameProperty);
            if ( !votingTable.contains(name) ) votingTable.addToCandidateTable(name, candidate);
            else votingTable.updateScore(name, 1);
        }
    }

    private String getLiteralValue(Individual individual, Property property) {
        Statement statement = individual.getProperty(property);
        if (statement == null) return null;
        if (!statement.getObject().isLiteral()) return null;
        return statement.getObject().asLiteral().getString();
    }

    public List<IResultSet> query(EntityContext ecx) {
        String sparqlQuery = "SELECT ?x \n" +
                " WHERE { \n" +
                "?x <" + RDF.type + "> <" + Constants.CuenetNamespace + "person> .\n" +
                "?y <" + RDF.type + "> <" + Constants.CuenetNamespace + "person> .\n" +
                "?y <" + Constants.CuenetNamespace + "knows" + ">" + " ?x .\n";

        if (ecx.email != null)
            sparqlQuery += "?y <" + Constants.CuenetNamespace + "email> \"" + ecx.email + "\" .\n";
        if (ecx.name != null)
            sparqlQuery += "?y <" + Constants.CuenetNamespace + "name> \"" + ecx.name + "\" .\n";

        sparqlQuery += "}";

        logger.info("Executing Sparql Query: \n" + sparqlQuery);
        return queryEngine.execute(sparqlQuery);
    }

    public boolean isVerified(String name) {
        for (EntityContext ecx: verifiedEntities) if (ecx.name.equals(name)) return true;
        return false;
    }

    public void addToVerifiedList(Entity verifiedEntity) {
        EntityContext ecx = new EntityContext(verifiedEntity,
                getLiteralValue(verifiedEntity.getIndividual(), nameProperty),
                getLiteralValue(verifiedEntity.getIndividual(), emailProperty));
        verifiedEntities.add(ecx);
    }

    private class EntityContext {
        public EntityContext(Entity entity, String name, String email) {
            this.email = email;
            this.entity = entity;
            this.name = name;
        }
        Entity entity;
        String name;
        String email;
    }

}