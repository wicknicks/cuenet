package esl.cuenet.algorithms.collation;

import esl.cuenet.mapper.parser.ParseException;
import esl.cuenet.mapper.tree.SourceParseException;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.FileNotFoundException;

public class CollationAlgorithmTest {

    private Logger logger = Logger.getLogger(CollationAlgorithmTest.class);


    @Test
    public void doTest() throws SourceParseException, FileNotFoundException, ParseException {

        long l = System.currentTimeMillis();
        new CollationAlgorithm();
        logger.info("Time taken to load: " + (System.currentTimeMillis() - l) + "ms");

    }

}
