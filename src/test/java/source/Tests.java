package source;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.mongodb.BasicDBObject;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Calendar;
import java.util.Iterator;

public class Tests {

    @Test
    public void createConferenceRecord() {
        /*BasicDBObject obj = new BasicDBObject();

        obj.put("title", "ACM International Conference on Multimedia 2009");
        obj.put("short", "ACM MM 2009");
        obj.put("start-date", getDate(2009, Calendar.OCTOBER, 19));
        obj.put("end-date", getDate(2009, Calendar.OCTOBER, 23));
        obj.put("location", "Beijing Hotel, Beijing, China");
        obj.put("url", "http://www.sigmm.org/archive/MM/mm09/index.htm");

        */
        System.out.println(getDate(2011, Calendar.AUGUST, 29));
        System.out.println(getDate(2011, Calendar.SEPTEMBER, 3));
    }

    public long getDate(int year, int month, int date) {
        Calendar c = Calendar.getInstance();
        c.set(year, month-1, date);
        return c.getTimeInMillis();
    }

    //@Test
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
                " WHERE { _:b0 <http://cuenet/address> ?z . } ";

        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecutor = QueryExecutionFactory.create(query, model);

        ResultSet results = queryExecutor.execSelect();

        while (results.hasNext()) {
            QuerySolution qs = results.next();
            Literal l = qs.getLiteral("?z");
            System.out.println(l.getDatatypeURI());
            System.out.println(qs.getLiteral("?z"));
        }

        ResultSetFormatter.out(System.out, results, query);

    }

    //@Test
    public void listClassTest() throws FileNotFoundException {
        OntModel model = ModelFactory.createOntologyModel();

        model.read(new FileReader("/home/arjun/Documents/Dropbox/Ontologies/cuenet-main/cuenet-main.owl"),
                "http://www.semanticweb.org/arjun/cuenet-main.owl");

        OntClass c = model.getOntClass("http://www.semanticweb.org/arjun/cuenet-main.owl#coordinates");
        System.out.println(c == null);

        DatatypeProperty dp = model.getDatatypeProperty("http://www.semanticweb.org/arjun/cuenet-main.owl#latitude");
        System.out.println(dp == null);

        Iterator<OntClass> it = model.listClasses();
        while (it.hasNext()) {
            OntClass i = it.next();
            System.out.println("Logger: " + i.getURI());
        }
    }

}
