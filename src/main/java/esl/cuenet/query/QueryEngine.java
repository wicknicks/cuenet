package esl.cuenet.query;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.*;
import esl.cuenet.mapper.tree.SourceMapper;
import esl.cuenet.source.ISource;
import esl.cuenet.source.SourceMapVisitor;
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
    private List<Var> projectVars = null;

    public QueryEngine(OntModel model, SourceMapper sourceMapper) {
        this.model = model;
        this.sourceMapper = sourceMapper;
    }

    public void execute(String sparqlQuery) {
        Query query = QueryFactory.create(sparqlQuery);
        query.getQueryPattern().visit(new ElementVisitorImpl(queryGraph));
        projectVars = query.getProjectVars();
        logger.info("Finished parsing query");

        // convert query triples to individuals and their attributes
        List<RelationGraphNode> allTypedNodes = queryGraph.getAllTypedNodes();
        List<RelationGraphNode> attributeNodes = new ArrayList<RelationGraphNode>();
        for (RelationGraphNode node: allTypedNodes) {
            boolean flag = false;
            for (Var projectVar: projectVars)
            if (node.name().equalsIgnoreCase(projectVar.toString())) flag = true;
            if (flag) attributeNodes.add(node);
        }

        eval();
    }

    private void eval() {
        if (sourceMapper == null) {
            logger.info("No sources added. Returning");
            return;
        }

        sourceMapper.accept(new SourceVisitor(queryGraph));
    }

    public class SourceVisitor implements SourceMapVisitor {
        private QueryGraph graph;

        public SourceVisitor(QueryGraph graph) {
            this.graph = graph;
        }

        @Override
        public void visit(ISource source) {
            logger.info("Visiting " + source.getName());
            List<RelationGraphNode> nodes = graph.getAllNodes();
            for (RelationGraphNode node: nodes) {
                if (source.getRelationGraph().containsClass(node.name()))
                    logger.info("Found! " + node.name());
            }
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
            for (TriplePath tp: el.getPattern().getList()) {
                Node sub = tp.getSubject();
                Node pre = tp.getPredicate();
                Node obj = tp.getObject();

                RelationGraphNode subNode;
                if (graph.containsClass(sub.toString())) {
                    subNode = graph.getNodeByName(sub.toString());
                } else subNode = graph.createNode(sub.toString());

                RelationGraphNode objNode;
                if (obj.isLiteral()) objNode = graph.createNode(obj.toString());
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
            for (Element m: el.getElements()) {
                m.visit(this);
            }
        }

        @Override
        public void visit(ElementTriplesBlock el) { }

        @Override
        public void visit(ElementAssign el) { }

        @Override
        public void visit(ElementBind elementBind) { }

        @Override
        public void visit(ElementUnion el) { }

        @Override
        public void visit(ElementOptional el) { }

        @Override
        public void visit(ElementDataset el) { }

        @Override
        public void visit(ElementNamedGraph el) { }

        @Override
        public void visit(ElementExists elementExists) { }

        @Override
        public void visit(ElementNotExists elementNotExists) { }

        @Override
        public void visit(ElementMinus elementMinus) { }

        @Override
        public void visit(ElementService el) { }

        @Override
        public void visit(ElementFetch el) { }

        @Override
        public void visit(ElementSubQuery el) { }
    }

}
