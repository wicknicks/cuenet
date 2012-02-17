package esl.cuenet.mapper.tree;

import org.apache.log4j.Logger;

import java.util.*;

public class ParseTree implements IParseTree, IParseTreeCreator {

    private String mappingsFile = null;
    private ParseTreeNode document = new ParseTreeNode(IParseTreeNode.Type.ROOT, "document");
    private Stack<ParseTreeNode> parentStack = new Stack<ParseTreeNode>();
    
    private Logger logger = Logger.getLogger(ParseTree.class);

    public ParseTree(String mappingsFile) {
        this.mappingsFile = mappingsFile;
        parentStack.add(document);
        logger.info("Created Document Node");
    }

    @Override
    public IParseTreeNode getDocument() {
        return document;
    }

    @Override
    public void addOperator(String label) {

        if (parentStack.size() == 0) throw new RuntimeException("Premature stack underflow");

        ParseTreeNode parent = parentStack.peek();
        ParseTreeNode current = new ParseTreeNode(IParseTreeNode.Type.OPERATOR, label);

        parent.addChild(current);

        parentStack.add(current);
    }

    @Override
    public void addOperand(String operandValue) {
        ParseTreeNode parent = parentStack.peek();
        ParseTreeNode current = new ParseTreeNode(IParseTreeNode.Type.OPERAND, operandValue);

        parent.addChild(current);
    }

    @Override
    public void startSExpression() {

    }

    @Override
    public void endSExpression() {
        if (parentStack.size() <= 1) throw new RuntimeException("endSExpression: premature stack underflow");

        parentStack.pop();
    }

    @Override
    public void eof() {
        logger.info("Reached End of File");
    }

    @Override
    public IParseTree getTree() {
        return this;
    }

    private class ParseTreeNode implements IParseTreeNode {

        Type type;
        String label;
        List<IParseTreeNode> children = null;
        
        public ParseTreeNode() {
            this.type = null;
            this.label = null;
            children = new ArrayList<IParseTreeNode>();
        }
        
        public ParseTreeNode(Type type, String label) {
            this.type = type;
            this.label = label;
            children = new ArrayList<IParseTreeNode>();
        }

        public void addChild(IParseTreeNode node) {
            children.add(node);
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public List<IParseTreeNode> children() {
            return children;
        }
    }
}
