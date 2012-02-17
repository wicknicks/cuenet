package esl.cuenet.mapper.tree;

public interface IParseTreeCreator {
    
    void addOperator(String label);
    
    void addOperand(String operandValue);

    void startSExpression();

    void endSExpression();

    void eof();
    
    IParseTree getTree();
    
}
