package esl.cuenet.algorithms.firstk.impl;

import com.hp.hpl.jena.vocabulary.RDF;
import esl.cuenet.algorithms.firstk.Vote;
import esl.cuenet.algorithms.firstk.Voter;
import esl.cuenet.algorithms.firstk.exceptions.EventGraphException;
import esl.cuenet.algorithms.firstk.structs.eventgraph.BFSEventGraphTraverser;
import esl.cuenet.algorithms.firstk.structs.eventgraph.Entity;
import esl.cuenet.algorithms.firstk.structs.eventgraph.EventGraph;
import esl.cuenet.model.Constants;
import esl.cuenet.query.QueryEngine;
import esl.datastructures.graph.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EntityVoter implements Voter {

    private Logger logger = Logger.getLogger(EntityVoter.class);
    private QueryEngine queryEngine = null;

    public EntityVoter (QueryEngine engine) {
        this.queryEngine = engine;
    }

    @Override
    public Vote[] vote(EventGraph graph) {
        BFSEventGraphTraverser traverser = new BFSEventGraphTraverser(graph);
        List<Entity> graphEntities = new ArrayList<Entity>();

        TraversalContext traversalContext = new TraversalContext();
        traversalContext.setCx(graphEntities);
        traverser.setTraversalContext(traversalContext);

        traverser.setNodeVisitorCallback(new NodeVisitor() {
            @Override
            @SuppressWarnings("unchecked")
            public void visit(Node node, TraversalContext traversalContext) {
                List<Entity> entities = (List<Entity>) traversalContext.getCx();
                if (node instanceof Entity) entities.add((Entity) node);
            }
        });

        traverser.setEdgeVisitorCallback(new EdgeVisitor() {
            @Override
            public void visit(Edge edge, TraversalContext traversalContext) { }
        });

        traverser.start();

        logger.info("Entities found: " + graphEntities.size());

        for (Entity entity: graphEntities) {
            String name = null;
            String email = null;

            try {
                if (entity.containsLiteralEdge(Constants.Name)) {
                    logger.info(entity.getLiteralValue(Constants.Name));
                    name = (String) entity.getLiteralValue(Constants.Name);
                }
                if (entity.containsLiteralEdge(Constants.Email)) {
                    logger.info(entity.getLiteralValue(Constants.Email));
                    email = (String) entity.getLiteralValue(Constants.Email);
                }
            } catch (EventGraphException e) {
                e.printStackTrace();
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
            queryEngine.execute(sparqlQuery);

        }

        System.out.print("Enter choice: ");
        Scanner scanner = new Scanner(System.in);
        String data = scanner.nextLine();

        if (data.compareToIgnoreCase("q") == 0) System.exit(0);

        logger.info("Choice: " + data);

        return new Vote[0];
    }

    // throw some basic stuff out for now.
    public String getUIDs(String name) {
        if (name.compareTo("Torsten Grust") == 0) return "acx_1@wicknicks";
        if (name.compareTo("Thomas Willhalm") == 0) return "acx_2@wicknicks";
        else return "fb_717562539@wicknicks";
    }

}
