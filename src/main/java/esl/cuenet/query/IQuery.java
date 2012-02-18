package esl.cuenet.query;

import com.hp.hpl.jena.rdf.model.Statement;
import esl.cuenet.source.Source;

import java.util.List;

public interface IQuery {

    IResultSet query(Source source, Statement[] statements);

    IResultSet query(Source source, List<Statement> statements);

}
