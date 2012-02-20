package source;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import org.junit.Test;

public class Tests {

    @Test
    public void statementTest() {

        OntModel model = ModelFactory.createOntologyModel();

        Resource coordinate = model.createResource("http://cuenet/coordinate");
        Property latitude = model.createProperty("http://cuenet/latitude");
        Property longitude = model.createProperty("http://cuenet/longitude");
        Property address = model.createProperty("http://cuenet/address");

        Individual c0 = model.createIndividual(coordinate);
        c0.addLiteral(latitude, 33.645921);
        c0.addLiteral(longitude, -117.948732);
        c0.addLiteral(address, "Verano Pl, Irvine, CA");

        model.write(System.out);

        String queryString = "SELECT ?x ?y ?z" +
                " WHERE { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://cuenet/coordinate> . } ";

        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecutor = QueryExecutionFactory.create(query, model);

        ResultSet results = queryExecutor.execSelect();
        ResultSetFormatter.out(System.out, results, query);



    }
}
