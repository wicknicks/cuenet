package esl.cuenet.ranking.network;

import com.hp.hpl.jena.ontology.Individual;
import esl.cuenet.ranking.OntoInstanceFactory;
import esl.cuenet.ranking.URINode;
import org.neo4j.graphdb.GraphDatabaseService;

public class NeoOntoInstanceImporter implements OntoInstanceFactory {

    private final GraphDatabaseService graphDb;

    public NeoOntoInstanceImporter(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    @Override
    public URINode createNode(Individual ontologyInstance) {
        return null;
    }

}
