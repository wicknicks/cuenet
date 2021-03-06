package esl.cuenet.source.accessors;

import esl.cuenet.mapper.parser.ParseException;
import esl.datastructures.Location;
import esl.datastructures.TimeInterval;
import org.junit.Test;
import test.TestBase;

import java.io.IOException;

public class ConferenceAccessorTest extends TestBase {

    @Test
    public void doConfSourceQueryTest() throws IOException, ParseException {

        TestAlgorithm algorithm = new TestAlgorithm();
        ConferenceAccessor accessorTest = (ConferenceAccessor) algorithm.getSourceMapper().get("conferences").getAccessor();
        accessorTest.search(TimeInterval.createFromMoment(1251173399000L, algorithm.getModel()),
                Location.createFromAddress("Lyon, France", algorithm.getModel()));
    }

}
