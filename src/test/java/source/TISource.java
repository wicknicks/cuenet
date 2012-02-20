package source;

import com.hp.hpl.jena.rdf.model.Literal;
import esl.cuenet.source.Source;

public interface TISource {

    Source.TYPE getType();

    Source.IO getIO();

    TIResultSet query(String[] pathExpression, Literal[] literals) throws TSourceQueryException;

}
