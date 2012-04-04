package esl.cuenet.source;

import com.hp.hpl.jena.rdf.model.Literal;
import esl.cuenet.query.IResultSet;
import esl.datastructures.graph.relationgraph.IRelationGraph;

import java.util.List;

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
    IAccessor getAccessor();
    IRelationGraph getRelationGraph();


    void setAttributes(Attribute[] attributes);
    Attribute[] getAttributes();

    IResultSet query(String[] pathExpression, Literal[] literals) throws SourceQueryException, AccesorInitializationException;

    IResultSet query(List<String> pathExpressions, List<Literal> literals) throws SourceQueryException, AccesorInitializationException;

}
