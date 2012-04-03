package esl.cuenet.source;

import esl.cuenet.mapper.tree.IParseTreeNode;
import esl.datastructures.graph.Graph;

public interface IRelationGraph extends Graph {

    public boolean containsClass(String name);

}
