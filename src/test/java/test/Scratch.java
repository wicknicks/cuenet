package test;

import esl.cuenet.algorithms.firstk.exceptions.EventGraphException;
import esl.cuenet.algorithms.firstk.impl.FirstKDiscoverer;
import esl.cuenet.algorithms.firstk.impl.LocalFileDataset;
import esl.cuenet.mapper.parser.ParseException;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class Scratch extends TestBase {

    private Logger logger = Logger.getLogger(Scratch.class);

    @Test
    public void doSingleFileTest() throws IOException, ParseException, EventGraphException {

        File file = new File("/home/arjun/Dataset/ramesh/confs/DSCN4265.JPG");
        FirstKDiscoverer firstKDiscoverer = new FirstKDiscoverer();
        firstKDiscoverer.execute(new LocalFileDataset(file));

    }

}
