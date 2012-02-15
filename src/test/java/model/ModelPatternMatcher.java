package model;

import com.hp.hpl.jena.ontology.ProfileRegistry;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.VCARD;
import org.junit.Test;

public class ModelPatternMatcher {

    @Test
    public void main() {

        Model model = ModelFactory.createOntologyModel(ProfileRegistry.OWL_LANG);

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

        StmtIterator iter = model.listStatements();
        while(iter.hasNext()) {
            Statement statement = iter.nextStatement();
            Resource subject = statement.getSubject();
            Property predicate = statement.getPredicate();
            RDFNode object = statement.getObject();

            System.out.print(subject.toString() + " ");
            System.out.print(predicate.toString() + " ");
            if (object instanceof Resource) {
                System.out.print(object.toString());
            } else
            System.out.print(" \"" + object.toString() + "\"");

            System.out.println(" .");
        }

        model.write(System.out);

        String queryString = "SELECT ?givenName" +
                " WHERE { ?y <http://www.w3.org/2001/vcard-rdf/3.0#Family> \"Satish\" . " +
                         "?y <http://www.w3.org/2001/vcard-rdf/3.0#Given> ?givenName . }";

        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecutor = QueryExecutionFactory.create(query, model);

        ResultSet results = queryExecutor.execSelect();
        ResultSetFormatter.out(System.out, results, query);

        queryExecutor.close();
    }
}