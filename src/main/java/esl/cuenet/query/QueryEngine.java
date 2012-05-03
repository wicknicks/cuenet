package esl.cuenet.query;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDDateTimeType;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.*;
import com.hp.hpl.jena.vocabulary.RDF;
import esl.cuenet.mapper.tree.SourceMapper;
import esl.cuenet.model.Constants;
import esl.cuenet.source.*;
import esl.datastructures.graph.relationgraph.IRelationGraph;
import esl.datastructures.graph.relationgraph.RelationGraphEdge;
import esl.datastructures.graph.relationgraph.RelationGraphNode;
import org.apache.log4j.Logger;

import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.List;

public class QueryEngine {

    Logger logger = Logger.getLogger(QueryEngine.class);

    private OntModel model = null;
    private QueryGraph queryGraph = new QueryGraph();
    private SourceMapper sourceMapper = null;
    private List<String> projectTypeURIs = null;

    public QueryEngine(OntModel model, SourceMapper sourceMapper) {
        this.model = model;
        this.sourceMapper = sourceMapper;
    }

    public void execute(String sparqlQuery) {

        String ns = "http://www.semanticweb.org/arjun/cuenet-main.owl";
        Query query = QueryFactory.create(sparqlQuery);
        query.getQueryPattern().visit(new ElementVisitorImpl(queryGraph));

        List<Var> projectVars = query.getProjectVars();
        projectTypeURIs = new ArrayList<String>();
        for (Var projectVar: projectVars) {
            RelationGraphNode node =  queryGraph.getNodeByName(projectVar.toString());
            for (RelationGraphEdge edge: queryGraph.getOutgoingEdges(node)) {
                if (edge.label().equalsIgnoreCase(RDF.type.getURI()))
                    projectTypeURIs.add(queryGraph.getDestinationNode(edge).name());
            }
        }

        logger.info("Finished parsing query");

        // convert query triples to individuals and their attributes
        List<RelationGraphNode> allTypedNodes = queryGraph.getAllTypedNodes();
        List<RelationGraphNode> attributeNodes = new ArrayList<RelationGraphNode>();
        for (RelationGraphNode node : allTypedNodes) {
            boolean flag = true;
            for (Var projectVar : projectVars)
                if (node.name().equalsIgnoreCase(projectVar.toString())) flag = false;
            if (flag) attributeNodes.add(node);
        }

        List<Individual> individuals = new ArrayList<Individual>();

        for (RelationGraphNode attrib : attributeNodes) {
            OntClass cl = model.getOntClass(ns + attrib.name());
            Individual ind = model.createIndividual(cl);
            for (RelationGraphEdge edge : queryGraph.getOutgoingEdges(attrib)) {
                RelationGraphNode dest = queryGraph.getDestinationNode(edge);
                if (edge.label().equalsIgnoreCase(RDF.type.getURI()))
                    ind.addProperty(RDF.type, model.getOntClass(dest.name()));
                else {
                    Property property = model.getProperty(ns + edge.label());
                    ind.addLiteral(property, dest.name());
                }
            }
            individuals.add(ind);
        }



        for (Var projectVar : projectVars) {
            RelationGraphNode node = queryGraph.getNodeByName(projectVar.toString());
            OntClass cl = model.getOntClass(ns + node.name());
            Individual individual = model.createIndividual(cl);
            boolean added = false;
            for (RelationGraphEdge edge: queryGraph.getOutgoingEdges(node)) {
                if(edge.label().compareTo(Constants.CuenetNamespace+ Constants.OccursDuring) == 0) {
                    RelationGraphNode dest = queryGraph.getDestinationNode(edge);
                    Literal l = model.createTypedLiteral(dest.name(), XSDDatatype.XSDdateTime);
                    added = true;
                    individual.addProperty(model.getProperty(Constants.CuenetNamespace+ Constants.OccursDuring), l);
                }

                if (edge.label().equalsIgnoreCase(RDF.type.getURI())) {
                    RelationGraphNode dest = queryGraph.getDestinationNode(edge);
                    individual.addProperty(RDF.type, model.getOntClass(dest.name()));
                }
            }

            if (added)
                individuals.add(individual);
        }




        eval(individuals);
        clear();
    }

    private void clear() {
        queryGraph = new QueryGraph();
    }

    private void eval(List<Individual> individuals) {
        if (sourceMapper == null) {
            logger.info("No sources added. Returning");
            return;
        }

        sourceMapper.accept(new SourceVisitor(queryGraph, individuals));
    }

    public class SourceVisitor implements SourceMapVisitor {
        private QueryGraph graph;
        private List<Individual> inputIndividuals;

        public SourceVisitor(QueryGraph graph, List<Individual> inputIndividuals) {
            this.graph = graph;
            this.inputIndividuals = inputIndividuals;
        }

