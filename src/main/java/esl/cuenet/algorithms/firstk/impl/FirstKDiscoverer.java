package esl.cuenet.algorithms.firstk.impl;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import esl.cuenet.algorithms.firstk.FirstKAlgorithm;
import esl.cuenet.algorithms.firstk.Voter;
import esl.cuenet.algorithms.firstk.exceptions.CorruptDatasetException;
import esl.cuenet.algorithms.firstk.exceptions.EventGraphException;
import esl.cuenet.algorithms.firstk.structs.eventgraph.*;
import esl.cuenet.mapper.parser.ParseException;
import esl.cuenet.model.Constants;
import esl.cuenet.query.QueryEngine;
import esl.datastructures.TimeInterval;
import esl.datastructures.graph.*;
import org.apache.log4j.Logger;
import org.openjena.atlas.io.PrintUtils;

import java.io.FileNotFoundException;
import java.util.*;

public class FirstKDiscoverer extends FirstKAlgorithm {

    private Logger logger = Logger.getLogger(FirstKDiscoverer.class);
    private BFSEventGraphTraverser graphTraverser = null;
    private Queue<EventGraphNode> discoveryQueue = new LinkedList<EventGraphNode>();
    private QueryEngine queryEngine = null;

    private Property subeventOfProperty = null;
    private Property participatesInProperty = null;
    private Property occursDuringProperty = null;
    private Property occursAtProperty = null;

    private Voter voter = null;
    private EventGraph graph = null;

    private final String cuenetNameSpace = "http://www.semanticweb.org/arjun/cuenet-main.owl#";


    public FirstKDiscoverer() throws FileNotFoundException, ParseException {
        super();
        queryEngine = new QueryEngine(model, sourceMapper);
        voter = new EntityVoter(queryEngine);

        subeventOfProperty = model.getProperty(cuenetNameSpace + "subevent-of");
        participatesInProperty = model.getProperty(Constants.DOLCE_Lite_Namespace + "participant-in");
        occursDuringProperty = model.getProperty(Constants.CuenetNamespace + "occurs-during");
        occursAtProperty = model.getProperty(Constants.CuenetNamespace + "occurs-at");
    }

    public void execute(LocalFileDataset lds) throws CorruptDatasetException, EventGraphException {

        LocalFilePreprocessor preprocessor = new LocalFilePreprocessor(model);
        graph = preprocessor.process(lds);

        TraversalContext traversalContext = new TraversalContext();
        traversalContext.setCx(discoveryQueue);
        graphTraverser = new BFSEventGraphTraverser(graph);
        graphTraverser.setTraversalContext(traversalContext);
        graphTraverser.setNodeVisitorCallback(new NodeVisitor() {
            @Override
            @SuppressWarnings("unchecked")
            public void visit(Node node, TraversalContext traversalContext) {
                Queue<EventGraphNode> dQueue = (Queue<EventGraphNode>) traversalContext.getCx();
                dQueue.add((EventGraphNode) node);
            }
        });
        graphTraverser.setEdgeVisitorCallback(new EdgeVisitor() {
            @Override
            public void visit(Edge edge, TraversalContext traversalContext) { } });

        discover(graph);
    }

    private void discover(EventGraph graph) throws EventGraphException {
        if (terminate(graph)) return;
        discoveryQueue.clear();
        graphTraverser.start();
        logger.info("Size of DQ: " + discoveryQueue.size());

        for (EventGraphNode node : discoveryQueue) {
            if (node.getType() == EventGraph.NodeType.EVENT) discover((Event) node);
            else if (node.getType() == EventGraph.NodeType.ENTITY) discover((Entity) node);
        }

        voter.vote(graph);

        discover(graph);
    }

