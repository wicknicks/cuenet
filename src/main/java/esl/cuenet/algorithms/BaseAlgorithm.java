package esl.cuenet.algorithms;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import esl.cuenet.mapper.parser.MappingParser;
import esl.cuenet.mapper.parser.ParseException;
import esl.cuenet.mapper.tree.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;

public abstract class BaseAlgorithm implements IAlgorithm {

    protected OntModel model = null;
    protected SourceMapper sourceMapper = null;
    protected String mappingsFile = "./mappings/sources.map";

    public BaseAlgorithm() throws FileNotFoundException, ParseException, SourceParseException {
        model = ModelFactory.createOntologyModel();

        model.read(new FileReader("/home/arjun/Documents/Dropbox/Ontologies/cuenet-main/cuenet-main.owl"),
                "http://www.semanticweb.org/arjun/cuenet-main.owl");

        MappingParser parser = new MappingParser(new FileInputStream(mappingsFile));
        parser.setIParseTreeCreator(new ParseTree(mappingsFile));
        IParseTree tree = parser.parse_document();

        ParseTreeInterpreter interpreter = new ParseTreeInterpreter(tree);
        interpreter.setOntologyModel(model);
        interpreter.interpret();

        sourceMapper = interpreter.getSourceMapper();
    }

}
