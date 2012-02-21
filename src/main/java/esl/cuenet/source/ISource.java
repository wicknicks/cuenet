package esl.cuenet.source;

import com.hp.hpl.jena.rdf.model.Literal;
import esl.cuenet.query.IResultSet;

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

    TYPE getType();
    IO getIO();
    String getName();
    IMapper getMapper();

    void setAttributes(Attribute[] attributes);
    Attribute[] getAttributes();

    IResultSet query(String[] pathExpression, Literal[] literals) throws SourceQueryException;

}
