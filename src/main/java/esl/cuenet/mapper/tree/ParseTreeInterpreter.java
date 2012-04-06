package esl.cuenet.mapper.tree;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.vocabulary.RDF;
import esl.cuenet.query.QueryOperator;
import esl.cuenet.source.Adornment;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.ISource;
import esl.datastructures.graph.relationgraph.IRelationGraph;
import esl.datastructures.graph.relationgraph.RelationGraphEdge;
import esl.datastructures.graph.relationgraph.RelationGraphNode;
import org.apache.log4j.Logger;

import java.util.Iterator;

public class ParseTreeInterpreter {

    private IParseTree parseTree = null;
    private Logger logger = Logger.getLogger(ParseTreeInterpreter.class);
    private SourceMapper sourceMapper = new SourceMapper();

    public ParseTreeInterpreter(IParseTree parseTree) {
        this.parseTree = parseTree;
    }

    public void interpret() throws SourceParseException {

        //process namespace mappings
        IParseTreeNode document = parseTree.getDocument();
        for (IParseTreeNode child : document.children()) {
            if (child.getType() != IParseTreeNode.Type.OPERATOR) continue;

            if (child.getLabel().equalsIgnoreCase(MappingOperators.NAMESPACE))
                interpretNamespace(child);
        }

        for (IParseTreeNode child : document.children()) {
            if (child.getType() != IParseTreeNode.Type.OPERATOR) continue;

            if (child.getLabel().equalsIgnoreCase(MappingOperators.SOURCE))
                interpretSourceDeclaration(child);
        }

    }

    public void setOntologyModel(OntModel model) {
        sourceMapper.setOntologyModel(model);
    }

    private void interpretSourceDeclaration(IParseTreeNode sourceNode) throws SourceParseException {
        ISource source = null;
        for (IParseTreeNode child : sourceNode.children()) {
            if (child.getType() == IParseTreeNode.Type.OPERAND) {
                source = sourceMapper.createSource(child.getLabel());
            }
        }

        for (IParseTreeNode child : sourceNode.children()) {
            if (child.getType() != IParseTreeNode.Type.OPERATOR) continue;

            if (child.getLabel().equalsIgnoreCase(MappingOperators.RELATION))
                associateRelations(source, child);

            if (child.getLabel().equalsIgnoreCase(MappingOperators.SOURCE_IO))
                associateIO(source, child);

            if (child.getLabel().equalsIgnoreCase(MappingOperators.SOURCE_TYPE))
                associateType(source, child);

            if (child.getLabel().equalsIgnoreCase(MappingOperators.AXIOM))
                associateAxioms(source, child);

            if (child.getLabel().equalsIgnoreCase(MappingOperators.ATTRIBUTES))
                associateAttributes(source, child);
        }

    }

    private void associateRelations(ISource source, IParseTreeNode relationNode) {
        IRelationGraph relGraph = source.getRelationGraph();
        if (relationNode.children().size() != 3)
            throw new RuntimeException("Relation node must contain exactly 3 children");

        IParseTreeNode sub = relationNode.children().get(0);
        IParseTreeNode pred = relationNode.children().get(1);
        IParseTreeNode obj = relationNode.children().get(2);

        boolean flag = false;
        RelationGraphNode subNode = relGraph.getNodeByName(sub.getLabel());
        if (subNode != null) {
            for (RelationGraphEdge edge : relGraph.getOutgoingEdges(subNode)) {
                if (relGraph.getDestinationNode(edge).name().compareTo(obj.getLabel()) == 0)
                    flag = true;
            }
        }
        if (flag) {
            throw new RuntimeException("Multiple Edges between same node pairs : "
                    + sub.getLabel() + " -> " + obj.getLabel());
        }

        if (subNode == null) subNode = relGraph.createNode(sub.getLabel());

        RelationGraphNode objNode;
        if (relGraph.containsClass(obj.getLabel())) objNode = relGraph.getNodeByName(obj.getLabel());
        else objNode = relGraph.createNode(obj.getLabel());

        if (pred.getLabel().compareTo("type")==0)
            relGraph.createEdge(RDF.type.getURI(), subNode, objNode);
        else
            relGraph.createEdge(pred.getLabel(), subNode, objNode);
    }

    private void associateAxioms(ISource source, IParseTreeNode axiomNode) {

        for (IParseTreeNode child : axiomNode.children()) {
            if (child.getType() == IParseTreeNode.Type.OPERAND)
                throw new SourceInitializationException("Found an operand inside the Axiom Node");

            if (child.getLabel().compareTo(MappingOperators.MAP) == 0) {
                mapAttribute(source, child);
            }
        }

    }

