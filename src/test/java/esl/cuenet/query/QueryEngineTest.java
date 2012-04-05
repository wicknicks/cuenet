package esl.cuenet.query;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.ProfileRegistry;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.VCARD;
import org.junit.Test;
import test.TestBase;

public class QueryEngineTest extends TestBase {

    @Test
    public void parseSingleQuery() {
        OntModel model = ModelFactory.createOntologyModel(ProfileRegistry.OWL_LANG);

        String personURI = "http://somewhere/arjun-satish-uri";
        String givenName = "Arjun";
        String familyName = "Satish";
        String fullName = givenName + " " + familyName;

        model.createResource(personURI).addProperty(VCARD.FN, fullName)
                .addProperty(VCARD.N, model.createResource()
                        .addProperty(VCARD.Given, givenName)
                        .addProperty(VCARD.Family, familyName));

        personURI = "http://somewhere/john-smith-uri";
        givenName = "John";
        familyName = "Smith";
        fullName = givenName + " " + familyName;

        model.createResource(personURI).addProperty(VCARD.FN, fullName)
                .addProperty(VCARD.N, model.createResource()
                        .addProperty(VCARD.Given, givenName)
                        .addProperty(VCARD.Family, familyName));

        personURI = "http://somewhere/adarsh-satish-uri";
        givenName = "Adarsh";
        familyName = "Satish";
        fullName = givenName + " " + familyName;

        model.createResource(personURI).addProperty(VCARD.FN, fullName)
                .addProperty(VCARD.N, model.createResource()
                        .addProperty(VCARD.Given, givenName)
                        .addProperty(VCARD.Family, familyName));

        String queryString = "SELECT ?x ?givenName ?name" +
                " WHERE { " +
                "?x <http://www.w3.org/2001/vcard-rdf/3.0#FN> ?fullname . " +
                "?x <http://www.w3.org/2001/vcard-rdf/3.0#N> ?name . " +
                "?x <http://www.w3.org/2001/vcard-rdf/3.0#AGE> 40 . " +
                "?name <http://www.w3.org/2001/vcard-rdf/3.0#Given> ?givenName . " +
                " FILTER (?x!=<http://somewhere/arjun-satish-uri> && ?givenName!=\"Nujre\") }";

        QueryEngine engine = new QueryEngine(model, null);
        engine.execute(queryString);


    }

}
