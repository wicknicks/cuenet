package source;

import com.hp.hpl.jena.rdf.model.Statement;

public interface TIMapper {

    void map(String pattern, String attribute);

    void map(String pattern, int attribute);

    void map(String pattern, Statement[] statements);

}
