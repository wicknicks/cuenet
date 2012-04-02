package esl.cuenet.source.accessors;

import esl.cuenet.source.SourceQueryException;
import org.apache.log4j.Logger;
import org.junit.Test;
import test.TestBase;


public class FacebookUserAccessorTest extends TestBase {

    private Logger logger = Logger.getLogger(FacebookUserAccessorTest.class);

    public FacebookUserAccessorTest() {
        super();
    }

    @Test
    public void doTest() throws SourceQueryException {
        FacebookUserAccessor accessor = new FacebookUserAccessor();
        accessor.start();
        accessor.executeQuery("Arjun Satish", null);
        logger.info("Done");
        accessor.start();
        accessor.executeQuery(null, "06/19");
    }

}
