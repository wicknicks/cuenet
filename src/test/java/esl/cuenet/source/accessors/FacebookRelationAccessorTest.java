package esl.cuenet.source.accessors;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.mongodb.BasicDBList;
import com.mongodb.util.JSON;
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

public class FacebookRelationAccessorTest extends TestBase {

    Logger logger = Logger.getLogger(FacebookRelationAccessorTest.class);

    public FacebookRelationAccessorTest() {
        super();
    }

    @Test
    public void doTest() throws SourceQueryException, ParseException, IOException {

        QueryRelationsTest qrt = new QueryRelationsTest();
        FacebookRelationAccessor accessor = new FacebookRelationAccessor(qrt.getModel());

        accessor.start();
        IResultSet rs = accessor.executeQuery(717562539);
        //BasicDBList k = (BasicDBList) JSON.parse(rs.printResults());
        //logger.info("717562539 has " + k.size() + " relationships");

        rs = accessor.executeQuery(111290);
        logger.info(rs.printResults());

        rs = accessor.executeQuery(570206500);
        logger.info(rs.printResults());

    }

    //@Test
    public void sourceQueryTest() throws IOException, ParseException {
        QueryRelationsTest qrt = new QueryRelationsTest();
        qrt.query();
    }

    public class QueryRelationsTest extends BaseAlgorithm {

        public QueryRelationsTest() throws IOException, ParseException {
            super();
        }

        private void tQuery() throws SourceQueryException, AccesorInitializationException {
            ISource source = sourceMapper.get("fb-relation");
            IRelationGraph relationGraph = source.getRelationGraph();
            IMapper mapper = source.getMapper();

            OntClass personClass = model.getOntClass("http://www.semanticweb.org/arjun/cuenet-main.owl#person");
            DatatypeProperty nameProperty = model.getDatatypeProperty("http://www.semanticweb.org/arjun/cuenet-main.owl#name");
            Individual person = personClass.createIndividual(
                    "http://www.semanticweb.org/arjun/cuenet-main.owl#person-fb_717562539");
            person.addLiteral(nameProperty, "Arjun Satish");

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

            /* set the query attributes */
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
