package esl.cuenet.algorithms.firstk.impl;

import esl.cuenet.algorithms.firstk.exceptions.CorruptDatasetException;
import esl.cuenet.algorithms.firstk.exceptions.EventGraphException;
import esl.cuenet.mapper.parser.ParseException;
import org.apache.log4j.Logger;
import org.junit.Test;
import test.TestBase;

import java.io.File;
import java.io.IOException;

public class FirstKImplTests extends TestBase {

    private Logger logger = Logger.getLogger(FirstKImplTests.class);

    @Test
    public void doSingleFileTest() throws IOException, ParseException, EventGraphException {

        File file = new File("/home/arjun/Dataset/ramesh/confs/DSCN4265.JPG");
        FirstKDiscoverer firstKDiscoverer = new FirstKDiscoverer();
        firstKDiscoverer.execute(new LocalFileDataset(file, null));

    }


    @Test
    public void doMultipleFilesTest() throws IOException, ParseException, EventGraphException {
        String[] files = new String[]{"/home/arjun/Dataset/ramesh/confs/DSCN4265.JPG",
                "/home/arjun/Dataset/ramesh/confs/DSCN4352.JPG",
                "/home/arjun/Dataset/ramesh/confs/DSCN4357.JPG"};

        FirstKDiscoverer firstKDiscoverer = new FirstKDiscoverer();
        for (String file: files) {
            firstKDiscoverer.execute(new LocalFileDataset(file));
        }

    }


    @Test
    public void testCorruptDataset() throws IOException, ParseException {

        File file = new File("NONAME.JPG");
        FirstKDiscoverer firstKDiscoverer = new FirstKDiscoverer();

        try {
            firstKDiscoverer.execute(new LocalFileDataset(file, null));
        } catch (CorruptDatasetException cde) {
            logger.info("CorruptDatasetException: " + cde.getLocalizedMessage());
        } catch (EventGraphException e) {
            e.printStackTrace();
        }

    }

}
