package esl.cuenet.source;

import esl.cuenet.query.IResultSet;
import esl.datastructures.Location;
import esl.datastructures.TimeInterval;

public interface IAccessor {

    void setAttributeNames(Attribute[] attributes) throws AccesorInitializationException;

    void start();


    void associateTimeInterval(Attribute attribute, TimeInterval timeInterval) throws AccesorInitializationException;
    void associateLocation(Attribute attribute, Location timeInterval) throws AccesorInitializationException;
    void associateLong(Attribute attribute, long value) throws AccesorInitializationException;
    void associateString(Attribute attribute, String value) throws AccesorInitializationException;
    void associateDouble(Attribute attribute, double value) throws AccesorInitializationException;

    IResultSet executeQuery() throws SourceQueryException;

}
