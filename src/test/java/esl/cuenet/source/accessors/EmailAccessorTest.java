package esl.cuenet.source.accessors;

import com.hp.hpl.jena.ontology.OntModel;
import esl.cuenet.algorithms.BaseAlgorithm;
import esl.cuenet.mapper.parser.ParseException;
import org.apache.log4j.Logger;
import org.junit.Test;
import test.TestBase;

import java.io.FileNotFoundException;

public class EmailAccessorTest extends TestBase {

    Logger logger = Logger.getLogger(EmailAccessorTest.class);

    public EmailAccessorTest() {
        super();
    }

    @Test
    public void doTest() throws FileNotFoundException, ParseException {

        EAAlgorithm algorithm = new EAAlgorithm();
        EmailAccessor accessorTest = new EmailAccessor(algorithm.getModel());
        accessorTest.execute(new String[]{"jain@ics.uci.edu", "gupta@sdsc.edu"});
    }


    public class EAAlgorithm extends BaseAlgorithm {
        public EAAlgorithm() throws FileNotFoundException, ParseException {
            super();
        }
    }

}
