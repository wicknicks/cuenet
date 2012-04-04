package esl.cuenet.algorithms.collation;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import esl.cuenet.mapper.parser.ParseException;
import esl.cuenet.mapper.tree.SourceParseException;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.FileNotFoundException;

public class CollationAlgorithmTest {

    private Logger logger = Logger.getLogger(CollationAlgorithmTest.class);


    @Test
    public void doTest() throws SourceParseException, FileNotFoundException, ParseException {

        long l = System.currentTimeMillis();
        CollationAlgorithm collator = new CollationAlgorithm();
        logger.info("Time taken to load: " + (System.currentTimeMillis() - l) + "ms");

        OntModel model = collator.getModel();

        Individual coordinates = model.createIndividual(model.getOntClass("http://www.semanticweb.org/arjun/cuenet-main.owl#coordinates"));
        coordinates.addLiteral(model.getDatatypeProperty("http://www.semanticweb.org/arjun/cuenet-main.owl#latitude"), 33.690924);
        coordinates.addLiteral(model.getDatatypeProperty("http://www.semanticweb.org/arjun/cuenet-main.owl#longitude"), -117.889227);
        
//        StmtIterator iter = coordinates.listProperties();
//        while (iter.hasNext()) {
//            Statement s = iter.nextStatement();
//            logger.info(s.toString() + " " + (s.getObject().isLiteral()));
//        }

        collator.addSeeds(new Individual[]{coordinates});
        collator.start();                                    
    }

}