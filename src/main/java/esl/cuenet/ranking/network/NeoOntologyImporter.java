package esl.cuenet.ranking.network;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.*;
import esl.cuenet.model.Constants;
import esl.cuenet.ranking.EventEntityNetwork;
import esl.cuenet.ranking.TextIndex;
import esl.cuenet.ranking.TypedEdge;
import esl.cuenet.ranking.URINode;
import org.apache.log4j.Logger;

import java.util.HashMap;

public class NeoOntologyImporter {

    private final OntModel model;
    private HashMap<String, URINode> nodeMap = null;
    private HashMap<String, URINode> blankNodeMap = new HashMap<String, URINode>(100);

    public static final String nodeURIIndexName = "nodeURIIndex";
    private TextIndex uriIndex = null;

    private Logger logger = Logger.getLogger(NeoOntologyImporter.class);

    public NeoOntologyImporter(OntModel model) {
        this.model = model;
    }

    public void loadIntoGraph(EventEntityNetwork network) {
        nodeMap = new HashMap<String, URINode>(500);
        uriIndex = network.textIndex(nodeURIIndexName);

        StmtIterator stmts = model.listStatements();
        while (stmts.hasNext()) {
            Statement statement = stmts.next();
            loadIntoGraph(network, statement.getSubject(), statement.getPredicate(), statement.getObject());
        }

        //modify blank nodes URIs
        int i = 0;
        for (String blankNodeURI: blankNodeMap.keySet()) {
            URINode bn = blankNodeMap.get(blankNodeURI);
            String uri = Constants.CuenetNamespace + Constants.BlankNode + i;
            bn.setProperty(OntProperties.ONT_URI, uri);
            uriIndex.put(bn, OntProperties.ONT_URI, uri);
            i++;
        }

    }

    private void loadIntoGraph(EventEntityNetwork network, Resource subject, Property predicate, RDFNode object) {
        if (subject.isAnon() || object.isAnon()) {
            loadBlank(network, subject, predicate, object);
            logger.info("BLANK " + subject + " <=> " + predicate + " <=> " + object);
            logger.info("BLANK " + subject.isAnon() + " " + object.isAnon());
            return;
        }

        if ( !object.isResource() ) return;

        logger.info(subject + " <=> " + predicate + " <=> " + object);

        //find Node corresponding to subject
        URINode subjectNode;
        String subjectURI = subject.getURI();
        if ( !nodeMap.containsKey(subjectURI) ) {
            subjectNode = network.createNode();
            subjectNode.setProperty(OntProperties.ONT_URI, subjectURI);
            uriIndex.put(subjectNode, OntProperties.ONT_URI, subjectURI);
            nodeMap.put(subjectURI, subjectNode);
        }
        else subjectNode = nodeMap.get(subject.getURI());

        //find Node corresponding to object
        URINode objectNode;
        String objectURI = object.asResource().toString();
        if ( !nodeMap.containsKey(object.asResource().toString())) {
            objectNode = network.createNode();
            objectNode.setProperty(OntProperties.ONT_URI, objectURI);
            uriIndex.put(objectNode, OntProperties.ONT_URI, objectURI);
            nodeMap.put(objectURI, objectNode);
        }
        else objectNode = nodeMap.get(objectURI);

        TypedEdge edge = subjectNode.createEdgeTo(objectNode);
        edge.setProperty(OntProperties.ONT_URI, predicate.getURI());

        logger.info(subjectURI + " <=> " + predicate.getURI() + " <=> " + objectURI);
    }

    private void loadBlank(EventEntityNetwork network, Resource subject, Property predicate, RDFNode object) {

        //find Node corresponding to subject
        URINode subjectNode;
        String subjectURI;
        HashMap<String, URINode> subjectMap;
        if (subject.isAnon()) {
            subjectMap = blankNodeMap;
            subjectURI = "B" + subject.asNode().getBlankNodeLabel();
        }
        else {
            subjectMap = nodeMap;
            subjectURI = subject.getURI();
        }

        if ( !subjectMap.containsKey(subjectURI) ) {
            subjectNode = network.createNode();
            subjectNode.setProperty(OntProperties.ONT_URI, subjectURI);
            if ( !subject.isAnon() ) uriIndex.put(subjectNode, OntProperties.ONT_URI, subjectURI);
            subjectMap.put(subjectURI, subjectNode);
        }
        else subjectNode = subjectMap.get(subjectURI);

        //find Node corresponding to object
        URINode objectNode;
        String objectURI;
        HashMap<String, URINode> objectMap;
        if (object.asNode().isBlank()) {
            objectMap = blankNodeMap;
            objectURI = "B" + object.asNode().getBlankNodeLabel();
        }
        else {
            objectMap = nodeMap;
            objectURI = object.asResource().getURI();
        }

        if ( !objectMap.containsKey(objectURI) ) {
            objectNode = network.createNode();
            objectNode.setProperty(OntProperties.ONT_URI, objectURI);
            if ( !object.isAnon() ) uriIndex.put(objectNode, OntProperties.ONT_URI, objectURI);
            objectMap.put(objectURI, objectNode);
        }
        else objectNode = objectMap.get(objectURI);

        TypedEdge edge = subjectNode.createEdgeTo(objectNode);
        edge.setProperty(OntProperties.ONT_URI, predicate.getURI());
        logger.info(subjectURI + " <=> " + predicate + " <=> " + objectURI);
    }

    public void mapGraph(EventEntityNetwork network) {

    }

}
