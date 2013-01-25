package model;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import esl.cuenet.ranking.URINode;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class OntologyReadTests {
    private static OntModel model = null;
    private Logger logger = Logger.getLogger(OntologyReadTests.class);

    @BeforeClass
    public static void setup() {
        SysLoggerUtils.initLogger();
        model = ModelFactory.createOntologyModel();

        try {
            model.read(new FileReader("/home/arjun/Documents/Dropbox/Ontologies/cuenet-main/cuenet-main.owl"),
                    "http://www.semanticweb.org/arjun/cuenet-main.owl");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void listBlanks() {
        HashMap<String, URINode> blankNodeMap = new HashMap<String, URINode>(100);
        Resource subject;
        RDFNode object;

        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            Statement statement = iter.nextStatement();
            subject = statement.getSubject();
            object = statement.getObject();

            if (subject.asNode().isBlank())
                blankNodeMap.put(subject.toString(), null);
            if (object.asNode().isBlank())
                blankNodeMap.put(object.toString(), null);
        }

        for (String s: blankNodeMap.keySet()) logger.info(s);
    }

    @Test
    public void listClasses() {
        ExtendedIterator<OntClass> eIter = model.listClasses();
        while (eIter.hasNext()) {
            OntClass eit = eIter.next();
//            logger.info(eit + " => " + eit.isRestriction());
        }
    }

    @Test
    public void listProperties() {
        ExtendedIterator<OntProperty> eIter = model.listAllOntProperties();
        while (eIter.hasNext()) {
            String uri = eIter.next().getURI();
//            if (uri.trim().length() > 0) logger.info(uri);
        }
    }

    @Test
    public void listStatements() {
        Resource subject, predicate;
        RDFNode object;

        Set<String> types = new HashSet<String>();

        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            Statement statement = iter.nextStatement();
            subject = statement.getSubject();
            predicate = statement.getPredicate();
            object = statement.getObject();

//            if (subject.asNode().isBlank())
                logger.info(subject + " => " + (predicate) + " .... " + object);

            if (predicate.equals(RDF.type)) {
                types.add(object.toString());
//                logger.info(subject + " => " + (predicate) + " .... " + object);
            }
        }

//        for (String s: types) logger.info(s);

    }

}
