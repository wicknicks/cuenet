package esl.cuenet.algorithms.firstk.impl;

import esl.cuenet.algorithms.firstk.Vote;
import esl.cuenet.algorithms.firstk.Voter;
import esl.cuenet.algorithms.firstk.structs.eventgraph.BFSEventGraphTraverser;
import esl.cuenet.algorithms.firstk.structs.eventgraph.Entity;
import esl.cuenet.algorithms.firstk.structs.eventgraph.EventGraph;
import esl.datastructures.graph.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EntityVoter implements Voter {

    private Logger logger = Logger.getLogger(EntityVoter.class);

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

        System.out.print("Enter choice: ");
        Scanner scanner = new Scanner(System.in);
        String data = scanner.nextLine();

        if (data.compareToIgnoreCase("q") == 0) System.exit(0);

        logger.info("Choice: " + data);

        return new Vote[0];
    }

}
