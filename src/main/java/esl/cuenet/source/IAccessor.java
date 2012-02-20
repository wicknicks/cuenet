package esl.cuenet.source;

import esl.cuenet.query.IResultSet;

public interface IAccessor {

    void setAttributeNames(Attribute[] attributes) throws AccesorInitializationException;

    void start();

    void associateInt(Attribute attribute, int value) throws AccesorInitializationException;
    void associateString(Attribute attribute, String value) throws AccesorInitializationException;
    void associateDouble(Attribute attribute, double value) throws AccesorInitializationException;

    IResultSet executeQuery() throws SourceQueryException;

}
