package source;

import esl.cuenet.query.IResultSet;

public interface TIAccessor {

    IResultSet query(TIAttribute[] available);

}
