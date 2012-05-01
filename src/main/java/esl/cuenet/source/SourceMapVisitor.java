package esl.cuenet.source;

import esl.cuenet.query.IResultSet;

public interface SourceMapVisitor {

    public IResultSet visit(ISource source);

}
