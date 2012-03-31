package model;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import org.junit.Test;

public class DBPediaQuery {

    @Test
    public void d() {
        String service = "http://dbpedia.org/sparql";
        String query = "select distinct ?Concept where {[] a ?Concept}";
        QueryExecution qe = QueryExecutionFactory.sparqlService(service, query);
        try {
            ResultSet rset = qe.execSelect();
            while (rset.hasNext()) {
                QuerySolution qs = rset.next();
                System.out.println(qs.toString());
            }
        } catch (QueryExceptionHTTP e) {
            System.out.println(service + " is DOWN");
        } finally {
            qe.close();
        }
    }

}
