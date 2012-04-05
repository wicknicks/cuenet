package esl.cuenet.algorithms.collation;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import esl.cuenet.mapper.parser.MappingParser;
import esl.cuenet.mapper.parser.ParseException;
import esl.cuenet.mapper.tree.*;
import esl.cuenet.query.QueryEngine;
import org.junit.Test;
import test.TestBase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class FirstKAlgorithmTest extends TestBase {

    public FirstKAlgorithmTest() {
        super();
    }

    protected OntModel model = null;
    protected SourceMapper sourceMapper = null;
    protected String mappingsFile = "./mappings/sources.map";

    @Test
    public void testFirstKAlgorithm() throws FileNotFoundException, ParseException, SourceParseException {
        OntModel model = ModelFactory.createOntologyModel();

        model.read(new FileReader("/home/arjun/Documents/Dropbox/Ontologies/cuenet-main/cuenet-main.owl"),
                "http://www.semanticweb.org/arjun/cuenet-main.owl");

        MappingParser parser = new MappingParser(new FileInputStream(mappingsFile));
        parser.setIParseTreeCreator(new ParseTree(mappingsFile));
        IParseTree tree = parser.parse_document();

        ParseTreeInterpreter interpreter = new ParseTreeInterpreter(tree);
        interpreter.setOntologyModel(model);
        interpreter.interpret();

        sourceMapper = interpreter.getSourceMapper();

        String queryString = "SELECT ?x ?givenName ?name" +
                " WHERE { " +
                "?x <type-of> <http://cn/person> . " +
                "?x <http://www.w3.org/2001/vcard-rdf/3.0#FN> ?fullname . " +
                "?x <http://www.w3.org/2001/vcard-rdf/3.0#N> ?name . " +
                "?x <http://www.w3.org/2001/vcard-rdf/3.0#AGE> 40 . " +
                "?name <http://www.w3.org/2001/vcard-rdf/3.0#Given> ?givenName . " +
                " FILTER (?x!=<http://somewhere/arjun-satish-uri> && ?givenName!=\"Nujre\") }";

        QueryEngine engine = new QueryEngine(model, sourceMapper);
        engine.execute(queryString);
    }

}
