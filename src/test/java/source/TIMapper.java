package source;

import com.hp.hpl.jena.rdf.model.Literal;

public interface TIMapper {

    /*
         Maps a path to a node in the tree mapper to an attribute in the source.
         Also associates the adornment, operatory type to this node.
     */
    void map (String pathExpression, TIAdornment adornment, TQueryOperator operator, TIAttribute attribute);

    /*
         Overloaded methods to map patterns
     */

    void map (String pathExpression, TIAdornment adornment);

    void map (String pathExpression, TQueryOperator operator);

    void map (String pathExpression, TIAttribute attribute);

    /*
        Returns true if the input pattern maps to some source attribute in the tree mapper.
        False, otherwise.
     */

    boolean containsPattern(String pathExpression);

}
