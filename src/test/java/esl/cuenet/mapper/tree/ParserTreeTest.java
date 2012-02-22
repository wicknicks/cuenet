package esl.cuenet.mapper.tree;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import esl.cuenet.mapper.parser.MappingParser;
import esl.cuenet.mapper.parser.ParseException;
import esl.cuenet.query.IResultSet;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.ISource;
import esl.cuenet.source.SourceQueryException;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ParserTreeTest {

    private Logger logger = Logger.getLogger(ParserTreeTest.class);

    @Test
    public void runParserTreeTests() throws ParseException, SourceQueryException, AccesorInitializationException {

//        File file = new File("./src/main/javacc/test/");
//        File[] files = file.listFiles(new FilenameFilter() {
//            @Override
//            public boolean accept(File dir, String name) {
//                return (name.contains("test"));
//            }
//        });

        SourceMapper mapper = parseFileToSourceMapper("./mappings/sources.map");

        ISource geocoderSource = mapper.get("yahoo-geocoder");
        IResultSet result = geocoderSource.query(new String[] {"coordinates.latitude",
                "coordinates.longitude"}, prepareLatLonLiterals());
//            IResultSet result = geocoderSource.query(new String[]{"address.street-address"},
//                    prepareAddressLiterals());
        logger.info(result.printResults());

//        for (File filename: files) {
//            logger.info("Parsing " + filename.getAbsolutePath());
//            parseFile(filename.getAbsolutePath());
//        }

    }

    public SourceMapper parseFileToSourceMapper(String filename) throws ParseException {

        SourceMapper m = null;
        
        try {
            MappingParser parser = new MappingParser(new FileInputStream(filename));
            parser.setIParseTreeCreator(new ParseTree(filename));
            IParseTree tree = parser.parse_document();

            ParseTreeInterpreter interpreter = new ParseTreeInterpreter(tree);
            interpreter.interpret();

            m = interpreter.getSourceMapper();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SourceParseException e) {
            e.printStackTrace();
        }


        return m;
    }

    public Literal[] prepareLatLonLiterals() {

        OntModel model = ModelFactory.createOntologyModel();

        Resource coordinate = model.createResource("http://cuenet/coordinate");
        Property latitude = model.createProperty("http://cuenet/latitude");
        Property longitude = model.createProperty("http://cuenet/longitude");
        Property address = model.createProperty("http://cuenet/address");

        Individual c0 = model.createIndividual(coordinate);
        c0.addLiteral(latitude, 33.645686);
        c0.addLiteral(longitude, -117.944156);
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
        }

        literals[0] = l;
        return literals;
    }

}
