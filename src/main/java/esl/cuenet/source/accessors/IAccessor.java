package esl.cuenet.source.accessors;

import esl.cuenet.query.IResultSet;

public interface IAccessor {

    IAttribute[] attributes();

    IResultSet query(IAttribute[] inputAttributes);

}
