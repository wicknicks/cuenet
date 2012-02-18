package esl.cuenet.source;

import com.hp.hpl.jena.rdf.model.Statement;
import esl.cuenet.query.IResultSet;

public interface ISource {

    public Source.TYPE getType();

    public Source.IO getIo();

    public IResultSet query(Statement[] statements);

}
