package source;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import esl.cuenet.query.IResultSet;
import esl.cuenet.source.*;
import esl.cuenet.source.accessors.YahooPlaceFinderAPI;
import org.apache.log4j.Logger;
import org.junit.Test;

public class SourceTest {

    private Logger logger = Logger.getLogger(SourceTest.class);

    @Test
    public void gecoderSourceTest() {

        IMapper mapper = new TreeMapper();
        IAccessor accessor = new YahooPlaceFinderAPI();

        Source geocoderSource = new Source("geocoder", accessor, mapper);
        geocoderSource.setAttributes(new Attribute[] {
                new Attribute("latitude"),
                new Attribute("longitude"),
                new Attribute("address")
        });

        geocoderSource.getMapper().map("coordinate.latitude", new Attribute("latitude"));
        geocoderSource.getMapper().map("coordinate.longitude", new Attribute("longitude"));
        geocoderSource.getMapper().map("coordinate.address", new Attribute("address"));

        try {
//            IResultSet result = geocoderSource.query(new String[] {"coordinate.latitude",
//                    "coordinate.longitude"}, prepareLatLonLiterals());
            IResultSet result = geocoderSource.query(new String[] {"coordinate.address"},
                    prepareAddressLiterals());
            logger.info(result.printResults());

        } catch (SourceQueryException e) {
            e.printStackTrace();
        }

    }

    public Literal[] prepareLatLonLiterals() {

        OntModel model = ModelFactory.createOntologyModel();

        Resource coordinate = model.createResource("http://cuenet/coordinate");
        Property latitude = model.createProperty("http://cuenet/latitude");
        Property longitude = model.createProperty("http://cuenet/longitude");
        Property address = model.createProperty("http://cuenet/address");

        Individual c0 = model.createIndividual(coordinate);
        c0.addLiteral(latitude, 33.645921);
        c0.addLiteral(longitude, -117.948732);
        c0.addLiteral(address, "Verano Pl, Irvine, CA");

        String queryString = "SELECT distinct ?y ?z" +
                " WHERE { _:b0 <http://cuenet/latitude> ?z . } ";

        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecutor = QueryExecutionFactory.create(query, model);

        Literal[] literals = new Literal[2];
        ResultSet results = queryExecutor.execSelect();

        Literal l = null;
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            l = qs.getLiteral("?z");
            System.out.println(l.getDatatypeURI());
            System.out.println(qs.getLiteral("?z"));
        }

        literals[0] = l;

        queryString = "SELECT distinct ?y ?z" +
                " WHERE { _:b0 <http://cuenet/longitude> ?z . } ";

        query = QueryFactory.create(queryString);
        queryExecutor = QueryExecutionFactory.create(query, model);

        results = queryExecutor.execSelect();

        while (results.hasNext()) {
            QuerySolution qs = results.next();
            l = qs.getLiteral("?z");
            System.out.println(l.getDatatypeURI());
            System.out.println(qs.getLiteral("?z"));
        }

        literals[1] = l;
        return literals;
    }

    public Literal[] prepareAddressLiterals() {

        OntModel model = ModelFactory.createOntologyModel();

        Resource coordinate = model.createResource("http://cuenet/coordinate");
        Property latitude = model.createProperty("http://cuenet/latitude");
        Property longitude = model.createProperty("http://cuenet/longitude");
        Property address = model.createProperty("http://cuenet/address");

        Individual c0 = model.createIndividual(coordinate);
        c0.addLiteral(latitude, 33.645921);
        c0.addLiteral(longitude, -117.948732);
        c0.addLiteral(address, "Verano Pl, Irvine, CA");

        String queryString = "SELECT distinct ?y ?z" +
                " WHERE { _:b0 <http://cuenet/address> ?z . } ";

        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecutor = QueryExecutionFactory.create(query, model);

        Literal[] literals = new Literal[1];
        ResultSet results = queryExecutor.execSelect();

        Literal l = null;
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            l = qs.getLiteral("?z");
            System.out.println(l.getDatatypeURI());
            System.out.println(qs.getLiteral("?z"));
        }

        literals[0] = l;
        return literals;
    }


}
