package source;

import com.hp.hpl.jena.rdf.model.Statement;
import esl.cuenet.source.Source;

public interface TISource {

    Source.TYPE getType();

    Source.IO getIO();

    TIAttribute[] getAllAttributes();

    void query(Statement[] statements);

}
