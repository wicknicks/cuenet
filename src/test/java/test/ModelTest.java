package test;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import esl.cuenet.model.Constants;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ModelTest extends TestBase {

    private Logger logger = Logger.getLogger(ModelTest.class);
    private OntModel model;

    @Test
    public void test() throws FileNotFoundException {

        model = ModelFactory.createOntologyModel();
        model.read(new FileReader("/home/arjun/Documents/Dropbox/Ontologies/cuenet-main/cuenet-main.owl"),
                "http://www.semanticweb.org/arjun/cuenet-main.owl");

        String ns = "http://www.semanticweb.org/arjun/cuenet-main.owl#";

        String nsPrefix = model.getNsPrefixMap().get("");

        logger.info("ModelTest Default Namespace: " + nsPrefix);

        Individual indi = model.createIndividual(model.getOntClass(ns + "concert"));
        logger.info(indi.getOntClass());

    }

}
