package esl.cuenet;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.log4j.Logger;
import org.junit.Test;
import test.TestBase;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class ModelTest extends TestBase {

    private Logger logger = Logger.getLogger(ModelTest.class);

    @Test
    public void test() throws FileNotFoundException {

        OntModel model = ModelFactory.createOntologyModel();
        model.read(new FileReader("/home/arjun/Documents/Dropbox/Ontologies/cuenet-main/cuenet-main.owl"),
                "http://www.semanticweb.org/arjun/cuenet-main.owl");

        String nsPrefix = model.getNsPrefixMap().get("");
        logger.info("ModelTest Default Namespace: " + nsPrefix);

    }

}
