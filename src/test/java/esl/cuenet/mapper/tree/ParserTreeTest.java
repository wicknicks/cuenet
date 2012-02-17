package esl.cuenet.mapper.tree;

import esl.cuenet.mapper.parser.MappingParser;
import esl.cuenet.mapper.parser.ParseException;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

public class ParserTreeTest {

    private Logger logger = Logger.getLogger(ParserTreeTest.class);
    
    @Test
    public void runParserTreeTests() throws ParseException {

        File file = new File("./src/main/javacc/test/");
        File[] files = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.contains("test"));
            }
        });

        for (File filename: files) {
            logger.info("Parsing " + filename.getAbsolutePath());
            parseFile(filename.getAbsolutePath());
        }

    }

    public void parseFile(String filename) throws ParseException {

        try {
            MappingParser parser = new MappingParser( new FileInputStream(filename));
            parser.setIParseTreeCreator(new ParseTree(filename));
            IParseTree tree =  parser.parse_document();

            ParseTreeInterpreter interpreter = new ParseTreeInterpreter(tree);
            interpreter.interpret();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
