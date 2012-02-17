package esl.cuenet.mapper.parsers;

import esl.cuenet.mapper.parser.MappingParser;
import esl.cuenet.mapper.parser.ParseException;
import esl.cuenet.mapper.tree.IParseTree;
import esl.cuenet.mapper.tree.IParseTreeCreator;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;

public class MappingParserTest {

    public class ParseTreeCreatorTest implements IParseTreeCreator {

        @Override
        public void addOperator(String label) {
            System.out.println("Operator: " + label);
        }

        @Override
        public void addOperand(String operandValue) {
            System.out.println("Operand: " + operandValue);
        }

        @Override
        public void startSExpression() {

        }

        @Override
        public void endSExpression() {

        }

        @Override
        public void eof() {

        }

        @Override
        public IParseTree getTree() {
            return null;
        }

    }

    @Test
    public void doTest() throws ParseException {

        String example1 = "(:axioms\n" +
            "  (:map @yale_bib:book book)\n" +
            "  (:map @cmu_bib:book book))";

        String example2 = "(:axioms\n" +
            "  (:map @yale_bib:Book Book)\n" +
            "  (:map @cmu_bib:Book Book))";

        String example3 = "(:name (:lookup first-name) (:lookup last-name))";

        parseFile("./src/main/javacc/test/test.2.map");

        test(example1);
        test(example2);
        test(example3);

    }
    
    private void test(String example) throws ParseException {

        MappingParser parser = null;

        parser = new MappingParser( new StringReader(example));
        parser.setIParseTreeCreator(new ParseTreeCreatorTest());
        parser.parse_document();

        System.out.println("");

    }
    
    public void parseFile(String filename) throws ParseException {


        try {
            MappingParser parser = new MappingParser( new FileInputStream(filename));
            parser.setIParseTreeCreator(new ParseTreeCreatorTest());
            parser.parse_document();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        System.out.println("");

    }

}
