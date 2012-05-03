package esl.cuenet.source.accessors;

import esl.cuenet.algorithms.BaseAlgorithm;
import esl.cuenet.mapper.parser.ParseException;
import esl.cuenet.mapper.tree.SourceParseException;
import esl.cuenet.source.SourceQueryException;
import org.apache.log4j.Logger;
import org.junit.Test;
import test.TestBase;

import java.io.FileNotFoundException;
import java.io.IOException;


public class FacebookUserAccessorTest extends TestBase {

    private Logger logger = Logger.getLogger(FacebookUserAccessorTest.class);

    public FacebookUserAccessorTest() {
        super();
    }

    @Test
    public void doModelTest() throws ParseException, IOException, SourceQueryException {
        TestAlgorithm fbTestBase = new TestAlgorithm();
        FacebookUserAccessor accessor = new FacebookUserAccessor(fbTestBase.getModel());
        accessor.start();
        accessor.executeQuery("Arjun Satish", null);
        logger.info("Done");
    }

    //@Test
    public void doTest() throws SourceQueryException {
        FacebookUserAccessor accessor = new FacebookUserAccessor();
        accessor.start();
        accessor.executeQuery("Arjun Satish", null);
        logger.info("Done");
        accessor.start();
        accessor.executeQuery(null, "06/19");
    }

}
