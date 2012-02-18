package source;

import esl.cuenet.query.IResultSet;

public interface IAccessor {

    IResultSet query(IAttribute[] available);

}