    private void mapAttribute(ISource source, IParseTreeNode mapNode) {
        boolean fContainsOperatorChildren = false;

        for (IParseTreeNode child : mapNode.children())
            if (child.getType() == IParseTreeNode.Type.OPERATOR) fContainsOperatorChildren = true;

        if (!fContainsOperatorChildren) {
            String[] operands = new String[mapNode.children().size()];
            int ix = 0;
            for (IParseTreeNode child : mapNode.children()) operands[ix++] = child.getLabel();

            if (operands.length < 2) throw new SourceInitializationException("Insufficient Mapping Axiom");
            else source.getMapper().map(operands[0], new Attribute(operands[1]));

            for (int i = 2; i < operands.length; i++) {
                if (operands[i].charAt(0) == '[') associateOperators(source, operands[0], operands[i]);
                else associateAdornment(source, operands[0], operands[i]);
            }
        }

        if (fContainsOperatorChildren) {
            int count = 0;
            for (IParseTreeNode child : mapNode.children())
                if (child.getType() == IParseTreeNode.Type.OPERAND) count++;

            String[] operands = new String[count];
            int ix = 0;
            for (IParseTreeNode child : mapNode.children())
                if (child.getType() == IParseTreeNode.Type.OPERAND) operands[ix++] = child.getLabel();

            if (operands.length == 1) source.getMapper().map(operands[0], null, null, null);
            for (int i = 1; i < operands.length; i++) {
                if (operands[i].charAt(0) == '[') associateOperators(source, operands[0], operands[i]);
                else associateAdornment(source, operands[0], operands[i]);
            }

            //process operators
            for (IParseTreeNode child : mapNode.children()) {
                if (child.getType() == IParseTreeNode.Type.OPERATOR) {
                    if (child.getLabel().compareTo(MappingOperators.PROP) == 0) {
                        source.getMapper().map(operands[0] + "." + child.children().get(0).getLabel(),
                                new Attribute(child.children().get(1).getLabel()));
                    }
                }
            }
        }

    }

    private void associateAdornment(ISource source, String pathExpression, String adornmentLabel) {
        Adornment adornment;

        if (adornmentLabel.compareTo("F") == 0) adornment = new Adornment(Adornment.AdornmentType.Free);
        else if (adornmentLabel.compareTo("U") == 0) adornment = new Adornment(Adornment.AdornmentType.Unspecifiable);
        else if (adornmentLabel.compareTo("B") == 0) adornment = new Adornment(Adornment.AdornmentType.Bound);
        else if (adornmentLabel.compareTo("C") == 0) adornment = new Adornment(Adornment.AdornmentType.Constant);
        else if (adornmentLabel.compareTo("O") == 0) adornment = new Adornment(Adornment.AdornmentType.Optional);
        else throw new SourceInitializationException("Invalid Adornment: " + adornmentLabel);

        source.getMapper().map(pathExpression, adornment);
    }

    private void associateOperators(ISource source, String pathExpression, String operatorLabelArray) {

        QueryOperator queryOperator;
        operatorLabelArray = operatorLabelArray.substring(1, operatorLabelArray.indexOf(']'));

        if (operatorLabelArray.compareTo("EQUALS") == 0)
            queryOperator = new QueryOperator(QueryOperator.Operators.EQUALS);
        else if (operatorLabelArray.compareTo("T_STARTS") == 0)
            queryOperator = new QueryOperator(QueryOperator.Operators.TEMPORAL_STARTS);
        else if (operatorLabelArray.compareTo("T_FINISHES") == 0)
            queryOperator = new QueryOperator(QueryOperator.Operators.TEMPORAL_FINISHES);
        else if (operatorLabelArray.compareTo("T_INTERSECTS") == 0)
            queryOperator = new QueryOperator(QueryOperator.Operators.TEMPORAL_INTERSECTS);
        else if (operatorLabelArray.compareTo("S_OVERLAPS") == 0)
            queryOperator = new QueryOperator(QueryOperator.Operators.SPATIAL_OVERLAPS);
        else if (operatorLabelArray.compareTo("R_CONTAINS") == 0)
            queryOperator = new QueryOperator(QueryOperator.Operators.STRING_CONTAINS);
        else if (operatorLabelArray.compareTo("S_NEARBY") == 0)
            queryOperator = new QueryOperator(QueryOperator.Operators.SPATIAL_NEARBY);
        else if (operatorLabelArray.compareTo("R_FUZZY") == 0)
            queryOperator = new QueryOperator(QueryOperator.Operators.STRING_FUZZY);
        else throw new SourceInitializationException("Invalid Query Operator: " + operatorLabelArray);

        source.getMapper().map(pathExpression, queryOperator);
    }

    private void associateAttributes(ISource source, IParseTreeNode attributeNode) {
        Attribute[] srcAttributes = new Attribute[attributeNode.children().size()];
        int ix = 0;
        for (IParseTreeNode child : attributeNode.children()) {
            srcAttributes[ix++] = new Attribute(child.getLabel());
        }

        source.setAttributes(srcAttributes);
    }

    private void associateType(ISource source, IParseTreeNode type) throws SourceParseException {
        IParseTreeNode node = type.children().get(0);
        if (node.getLabel().equalsIgnoreCase("personal")) source.setType(ISource.TYPE.PERSONAL);
        else if (node.getLabel().equalsIgnoreCase("social")) source.setType(ISource.TYPE.SOCIAL);
        else if (node.getLabel().equalsIgnoreCase("public")) source.setType(ISource.TYPE.PUBLIC);
        else throw new SourceParseException("Unknown Source type associated with source " + source.getName());
    }

    private void associateIO(ISource source, IParseTreeNode io) {
        IParseTreeNode node = io.children().get(0);
        if (node.getLabel().equalsIgnoreCase("disk")) source.setIO(ISource.IO.DISK);
        else if (node.getLabel().equalsIgnoreCase("network")) source.setIO(ISource.IO.NETWORK);
        else throw new RuntimeException("Unknown IO method associated to source " + source.getName());
    }

    private void interpretNamespace(IParseTreeNode namespaceNode) {
        if (namespaceNode.children().size() != 2) throw new RuntimeException("Bad namespace node");

        Iterator<IParseTreeNode> children = namespaceNode.children().iterator();

        IParseTreeNode uriNode = children.next();   // get the uri
        IParseTreeNode shortHandNode = children.next();   // get the shorthand

        sourceMapper.addNamespaceMapping(uriNode.getLabel(), shortHandNode.getLabel());
    }

    public SourceMapper getSourceMapper() {
        return sourceMapper;
    }

}
