package esl.cuenet.source.accessors;

import esl.cuenet.mapper.parser.ParseException;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.SourceQueryException;
import esl.system.SysLoggerUtils;
import org.junit.Test;
import test.TestBase;

import java.io.IOException;

public class ConferenceSubEventAccessorTest {

    @Test
    public void getConfAttendeeTest() throws Exception {
        SysLoggerUtils.initLogger();

        TestBase.TestAlgorithm algorithm = new TestBase.TestAlgorithm();
        SessionAccessor accessorTest = new SessionAccessor(algorithm.getModel());
        accessorTest.setAttributeNames(new Attribute[]{new Attribute("url")});
        accessorTest.associateString(new Attribute("url"), "http://turing100.acm.org");
        accessorTest.executeQuery();
    }

}