        @Override
        public IResultSet visit(ISource source) {
            logger.info("Visiting " + source.getName());

            //check if source describe with any project URI

            IRelationGraph relationGraph = source.getRelationGraph();
            IMapper mapper = source.getMapper();

            boolean flag = false;
            for (String uri: projectTypeURIs) {
                List<RelationGraphNode> nodes = relationGraph.getNodesOfType(removeNamespace(uri));
                if (nodes != null && nodes.size() > 0)
                    flag = true;
                logger.info("Searching for: " + uri);
            }

            logger.info("Source suitable for querying: " + flag);
            if (!flag) return null;

            List<String> pathExpressions = new ArrayList<String>();
            List<Literal> literals = new ArrayList<Literal>();

            for (Individual ind : inputIndividuals) {

                Statement s = ind.getProperty(RDF.type);
                String personType = removeNamespace(s.getObject().toString());

                List<RelationGraphNode> queryNodes = relationGraph.getNodesOfType(personType);

                StmtIterator si;
                String pathExpr;
                for (RelationGraphNode queryNode : queryNodes) {
                    si = ind.listProperties();
                    while (si.hasNext()) {
                        Statement st = si.next();
                        if (!st.getPredicate().getURI().contains("cuenet")) continue;
                        if (st.getObject().isLiteral()) {
                            String p = removeNamespace(st.getPredicate().getURI());

                            pathExpr = queryNode.name() + "." + p;
                            if (!mapper.containsPattern(pathExpr)) continue;

                            Adornment a = mapper.getAdornment(pathExpr);

                            if (a != null && a.type() == Adornment.AdornmentType.Unspecifiable) continue;
                            logger.info("Query Params: " + pathExpr + ", " + st.getObject().asLiteral());
                            pathExpressions.add(pathExpr);
                            literals.add(st.getObject().asLiteral());
                        }
                    }
                }
            }

            if (pathExpressions.size() == 0 || literals.size() == 0 ) {
                logger.info("Zero input predicates for " + source.getName());
                return null;
            }

            /* set the query attributes */
            IResultSet resultsSet = null;
            try {
                resultsSet = source.query(pathExpressions, literals);
            } catch (SourceQueryException e) {
                e.printStackTrace();
            } catch (AccesorInitializationException e) {
                e.printStackTrace();
            }

            if (resultsSet != null) logger.info(resultsSet.printResults());
            else logger.info("NULL Result Set");

            return resultsSet;
        }

        private String removeNamespace(String uri) {
            int ix = uri.indexOf("#");
            return uri.substring(ix+1);
        }

    }

    public class ElementVisitorImpl implements ElementVisitor {

        private QueryGraph graph = null;

        public ElementVisitorImpl(QueryGraph graph) {
            this.graph = graph;
        }

        @Override
        public void visit(ElementPathBlock el) {
            logger.info("Visit: Element Path Block");
            for (TriplePath tp : el.getPattern().getList()) {
                Node sub = tp.getSubject();
                Node pre = tp.getPredicate();
                Node obj = tp.getObject();

                RelationGraphNode subNode;
                if (graph.containsClass(sub.toString())) {
                    subNode = graph.getNodeByName(sub.toString());
                } else subNode = graph.createNode(sub.toString());

                RelationGraphNode objNode;
                if (obj.isLiteral())
                    objNode = graph.createNode((String)obj.getLiteral().getValue());
                else {
                    if (graph.containsClass(obj.toString())) {
                        objNode = graph.getNodeByName(obj.toString());
                    } else
                        objNode = graph.createNode(obj.toString());
                }

                graph.createEdge(pre.toString(), subNode, objNode);
                logger.info(sub.getName() + " <> " + pre.toString() + " <> " + obj.toString());
            }
        }

        @Override
        public void visit(ElementFilter elementFilter) {
            Expr expr = elementFilter.getExpr();
            logger.info("Visit: Element Filter " + expr.toString());
        }

        @Override
        public void visit(ElementGroup el) {
            logger.info("Visit: Element Group");
            for (Element m : el.getElements()) {
                m.visit(this);
            }
        }

        @Override
        public void visit(ElementTriplesBlock el) {
        }

        @Override
        public void visit(ElementAssign el) {
        }

        @Override
        public void visit(ElementBind elementBind) {
        }

        @Override
        public void visit(ElementUnion el) {
        }

        @Override
        public void visit(ElementOptional el) {
        }

        @Override
        public void visit(ElementDataset el) {
        }

        @Override
        public void visit(ElementNamedGraph el) {
        }

        @Override
        public void visit(ElementExists elementExists) {
        }

        @Override
        public void visit(ElementNotExists elementNotExists) {
        }

        @Override
        public void visit(ElementMinus elementMinus) {
        }

        @Override
        public void visit(ElementService el) {
        }

        @Override
        public void visit(ElementFetch el) {
        }

        @Override
        public void visit(ElementSubQuery el) {
        }
    }

}
