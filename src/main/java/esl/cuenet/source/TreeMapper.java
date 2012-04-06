package esl.cuenet.source;

import com.hp.hpl.jena.ontology.EnumeratedClass;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import esl.cuenet.query.QueryOperator;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class TreeMapper implements IMapper {

    private TreeMapperNode root = new TreeMapperNode();
    private Logger logger = Logger.getLogger(TreeMapper.class);
    private OntModel model = null;
    private HashMap<String, String> namespaceMap;

    public TreeMapper(HashMap<String, String> namespaceMap) {
        root.name = "//";
        root.adornment = null;
        root.attribute = null;
        root.operator = null;

        if (namespaceMap == null) this.namespaceMap = new HashMap<String, String>();
        else this.namespaceMap = namespaceMap;
    }

    @Override
    public void map(String pathExpression, Adornment adornment,
                    QueryOperator operator, Attribute attribute) {

        if (pathExpression == null) throw new NullPointerException("Null Path Expression");

        if (containsPattern(pathExpression)) logger.debug("Path already in tree: " + pathExpression);
        else {
            TreeMapperNode node = completeExpression(pathExpression);
            node.adornment = adornment;
            node.operator = operator;
            node.attribute = attribute;
            if (node.resourcePath == null) node.resourcePath = nsEval(pathExpression);
        }
    }

    @Override
    public void map(String pathExpression, Adornment adornment) {

        TreeMapperNode node;
        if (containsPattern(pathExpression)) {
            logger.debug("[map-adornment] Path already in tree: " + pathExpression);
            TreeMapperNode[] exp = findSubExpressionNode(pathExpression);
            node = exp[exp.length - 1];
        } else {
            node = completeExpression(pathExpression);
        }
        node.adornment = adornment;
        if (node.resourcePath == null) node.resourcePath = nsEval(pathExpression);
    }

    @Override
    public void map(String pathExpression, QueryOperator operator) {

        TreeMapperNode node;
        if (containsPattern(pathExpression)) {
            logger.debug("[map-operator] Path already in tree: " + pathExpression);
            TreeMapperNode[] exp = findSubExpressionNode(pathExpression);
            node = exp[exp.length - 1];
        } else {
            node = completeExpression(pathExpression);
        }

        node.operator = operator;
        if (node.resourcePath == null) node.resourcePath = nsEval(pathExpression);
    }

    @Override
    public void map(String pathExpression, Attribute attribute) {

        TreeMapperNode node;
        if (containsPattern(pathExpression)) {
            logger.debug("[map-attribute] Path already in tree: " + pathExpression);
            TreeMapperNode[] exp = findSubExpressionNode(pathExpression);
            node = exp[exp.length - 1];
        } else {
            node = completeExpression(pathExpression);
        }

        node.attribute = attribute;
        if (node.resourcePath == null) node.resourcePath = nsEval(pathExpression);
    }

    private Resource[] nsEval(String pathExpression) {
        if (namespaceMap.size() == 0) return new Resource[0];

        String[] classnames = pathExpression.split("\\.");
        Resource[] resourcePath = new Resource[classnames.length];

        String uri;
        for (int i = 0; i < classnames.length - 1; i++) {
            uri = namespaceMap.get("this") + classnames[i];
            resourcePath[i] = model.getOntClass(uri);
            if (resourcePath[i] == null) logger.error("Class not found: " + uri + " " + pathExpression);
        }

        int last = classnames.length - 1;

        uri = namespaceMap.get("this") + classnames[last];

        if (model.getOntClass(uri) != null)
            resourcePath[last] = model.getOntClass(uri);
        else if (model.getDatatypeProperty(uri) != null)
            resourcePath[last] = model.getDatatypeProperty(uri);
        else
            logger.error("Class not found: " + uri + " " + pathExpression);

        return resourcePath;
    }

    @Override
    public boolean containsPattern(String pathExpression) {
        String exp = buildExpression(findSubExpressionNode(pathExpression));
        return exp.compareTo(pathExpression) == 0;
    }

    @Override
    public Attribute getAttribute(String pathExpression) {
        if (!containsPattern(pathExpression))
            throw new NullPointerException("Path Expression not found: " + pathExpression);

        TreeMapperNode[] nodes = findSubExpressionNode(pathExpression);
        return nodes[nodes.length - 1].attribute;
    }

    @Override
    public Adornment getAdornment(String pathExpression) {
        if (!containsPattern(pathExpression))
            throw new NullPointerException("Path Expression not found: " + pathExpression);

        TreeMapperNode[] nodes = findSubExpressionNode(pathExpression);
        return nodes[nodes.length - 1].adornment;
    }

    @Override
    public void setOntologyModel(OntModel model) {
        this.model = model;
    }

    @Override
    public OntModel getOntologyModel() {
        return this.model;
    }

    private TreeMapperNode completeExpression(String pathExpression) {

        TreeMapperNode[] partialExpressionNodes = findSubExpressionNode(pathExpression);
        TreeMapperNode start = root;

        String[] names = pathExpression.split("\\.");

        int ix = 0;
        if (partialExpressionNodes[0] != null) {
            for (; ix < partialExpressionNodes.length; ix++)
                if (partialExpressionNodes[ix] == null) {
                    start = partialExpressionNodes[ix - 1];
                    break;
                }
        }

        for (int i = ix; i < names.length; i++) {
            TreeMapperNode node = new TreeMapperNode();
            node.name = names[i];
            start.children.add(node);
            start = node;
        }

        return start;
    }

    private String buildExpression(TreeMapperNode[] nodes) {
        if (nodes == null) return "";
        StringBuilder builder = new StringBuilder();

        for (TreeMapperNode node : nodes)
            if (node != null)
                builder.append(node.name).append('.');

        int eix = builder.lastIndexOf(".");
        if (eix == -1) return "";

        return builder.substring(0, eix);
    }

    private TreeMapperNode[] findSubExpressionNode(String pathExpression) {
        String[] names = pathExpression.split("\\.");
        TreeMapperNode current = root;
        TreeMapperNode[] resultList = new TreeMapperNode[names.length];
        for (TreeMapperNode n : resultList) n = null;

        int ix = 0;

        while (true) {
            List<TreeMapperNode> children = current.children;

            for (TreeMapperNode node : children)
                if (node.name.compareTo(names[ix]) == 0) resultList[ix] = node;

            if (resultList[ix] == null) break;
            else current = resultList[ix];

            ix++;
            if (ix == names.length) break;
        }

        return resultList;
    }

    private class TreeMapperNode {

        public String name;
        public Adornment adornment;
        public QueryOperator operator;
        public Attribute attribute;
        public Resource[] resourcePath = null;
        public List<TreeMapperNode> children = new ArrayList<TreeMapperNode>();

        public String toString() {

            StringBuilder builder = new StringBuilder();
            builder.append('(').append(name).append(" -> ");
            for (TreeMapperNode c : children) builder.append(c.toString()).append("  ");
            builder.append(')');
            return builder.substring(0);
        }
    }

}
