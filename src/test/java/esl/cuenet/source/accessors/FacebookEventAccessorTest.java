package esl.cuenet.source.accessors;

import esl.cuenet.source.Attribute;
import esl.datastructures.TimeInterval;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import test.TestBase;

public class FacebookEventAccessorTest {

    private Logger logger = Logger.getLogger(ConferenceSubEventAccessorTest.class);

    public FacebookEventAccessorTest() {
        SysLoggerUtils.initLogger();
    }

    @Test
    public void getSessionTest() throws Exception {
        logger.info("Facebook Event Test");

        TestBase.TestAlgorithm algorithm = new TestBase.TestAlgorithm();
        FacebookEventAccessor accessorTest = new FacebookEventAccessor(algorithm.getModel());
        accessorTest.setAttributeNames(new Attribute[]{new Attribute("timeinterval")});
        accessorTest.associateTimeInterval(new Attribute("timeinterval"), TimeInterval.createFromMoment(1344749324, algorithm.getModel()));
        accessorTest.executeQuery();
    }

}
