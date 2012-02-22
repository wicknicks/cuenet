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
import esl.cuenet.source.accessors.UpcomingEventsAPI;
import esl.cuenet.source.accessors.YahooPlaceFinderAPI;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.Calendar;

public class SourceTest {

    private Logger logger = Logger.getLogger(SourceTest.class);

    @Test
    public void geocoderSourceTest() {

        IMapper mapper = new TreeMapper();
        IAccessor accessor = new YahooPlaceFinderAPI();

        Source geocoderSource = new Source("geocoder", accessor, mapper);
        geocoderSource.setAttributes(new Attribute[]{
                new Attribute("latitude"),
                new Attribute("longitude"),
                new Attribute("address")
        });

        geocoderSource.getMapper().map("coordinate.latitude", new Attribute("latitude"));
        geocoderSource.getMapper().map("coordinate.longitude", new Attribute("longitude"));
        geocoderSource.getMapper().map("coordinate.address", new Attribute("address"));

        try {
            IResultSet result = geocoderSource.query(new String[]{"coordinate.latitude",
                    "coordinate.longitude"}, prepareLatLonLiterals());
//            IResultSet result = geocoderSource.query(new String[] {"coordinate.address"},
//                    prepareAddressLiterals());
            logger.info(result.printResults());

        } catch (SourceQueryException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void upcomingSourceTest() {

        IMapper mapper = new TreeMapper();
        IAccessor accessor = new UpcomingEventsAPI();
        OntModel model = ModelFactory.createOntologyModel();

        Source geocoderSource = new Source("upcoming", accessor, mapper);
        geocoderSource.setAttributes(new Attribute[]{
                new Attribute("latitude"),
                new Attribute("longitude"),
                new Attribute("start"),
                new Attribute("end"),
                new Attribute("name"),
                new Attribute("description"),
        });

        geocoderSource.getMapper().map("coordinate.latitude", new Attribute("latitude"));
        geocoderSource.getMapper().map("coordinate.longitude", new Attribute("longitude"));
        geocoderSource.getMapper().map("interval.start", new Attribute("start"));
        geocoderSource.getMapper().map("interval.end", new Attribute("end"));

        Calendar c1 = Calendar.getInstance();
        c1.set(Calendar.YEAR, 2012);
        c1.set(Calendar.MONTH, 0);
        c1.set(Calendar.DAY_OF_MONTH, 1);

        Calendar c2 = Calendar.getInstance();
        c2.set(Calendar.YEAR, 2012);
        c2.set(Calendar.MONTH, 0);
        c2.set(Calendar.DAY_OF_MONTH, 10);

        try {
            IResultSet result = geocoderSource.query(new String[]{"coordinate.latitude",
                    "coordinate.longitude", "interval.start", "interval.end"}, new Literal[]{ model.createTypedLiteral(33.642795),
                    model.createTypedLiteral(-117.845196), model.createTypedLiteral(c1.getTimeInMillis()),
                    model.createTypedLiteral(c2.getTimeInMillis())
            });
//            IResultSet result = geocoderSource.query(new String[] {"coordinate.address"},
//                    prepareAddressLiterals());
            logger.info(result.printResults());

        } catch (SourceQueryException e) {
            e.printStackTrace();
        }

    }

    private OntModel getModel() {
        OntModel model = ModelFactory.createOntologyModel();

        Resource coordinate = model.createResource("http://cuenet/coordinate");
        Property latitude = model.createProperty("http://cuenet/latitude");
        Property longitude = model.createProperty("http://cuenet/longitude");
        Property address = model.createProperty("http://cuenet/address");

        Individual c0 = model.createIndividual(coordinate);
        c0.addLiteral(latitude, 33.645921);
        c0.addLiteral(longitude, -117.948732);
        c0.addLiteral(address, "Verano Pl, Irvine, CA");

        return model;
    }

    public Literal[] prepareLatLonLiterals() {

        OntModel model = getModel();

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

        OntModel model = getModel();

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
