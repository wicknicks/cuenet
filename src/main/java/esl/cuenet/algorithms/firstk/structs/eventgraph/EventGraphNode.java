package esl.cuenet.algorithms.firstk.structs.eventgraph;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import esl.cuenet.algorithms.firstk.exceptions.EventGraphException;
import esl.datastructures.graph.relationgraph.RelationGraphNode;
import org.apache.log4j.Logger;

import java.util.Map;

public abstract class EventGraphNode extends RelationGraphNode {

    protected Individual individual = null;
    private EventGraph.NodeType type = null;
    private static Logger logger = Logger.getLogger(EventGraphNode.class);

    public EventGraphNode(Individual individual, EventGraph.NodeType type) {
        super(individual.getURI());
        if (individual.getURI() == null) logger.error("Individual with NULL URI");
        this.individual = individual;
        this.type = type;
    }

    public Individual getIndividual() {
        return individual;
    }

    public EventGraph.NodeType getType() {
        return type;
    }

    public void addLiteral(Property property, Literal literal) {
        individual.addLiteral(property, literal);
    }

    public void addResource(Property property, Individual p) {
        individual.addProperty(property, p);
    }

    public boolean containsLiteralEdge(String literalLabel) {
        StmtIterator iter = individual.listProperties();
        Map<String, String> nsPrefixMap = individual.getModel().getNsPrefixMap();

        for (String key: nsPrefixMap.keySet()) {
            while (iter.hasNext()) {
                Statement statement = iter.nextStatement();
                if (statement.getPredicate().getURI().compareTo(nsPrefixMap.get(key) + literalLabel) == 0 &&
                        statement.getObject().isLiteral())
                    return true;
            }
        }

        return false;
    }

    public Object getLiteralValue(String literalLabel) throws EventGraphException {
        StmtIterator iter = individual.listProperties();
        Map<String, String> nsPrefixMap = individual.getModel().getNsPrefixMap();

        for (String key: nsPrefixMap.keySet()) {
            while (iter.hasNext()) {
                Statement statement = iter.nextStatement();
                if (statement.getPredicate().getURI().compareTo(nsPrefixMap.get(key) + literalLabel) == 0 &&
                        statement.getObject().isLiteral())
                    return statement.getObject().asLiteral().getValue();
            }
        }

        throw new EventGraphException("No value for literal label: " + literalLabel);
    }

}
