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
        EmailAccessor accessorTest = (EmailAccessor) algorithm.getSourceMapper().get("email").getAccessor();
        accessorTest.execute(new String[]{"Fabian.Groffen@cwi.nl"});
        //accessorTest.execute(new String[]{"21-questions@ics.uci.edu", "ppnguyen@uci.edu"});
    }


    public class EAAlgorithm extends BaseAlgorithm {
        public EAAlgorithm() throws FileNotFoundException, ParseException {
            super();
        }
    }

}
