package esl.cuenet.mapper.tree;

import java.util.List;

public interface IParseTreeNode {

    public enum Type {
        OPERATOR,
        OPERAND,
        ROOT
    }

    public Type getType();
    
    public String getLabel();

    public List<IParseTreeNode> children();

}
