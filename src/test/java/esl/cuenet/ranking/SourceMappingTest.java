package esl.cuenet.ranking;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import esl.cuenet.query.IResultSet;
import esl.cuenet.ranking.network.NeoOntoInstanceImporter;
import esl.cuenet.ranking.network.NeoOntologyImporter;
import esl.cuenet.ranking.network.PersistentEventEntityNetwork;
import esl.cuenet.ranking.sources.EmailSource;
import esl.cuenet.ranking.sources.FacebookPhotoSource;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.SourceQueryException;
import esl.cuenet.source.accessors.EmailAccessor;
import esl.cuenet.source.accessors.Utils;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import test.utils.DateTimeParser;

import javax.mail.internet.MailDateFormat;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SourceMappingTest {

    private Logger logger = Logger.getLogger(SourceMappingTest.class);
    private static OntModel model = null;


    @BeforeClass
    public static void setup() {
        SysLoggerUtils.initLogger();
        model = ModelFactory.createOntologyModel();

        try {
            model.read(new FileReader("/home/arjun/Documents/Dropbox/Ontologies/cuenet-main/cuenet-main.owl"),
                    "http://www.semanticweb.org/arjun/cuenet-main.owl");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private class RankerEmailAccessor extends EmailAccessor {

        public RankerEmailAccessor(OntModel model) {
            super(model);
        }

        @Override
        public IResultSet executeQuery() throws SourceQueryException {
            return query();
        }

        private IResultSet query() {
            DBReader reader = this.startReader("emails");
            if ( !setFlags[0] && !setFlags[1] && !setFlags[2] ) {
                logger.info("No flags set. Empty return set");
                return null;
            }

            BasicDBList predicates = new BasicDBList();
            for (String em: queryEmails) {
                Pattern emPattern = Pattern.compile(em, Pattern.CASE_INSENSITIVE);
                predicates.add(new BasicDBObject("to", emPattern));
                predicates.add(new BasicDBObject("from", emPattern));
                predicates.add(new BasicDBObject("cC", emPattern));
            }

            BasicDBObject keys = new BasicDBObject();
            keys.put("to", 1);
            keys.put("cc", 1);
            keys.put("from", 1);
            keys.put("_id", 0);

            BasicDBObject queryObject = new BasicDBObject("$or", predicates);
            reader.query(queryObject, keys);

            logger.info("Query: " + queryObject);

            int c = 1;
            String date, to, from, cc;
            while (reader.hasNext()) {
                BasicDBObject obj = (BasicDBObject) reader.next();
                List<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>();
                to = obj.getString("to");
                if (to != null) entries.addAll(Utils.parseEmailAddresses(to));

                from = obj.getString("from");
                if (from != null) entries.addAll(Utils.parseEmailAddresses(from));

                cc = obj.getString("cc");
                if (cc != null)entries.addAll(Utils.parseEmailAddresses(cc));

                for (Map.Entry<String, String> entry: entries) {
                    Utils.createPersonFromNameEmail(entry.getKey(), entry.getValue(), model);
                }


//                logger.info(obj.toString());

                c++;
                if (c > 100) break;
            }

            logger.info("Returning " + c + " emails.");
            return null;
        }
    }

    private static String directory = "/data/graph_db/sources";

//    @Test
    public void doTest() throws Exception {
        RankerEmailAccessor accessor = new RankerEmailAccessor(model);
        accessor.setAttributeNames(new Attribute[]{new Attribute("from"), new Attribute("to"), new Attribute("cc")});
        accessor.associateString(new Attribute("to"), "Fabian.Groffen@cwi.nl");
        accessor.executeQuery();

        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            Statement statement = iter.nextStatement();
            logger.info(statement.getSubject() + " <=> " + statement.getPredicate() + " <=> " + statement.getObject());
        }
        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );
        EventEntityNetwork network = new PersistentEventEntityNetwork( graphDb );

        NeoOntologyImporter importer = new NeoOntologyImporter( model );
        importer.loadIntoGraph(network);

        graphDb.shutdown();

    }

//    @Test
    public void testEmailSourceInstantiator() {
        long a = System.currentTimeMillis();
        SourceInstantiator src = new EmailSource();
        src.populate(model);
        logger.info("Time Taken: " + (System.currentTimeMillis() - a));
    }

    @Test
    public void testFacebookPhotoSourceInstantiator() {
        long a = System.currentTimeMillis();
        SourceInstantiator src = new FacebookPhotoSource();
        src.populate(model);
        logger.info("Time Taken: " + (System.currentTimeMillis() - a));
    }

    @Test
    public void sourceInstantiationTest() {
        GraphDatabaseService graphDb = new EmbeddedGraphDatabase( directory );
        EventEntityNetwork network = new PersistentEventEntityNetwork( graphDb );
        NeoOntoInstanceImporter importer = new NeoOntoInstanceImporter(network, new SourceInstantiator[]{
                new EmailSource(), new FacebookPhotoSource()
        });

        try {
            importer.populate();
        } catch (Exception e) {

        } finally {
            graphDb.shutdown();
        }

    }

}
