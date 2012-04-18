package esl.cuenet.source.accessors;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.mongodb.BasicDBObject;
import esl.cuenet.algorithms.BaseAlgorithm;
import esl.cuenet.mapper.parser.ParseException;
import esl.cuenet.mapper.tree.SourceParseException;
import esl.cuenet.query.IResultSet;
import esl.cuenet.source.*;
import esl.datastructures.graph.relationgraph.IRelationGraph;
import esl.datastructures.graph.relationgraph.RelationGraphNode;
import org.apache.log4j.Logger;
import org.junit.Test;
import test.TestBase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GoogleCalendarCollectionTest extends TestBase {

    private Logger logger = Logger.getLogger(YahooPlaceFinderTest.class);

    public GoogleCalendarCollectionTest() {
        super();
    }

    @Test
    public void runTest() throws IOException, SourceQueryException, AccesorInitializationException, ParseException {

        QueryCalendarTest qrt = new QueryCalendarTest();

        GoogleCalendarCollection calendarCollection = new GoogleCalendarCollection(qrt.getModel());
        //BasicDBObject o = calendarCollection.search("arjun", 1296171000000L);
        calendarCollection.setAttributeNames(new Attribute[]{null, new Attribute("owner.email"), null, null, null, null});
        calendarCollection.associateString(new Attribute("owner.email"), "arjun.satish@gmail.com");
        IResultSet rs = calendarCollection.executeQuery();
        if (rs != null) logger.info(rs.printResults());

    }


    public void sourceQueryTest() throws FileNotFoundException, ParseException {
        QueryCalendarTest qrt = new QueryCalendarTest();
        qrt.query();
    }

    public class QueryCalendarTest extends BaseAlgorithm {

        public QueryCalendarTest() throws FileNotFoundException, ParseException, SourceParseException {
            super();
        }

        private void tQuery() throws SourceQueryException, AccesorInitializationException {
            ISource source = sourceMapper.get("google-calendar");
            IRelationGraph relationGraph = source.getRelationGraph();
            IMapper mapper = source.getMapper();

            OntClass personClass = model.getOntClass("http://www.semanticweb.org/arjun/cuenet-main.owl#person");
            DatatypeProperty nameProperty = model.getDatatypeProperty(
                    "http://www.semanticweb.org/arjun/cuenet-main.owl#name");
            DatatypeProperty emailProperty = model.getDatatypeProperty(
                    "http://www.semanticweb.org/arjun/cuenet-main.owl#email");
            Individual person = personClass.createIndividual(
                    "http://www.semanticweb.org/arjun/cuenet-main.owl#person-fb_717562539");
            person.addLiteral(nameProperty, "Arjun Satish");
            person.addLiteral(emailProperty, "arjun.satish@gmail.com");

            Statement s = person.getProperty(RDF.type);
            String personType = removeNamespace(s.getObject().toString());

            List<RelationGraphNode> queryNodes = relationGraph.getNodesOfType (personType);
            List<String> pathExpressions = new ArrayList<String>();
            List<Literal> literals = new ArrayList<Literal>();

            StmtIterator si;
            String pathExpr;
            for (RelationGraphNode queryNode: queryNodes) {
                si = person.listProperties();
                while (si.hasNext()) {
                    Statement st = si.next();
                    if ( !st.getPredicate().getURI().contains("cuenet") ) continue;
                    if (st.getObject().isLiteral()) {
                        String p = removeNamespace(st.getPredicate().getURI());

                        pathExpr = queryNode.name()  + "." + p;
                        if (!mapper.containsPattern(pathExpr)) continue;

                        Adornment a = mapper.getAdornment(pathExpr);

                        if (a != null && a.type() == Adornment.AdornmentType.Unspecifiable) continue;
                        logger.info("Query Params: " + pathExpr + ", " + st.getObject().asLiteral());
                        pathExpressions.add(pathExpr);
                        literals.add(st.getObject().asLiteral());
                    }
                }
            }

            logger.info(pathExpressions.size() + ", " + literals.size());
            IResultSet resultsSet = source.query(pathExpressions, literals);
            logger.info(resultsSet.printResults());
        }

        private String removeNamespace(String uri) {
            int ix = uri.indexOf("#");
            return uri.substring(ix+1);
        }

        public void query()  {
            try {
                tQuery();
            } catch (SourceQueryException e) {
                e.printStackTrace();
            } catch (AccesorInitializationException e) {
                e.printStackTrace();
            }
        }
    }
}
