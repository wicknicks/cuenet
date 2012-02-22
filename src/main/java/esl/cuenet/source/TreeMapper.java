package esl.cuenet.source;

import esl.cuenet.query.QueryOperator;
import org.apache.log4j.Logger;
import sun.reflect.generics.tree.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class TreeMapper implements IMapper {

    private TreeMapperNode root = new TreeMapperNode();
    private Logger logger = Logger.getLogger(TreeMapper.class);

    public TreeMapper() {
        root.name = "//";
        root.adornment = null;
        root.attribute = null;
        root.operator = null;
    }

    @Override
    public void map(String pathExpression, Adornment adornment,
                    QueryOperator operator, Attribute attribute) {

        if (pathExpression == null) throw new NullPointerException("Null Path Expression");

        if (containsPattern(pathExpression)) logger.info("Path already in tree: " + pathExpression);
        else {
            TreeMapperNode node = completeExpression(pathExpression);
            node.adornment = adornment;
            node.operator = operator;
            node.attribute = attribute;
        }

    }

    @Override
    public void map(String pathExpression, Adornment adornment) {
        TreeMapperNode node = null;
        if (containsPattern(pathExpression)) {
            logger.info("[map-adornment] Path already in tree: " + pathExpression);
            TreeMapperNode[] exp = findSubExpressionNode(pathExpression);
            node = exp[exp.length - 1];
        } else {
            node = completeExpression(pathExpression);
        }
        node.adornment = adornment;
    }

    @Override
    public void map(String pathExpression, QueryOperator operator) {
        TreeMapperNode node = null;
        if (containsPattern(pathExpression)) {
            logger.info("[map-operator] Path already in tree: " + pathExpression);
            TreeMapperNode[] exp = findSubExpressionNode(pathExpression);
            node = exp[exp.length - 1];
        } else {
            node = completeExpression(pathExpression);
        }

        node.operator = operator;
    }

    @Override
    public void map(String pathExpression, Attribute attribute) {
        TreeMapperNode node = null;
        if (containsPattern(pathExpression)) {
            logger.info("[map-attribute] Path already in tree: " + pathExpression);
            TreeMapperNode[] exp = findSubExpressionNode(pathExpression);
            node = exp[exp.length - 1];
        } else {
            node = completeExpression(pathExpression);
        }

        node.attribute = attribute;
    }

    @Override
    public boolean containsPattern(String pathExpression) {
        String exp = buildExpression(findSubExpressionNode(pathExpression));
        return exp.compareTo(pathExpression) == 0;
    }

    @Override
    public Attribute get(String pathExpression) {
        if (!containsPattern(pathExpression))
            throw new NullPointerException("Path Expression not found: " + pathExpression);

        TreeMapperNode[] nodes = findSubExpressionNode(pathExpression);
        return nodes[nodes.length - 1].attribute;
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
