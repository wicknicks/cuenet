package source;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.lib.iterator.Iter;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import esl.cuenet.query.QueryOperator;
import org.junit.Test;

import java.util.Iterator;

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

        String queryString = "SELECT distinct ?y ?z" +
                " WHERE { _:b0 <http://cuenet/longitude> ?z . } ";

        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecutor = QueryExecutionFactory.create(query, model);

        ResultSet results = queryExecutor.execSelect();

        while (results.hasNext()) {
            QuerySolution qs = results.next();
            Literal l = qs.getLiteral("?z");
            System.out.println(l.getValue());
            System.out.println(qs.getLiteral("?z"));
        }

        ResultSetFormatter.out(System.out, results, query);

    }

}