    private void discover(Entity entity) throws EventGraphException {

        String name = null;
        String email = null;

        if (entity.containsLiteralEdge(Constants.Name)) {
            logger.info(entity.getLiteralValue(Constants.Name));
            name = (String) entity.getLiteralValue(Constants.Name);
        }
        if (entity.containsLiteralEdge(Constants.Email)) {
            logger.info(entity.getLiteralValue(Constants.Email));
            email = (String) entity.getLiteralValue(Constants.Email);
        }

        //find related event (if exists), and its related timestamp, location
        TimeInterval targetInterval = getTimeIntervalFromRelatedEvent(entity);

        String sparqlQuery = "SELECT ?x \n" +
                "WHERE { \n" +
                "?x <" + RDF.type + "> <" + Constants.DOLCE_Lite_Namespace + "event> . \n" +
                "?p <" + cuenetNameSpace + "participant-in> ?x . \n" +
                "?p <" + RDF.type + "> <" + cuenetNameSpace + "person> . \n";

        if (email != null)
            sparqlQuery += "?p <" + cuenetNameSpace + "email> \"" + email + "\" .\n";
        if (name != null)
            sparqlQuery += "?p <" + cuenetNameSpace + "name> \"" + name + "\" .\n";
        if (targetInterval != null) {
            sparqlQuery += "?x <" + occursDuringProperty.getURI() + "> \"" + targetInterval.getID() + "\" .\n";
        }

        sparqlQuery += "} \n";

        logger.info("Executing Sparql Query: \n" + sparqlQuery);
        queryEngine.execute(sparqlQuery);
    }

    private TimeInterval getTimeIntervalFromRelatedEvent(Entity entity) {
        List<EventGraphEdge> edges = graph.getEdges(entity);
        for (EventGraphEdge e: edges) {
            if (e.uri().compareTo(participatesInProperty.getURI()) == 0) {
                logger.info("Found: " + graph.getDestination(e).name());
                EventGraphNode eventNode = graph.getDestination(e);
                Statement ti = eventNode.getIndividual().getProperty(occursDuringProperty);
                TimeInterval itvl = lookupTi(ti.getObject().asResource());
                if (itvl != null) return itvl;
            }
        }
        return null;
    }

    private TimeInterval lookupTi(Resource r) {
        String uri = r.getURI();
        String parts[] = uri.split(" ");

        if (parts.length != 2) return null;

        if (parts[0].compareTo(Constants.DOLCE_Lite_Namespace + "time-interval") == 0) {
            return TimeInterval.getFromCache(parts[1]);
        }

        return null;
    }

    private void discover(Event event) throws EventGraphException {
        OntClass ontClass = event.getIndividual().getOntClass();
        List<OntClass> subevents = getPossibleSubeventClasses(ontClass.getURI());
        if (subevents.size() == 0) logger.info("No subevents for: " + ontClass.getURI());
        else logger.info(subevents.size() + " subevents for: " + ontClass.getURI());

        String sparqlQuery = "SELECT ?p \n" +
                "WHERE { \n" +
                "?x <" + RDF.type + "> <" + ontClass.getURI() + "> .\n" +
                "?p <" + cuenetNameSpace + "participant-in> ?x .\n" +
                "?p <" + RDF.type + "> <" + cuenetNameSpace + "person> .\n";

        sparqlQuery += "}";

        logger.info("Executing Sparql Query: \n" + sparqlQuery);
        queryEngine.execute(sparqlQuery);
    }

    private List<OntClass> getPossibleSubeventClasses(String superEventURI) {
        List<OntClass> subevents = new ArrayList<OntClass>();
        OntClass superEvent = model.getOntClass(superEventURI);

        // list all the events.
        OntClass eventOntClass = model.getOntClass(model.getNsPrefixURI(Constants.DOLCE_Lite_Namespace_Prefix) + "event");
        StmtIterator iterator = model.listStatements(null, RDFS.subClassOf, eventOntClass);

        while(iterator.hasNext()) {
            Statement stmt = iterator.nextStatement();
            OntClass event = model.getOntClass(stmt.getSubject().getURI());
            if (isSubeventOf(event, superEvent)) {
                logger.info("Found subevent: " + event.getURI()) ;
                subevents.add(event);
            }
        }

        return subevents;
    }

    private boolean isSubeventOf(OntClass event, OntClass superEvent) {
        OntClass superClass = event.getSuperClass();

        StmtIterator iter = model.listStatements(superClass, RDF.type, OWL.Restriction);
        while(iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            Resource restriction = stmt.getSubject();
            if (isSuperEventRestriction(restriction, superEvent)) return true;
        }

        return false;
    }

    private boolean isSuperEventRestriction(Resource classWithRestriction, OntClass superEvent) {

        Statement onProp = classWithRestriction.getProperty(OWL.onProperty);
        Statement someVal = classWithRestriction.getProperty(OWL.someValuesFrom);

        if (onProp == null || someVal == null) return false;

        if (!(onProp.getObject().equals(subeventOfProperty)))
            return false;

        //check if object is superEvent
        return someVal.getObject().equals(superEvent);
    }

    private boolean terminate(EventGraph graph) {
        return (!graph.equals(graph));
    }

}
