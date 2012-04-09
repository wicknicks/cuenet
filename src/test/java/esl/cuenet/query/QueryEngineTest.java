package esl.cuenet.query;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.ProfileRegistry;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.VCARD;
import esl.cuenet.mapper.parser.MappingParser;
import esl.cuenet.mapper.parser.ParseException;
import esl.cuenet.mapper.tree.*;
import org.junit.Test;
import test.TestBase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class QueryEngineTest extends TestBase {

    @Test
    public void parseSingleQuery() {
//        OntModel model = ModelFactory.createOntologyModel(ProfileRegistry.OWL_LANG);
//
//        String personURI = "http://somewhere/arjun-satish-uri";
//        String givenName = "Arjun";
//        String familyName = "Satish";
//        String fullName = givenName + " " + familyName;
//
//        model.createResource(personURI).addProperty(VCARD.FN, fullName)
//                .addProperty(VCARD.N, model.createResource()
//                        .addProperty(VCARD.Given, givenName)
//                        .addProperty(VCARD.Family, familyName));
//
//        personURI = "http://somewhere/john-smith-uri";
//        givenName = "John";
//        familyName = "Smith";
//        fullName = givenName + " " + familyName;
//
//        model.createResource(personURI).addProperty(VCARD.FN, fullName)
//                .addProperty(VCARD.N, model.createResource()
//                        .addProperty(VCARD.Given, givenName)
//                        .addProperty(VCARD.Family, familyName));
//
//        personURI = "http://somewhere/adarsh-satish-uri";
//        givenName = "Adarsh";
//        familyName = "Satish";
//        fullName = givenName + " " + familyName;
//
//        model.createResource(personURI).addProperty(VCARD.FN, fullName)
//                .addProperty(VCARD.N, model.createResource()
//                        .addProperty(VCARD.Given, givenName)
//                        .addProperty(VCARD.Family, familyName));

//        String queryString = "SELECT ?x ?givenName ?name" +
//                " WHERE { " +
//                "?x <http://www.w3.org/2001/vcard-rdf/3.0#FN> ?fullname . " +
//                "?x <http://www.w3.org/2001/vcard-rdf/3.0#N> ?name . " +
//                "?x <http://www.w3.org/2001/vcard-rdf/3.0#AGE> 40 . " +
//                "?name <http://www.w3.org/2001/vcard-rdf/3.0#Given> ?givenName . " +
//                " FILTER (?x!=<http://somewhere/arjun-satish-uri> && ?givenName!=\"Nujre\") }";


        OntModel model = ModelFactory.createOntologyModel();

        try {
            model.read(new FileReader("/home/arjun/Documents/Dropbox/Ontologies/cuenet-main/cuenet-main.owl"),
                    "http://www.semanticweb.org/arjun/cuenet-main.owl");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        SourceMapper mapper = getSourceMapper(model, "./mappings/sources.map");

        String queryString = "SELECT ?x " +
                " WHERE { " +
                "?x <" + RDF.type + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#event> . " +
                "?p <http://www.semanticweb.org/arjun/cuenet-main.owl#participant-in> ?x ." +
                "?p <" + RDF.type + "> <http://www.semanticweb.org/arjun/cuenet-main.owl#person> ." +
                "?p <http://www.semanticweb.org/arjun/cuenet-main.owl#email> \"arjun.satish@gmail.com\" ." +
                "?p <http://www.semanticweb.org/arjun/cuenet-main.owl#name> \"Arjun Satish\" }";
        System.out.println(queryString);


        QueryEngine engine = new QueryEngine(model, mapper);
        engine.execute(queryString);


    }

    private SourceMapper getSourceMapper(OntModel model,String filename) {
        MappingParser parser = null;
        try {
            parser = new MappingParser(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        parser.setIParseTreeCreator(new ParseTree(filename));
        IParseTree tree = null;
        try {
            tree = parser.parse_document();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ParseTreeInterpreter interpreter = new ParseTreeInterpreter(tree);
        interpreter.setOntologyModel(model);
        try {
            interpreter.interpret();
        } catch (SourceParseException e) {
            e.printStackTrace();
        }

        return interpreter.getSourceMapper();
    }

}