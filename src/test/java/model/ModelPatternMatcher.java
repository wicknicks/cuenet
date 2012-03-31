package model;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.ProfileRegistry;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.*;
import com.hp.hpl.jena.vocabulary.VCARD;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

public class ModelPatternMatcher {

    static { SysLoggerUtils.initLogger(); }

    @Test
    public void main() {

        Model model = ModelFactory.createOntologyModel(ProfileRegistry.OWL_LANG);

        String personURI = "http://somewhere/arjun-satish-uri";
        String givenName = "Arjun";
        String familyName = "Satish";
        String fullName = givenName + " " + familyName;

        model.createResource(personURI).addProperty(VCARD.FN, fullName)
                .addProperty(VCARD.N, model.createResource()
                        .addProperty(VCARD.Given, givenName)
                        .addProperty(VCARD.Family, familyName));

        personURI = "http://somewhere/john-smith-uri";
        givenName = "John";
        familyName = "Smith";
        fullName = givenName + " " + familyName;

        model.createResource(personURI).addProperty(VCARD.FN, fullName)
                .addProperty(VCARD.N, model.createResource()
                        .addProperty(VCARD.Given, givenName)
                        .addProperty(VCARD.Family, familyName));

        personURI = "http://somewhere/adarsh-satish-uri";
        givenName = "Adarsh";
        familyName = "Satish";
        fullName = givenName + " " + familyName;

        model.createResource(personURI).addProperty(VCARD.FN, fullName)
                .addProperty(VCARD.N, model.createResource()
                        .addProperty(VCARD.Given, givenName)
                        .addProperty(VCARD.Family, familyName));

        StmtIterator iter = model.listStatements();
//        while(iter.hasNext()) {
//            Statement statement = iter.nextStatement();
//            Resource subject = statement.getSubject();
//            Property predicate = statement.getPredicate();
//            RDFNode object = statement.getObject();
//
//            System.out.print(subject.toString() + " ");
//            System.out.print(predicate.toString() + " ");
//            if (object instanceof Resource) {
//                System.out.print(object.toString());
//            } else
//            System.out.print(" \"" + object.toString() + "\"");
//
//            System.out.println(" .");
//        }

//        model.write(System.out);

        String queryString = "SELECT ?x ?givenName ?name" +
                " WHERE { " +
                         "?x <http://www.w3.org/2001/vcard-rdf/3.0#FN> ?fullname . " +
                         "?x <http://www.w3.org/2001/vcard-rdf/3.0#N> ?name . " +
                         "?name <http://www.w3.org/2001/vcard-rdf/3.0#Given> ?givenName . " +
                         //"?name <http://www.w3.org/2001/vcard-rdf/3.0#Family> \"Satish\" . " +
                         //" FILTER (?givenName != \"Arjun\" && ?givenName != \"Nujre\") } ";
                         " FILTER (?x=<http://somewhere/arjun-satish-uri>) }"; // URI Filtering
                         // " FILTER (?givenName != \"Arjun\" && ?givenName != \"Nujre\") } ";
                         //" MINUS { (?y <http://www.w3.org/2001/vcard-rdf/3.0#Given> \"Arjun\") UNION " +
                         //"         (?y <http://www.w3.org/2001/vcard-rdf/3.0#Given> \"Nujre\") } }";

        Query query = QueryFactory.create(queryString);
        query.getQueryPattern().visit(new EV());
        QueryExecution queryExecutor = QueryExecutionFactory.create(query, model);

        ResultSet results = queryExecutor.execSelect();
        ResultSetFormatter.out(System.out, results, query);

        queryExecutor.close();
    }
    
    public class EV implements ElementVisitor {
        
        Logger logger = Logger.getLogger(EV.class);

        @Override
        public void visit(ElementTriplesBlock el) {
            logger.info("Visit: ElementTriplesBlock");
            for (Triple statement: el.getPattern()) {
                Node subject = statement.getSubject();
                Node predicate = statement.getPredicate();
                Node object = statement.getObject();

                System.out.print(subject.toString() + " ");
                System.out.print(predicate.toString() + " ");
                System.out.print(object.toString() + "\n");
            }
        }

        @Override
        public void visit(ElementPathBlock el) {
            logger.info("Visit: ElementPathBlock");
            for (TriplePath tp: el.getPattern().getList()) {
                logger.info("Visit Element Path Block: " + tp.toString());
            }
        }

        @Override
        public void visit(ElementFilter elementFilter) {
            logger.info("Visit: ElementFilter");
            Expr expr = elementFilter.getExpr();
            expr.getVarName();
        }

        @Override
        public void visit(ElementAssign el) {
            logger.info("Visit: ElementAssign");
        }

        @Override
        public void visit(ElementBind elementBind) {
            logger.info("Visit: ElementBind");
        }

        @Override
        public void visit(ElementUnion el) {
            logger.info("Visit: ElementUnion");
        }

        @Override
        public void visit(ElementOptional el) {
            logger.info("Visit: ElementOptional");
        }

        @Override
        public void visit(ElementGroup el) {
            logger.info("Visit: ElementGroup");
            for (Element m: el.getElements()) {
                m.visit(this);
            }
        }

        @Override
        public void visit(ElementDataset el) {
            logger.info("Visit: ElementDataset");
        }

        @Override
        public void visit(ElementNamedGraph el) {
            logger.info("Visit: ElementNamedGraph");
        }

        @Override
        public void visit(ElementExists elementExists) {
            logger.info("Visit: ElementExists");
        }

        @Override
        public void visit(ElementNotExists elementNotExists) {
            logger.info("Visit: ElementNotExists");
        }

        @Override
        public void visit(ElementMinus elementMinus) {
            logger.info("Visit: ElementMinus");
        }

        @Override
        public void visit(ElementService el) {
            logger.info("Visit: ElementService");
        }

        @Override
        public void visit(ElementFetch el) {
            logger.info("Visit: ElementFetch");
        }

        @Override
        public void visit(ElementSubQuery el) {
            logger.info("Visit: ElementSubQuery");
        }
    }
 }
