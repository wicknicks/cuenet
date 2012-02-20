package esl.cuenet.source;

import com.hp.hpl.jena.rdf.model.Literal;

public interface ISource {

    public enum IO {
        DISK,
        NETWORK
    }

    public enum TYPE {
        PUBLIC,
        SOCIAL,
        PERSONAL
    }


    void setType (TYPE type);
    void setIO (IO type);
    void setName (String name);

    TYPE getType();
    IO getIO();
    String getName();


    void setAttributeNames(Attribute[] names);
    IResultSet query(String[] pathExpression, Literal[] literals) throws SourceQueryException;

}
