package esl.cuenet.algorithms.firstk.impl;

import esl.cuenet.algorithms.firstk.CorruptDatasetException;
import esl.cuenet.mapper.parser.ParseException;
import esl.cuenet.mapper.tree.SourceParseException;
import org.apache.log4j.Logger;
import org.junit.Test;
import test.TestBase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FirstKImplTests extends TestBase {

    private Logger logger = Logger.getLogger(FirstKImplTests.class);

    @Test
    public void doSingleFileTest() throws IOException, ParseException {

        File file = new File("/home/arjun/Dataset/ramesh/confs/DSCN4265.JPG");
        FirstKImpl firstK = new FirstKImpl();
        firstK.execute(new LocalFileDataset(file));

    }


    @Test
    public void doMultipleFilesTest() throws IOException, ParseException {
        String[] files = new String[]{"/home/arjun/Dataset/ramesh/confs/DSCN4265.JPG",
                "/home/arjun/Dataset/ramesh/confs/DSCN4352.JPG",
                "/home/arjun/Dataset/ramesh/confs/DSCN4357.JPG"};

        FirstKImpl firstK = new FirstKImpl();
        for (String file: files) {
            firstK.execute(new LocalFileDataset(file));
        }

    }


    @Test
    public void testCorruptDataset() throws IOException, ParseException {

        File file = new File("NONAME.JPG");
        FirstKImpl firstK = new FirstKImpl();

        try {
            firstK.execute(new LocalFileDataset(file));
        } catch (CorruptDatasetException cde) {
            logger.info("CorruptDatasetException: " + cde.getLocalizedMessage());
        }

    }

}
