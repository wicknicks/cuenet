package esl.cuenet.source.accessors;

import esl.cuenet.source.Attribute;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import test.TestBase;

public class ConferenceSubEventAccessorTest {

    private Logger logger = Logger.getLogger(ConferenceSubEventAccessorTest.class);

    public ConferenceSubEventAccessorTest() {
        SysLoggerUtils.initLogger();
    }

    @Test
    public void getSessionTest() throws Exception {
        logger.info("Sessions");

        TestBase.TestAlgorithm algorithm = new TestBase.TestAlgorithm();
        SessionAccessor accessorTest = new SessionAccessor(algorithm.getModel());
        accessorTest.setAttributeNames(new Attribute[]{new Attribute("url")});
        accessorTest.associateString(new Attribute("url"), "http://turing100.acm.org");
        accessorTest.executeQuery();
    }

    @Test
    public void getKeynotesTest() throws Exception {
        logger.info("Keynotes");

        TestBase.TestAlgorithm algorithm = new TestBase.TestAlgorithm();
        KeynoteAccessor accessorTest = new KeynoteAccessor(algorithm.getModel());
        accessorTest.setAttributeNames(new Attribute[]{new Attribute("url")});
        accessorTest.associateString(new Attribute("url"), "http://turing100.acm.org");
        accessorTest.executeQuery();
    }

    @Test
    public void getLunchBreaksTest() throws Exception {
        logger.info("Lunch/Breaks");

        TestBase.TestAlgorithm algorithm = new TestBase.TestAlgorithm();
        ConferenceLunchAccessor accessorTest = new ConferenceLunchAccessor(algorithm.getModel());
        accessorTest.setAttributeNames(new Attribute[]{new Attribute("url")});
        accessorTest.associateString(new Attribute("url"), "http://turing100.acm.org");
        accessorTest.executeQuery();
    }

    @Test
    public void getConfTalksTest() throws Exception {
        logger.info("Talks");

        TestBase.TestAlgorithm algorithm = new TestBase.TestAlgorithm();
        ConferenceTalkAccessor accessorTest = new ConferenceTalkAccessor(algorithm.getModel());
        accessorTest.setAttributeNames(new Attribute[]{new Attribute("url")});
        accessorTest.associateString(new Attribute("url"), "http://turing100.acm.org");
        accessorTest.executeQuery();
    }

}
