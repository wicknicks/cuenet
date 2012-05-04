package esl.cuenet.source.accessors;

import esl.cuenet.mapper.parser.ParseException;
import esl.datastructures.Location;
import esl.datastructures.TimeInterval;
import org.junit.Test;
import test.TestBase;

import java.io.IOException;

public class ConferenceAttendeeAccessorTest extends TestBase {

    @Test
    public void getConfAttendeeTest() throws IOException, ParseException {
        TestAlgorithm algorithm = new TestAlgorithm();
        ConferenceAttendeeAccessor accessorTest = (ConferenceAttendeeAccessor) algorithm.getSourceMapper().get("conf-attendees").getAccessor();
        //accessorTest.search("http://vldb2009.org/", null);
        accessorTest.search(null, "Ramesh Jain");
    }

}
